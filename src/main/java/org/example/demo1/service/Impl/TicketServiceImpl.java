package org.example.demo1.service.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.demo1.common.ErrorCode;
import org.example.demo1.context.UserContext;
import org.example.demo1.exception.BusinessException;
import org.example.demo1.mapper.RepairCategoryMapper;
import org.example.demo1.mapper.RepairTicketLogMapper;
import org.example.demo1.mapper.RepairTicketMapper;
import org.example.demo1.mapper.SysUserMapper;
import org.example.demo1.pojo.PageResult;
import org.example.demo1.pojo.entity.RepairCategory;
import org.example.demo1.pojo.entity.RepairTicket;
import org.example.demo1.pojo.entity.RepairTicketLog;
import org.example.demo1.pojo.entity.TicketStatus;
import org.example.demo1.service.TicketService;
import org.example.demo1.util.DateTimeUtil;
import org.example.demo1.util.PermissionChecker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TicketServiceImpl implements TicketService {
    private static final int MAX_IN_PROGRESS = 5;

    private final RepairTicketMapper repairTicketMapper;
    private final RepairTicketLogMapper repairTicketLogMapper;
    private final RepairCategoryMapper repairCategoryMapper;
    private final SysUserMapper sysUserMapper;

    public TicketServiceImpl(RepairTicketMapper repairTicketMapper,
                             RepairTicketLogMapper repairTicketLogMapper,
                             RepairCategoryMapper repairCategoryMapper,
                             SysUserMapper sysUserMapper) {
        this.repairTicketMapper = repairTicketMapper;
        this.repairTicketLogMapper = repairTicketLogMapper;
        this.repairCategoryMapper = repairCategoryMapper;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    @Transactional
    public Map<String, Object> create(Map<String, Object> request) {
        PermissionChecker.require("ticket:create");
        Long userId = UserContext.getUserId();

        Long categoryId = toLong(request.get("categoryId"));
        String title = (String) request.get("title");
        String description = (String) request.get("description");
        String location = (String) request.get("location");
        if (categoryId == null || !StringUtils.hasText(title) || !StringUtils.hasText(description) || !StringUtils.hasText(location)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "参数错误");
        }
        if (title.length() > 100 || location.length() > 100) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "参数错误");
        }

        Long lockedUser = sysUserMapper.lockByIdForUpdate(userId);
        if (lockedUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "资源不存在");
        }
        // 行锁分类，与“删除分类”操作互斥，避免删除分类时仍有新工单引用该分类
        RepairCategory category = repairCategoryMapper.findByIdForUpdate(categoryId);
        if (category == null || category.getStatus() == null || category.getStatus() != 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "分类不存在或已停用");
        }
        int inProgress = repairTicketMapper.countInProgressByUserId(userId);
        if (inProgress >= MAX_IN_PROGRESS) {
            throw new BusinessException(ErrorCode.CONFLICT, "进行中工单已达上限");
        }

        RepairTicket ticket = new RepairTicket();
        ticket.setUserId(userId);
        ticket.setCategoryId(categoryId);
        ticket.setCategoryName(category.getName());
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setLocation(location);
        ticket.setContactPhone((String) request.get("contactPhone"));
        ticket.setImageUrl((String) request.get("imageUrl"));
        ticket.setStatus(TicketStatus.PENDING);
        repairTicketMapper.insert(ticket);

        writeLog(ticket.getId(), userId, null, TicketStatus.PENDING, "CREATE", "用户提交报修");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", ticket.getId());
        data.put("status", ticket.getStatus());
        return data;
    }

    @Override
    public PageResult<Map<String, Object>> myTickets(int page, int size, String status) {
        PermissionChecker.require("ticket:view:own");
        PageHelper.startPage(page, size);
        List<RepairTicket> list = repairTicketMapper.findMine(UserContext.getUserId(), status);
        PageInfo<RepairTicket> pageInfo = new PageInfo<>(list);
        List<Map<String, Object>> voList = list.stream().map(this::toListVo).collect(Collectors.toList());
        return new PageResult<>(voList, pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal());
    }

    @Override
    public Map<String, Object> getMyTicketDetail(Long id) {
        PermissionChecker.require("ticket:view:own");
        RepairTicket ticket = repairTicketMapper.findDetailById(id);
        if (ticket == null || !UserContext.getUserId().equals(ticket.getUserId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "资源不存在");
        }
        return toDetailVo(ticket);
    }

    @Override
    @Transactional
    public void cancelMyTicket(Long id) {
        PermissionChecker.require("ticket:cancel:own");
        transition(id, TicketStatus.PENDING, TicketStatus.CANCELLED, "CANCEL", "用户取消工单",
                ticket -> ticket.setCancelTime(LocalDateTime.now()), true);
    }

    @Override
    public PageResult<Map<String, Object>> adminTickets(int page, int size, String status, Long categoryId, String keyword) {
        PermissionChecker.require("ticket:view:all");
        PageHelper.startPage(page, size);
        List<RepairTicket> list = repairTicketMapper.findAdminList(status, categoryId, keyword);
        PageInfo<RepairTicket> pageInfo = new PageInfo<>(list);
        List<Map<String, Object>> voList = list.stream().map(this::toAdminListVo).collect(Collectors.toList());
        return new PageResult<>(voList, pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal());
    }

    @Override
    public Map<String, Object> adminTicketDetail(Long id) {
        PermissionChecker.require("ticket:view:all");
        RepairTicket ticket = repairTicketMapper.findDetailById(id);
        if (ticket == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "资源不存在");
        }
        return toAdminDetailVo(ticket);
    }

    @Override
    @Transactional
    public void accept(Long id) {
        PermissionChecker.require("ticket:accept");
        transition(id, TicketStatus.PENDING, TicketStatus.ACCEPTED, "ACCEPT", "管理员受理",
                ticket -> {
                    ticket.setHandlerId(UserContext.getUserId());
                    ticket.setAcceptTime(LocalDateTime.now());
                }, false);
    }

    @Override
    @Transactional
    public void reject(Long id, String reason) {
        PermissionChecker.require("ticket:reject");
        if (!StringUtils.hasText(reason) || reason.length() > 200) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "参数错误");
        }
        transition(id, TicketStatus.PENDING, TicketStatus.REJECTED, "REJECT", reason,
                ticket -> ticket.setRejectReason(reason), false);
    }

    @Override
    @Transactional
    public void start(Long id) {
        PermissionChecker.require("ticket:start");
        transition(id, TicketStatus.ACCEPTED, TicketStatus.PROCESSING, "START", "开始处理", ticket -> {
        }, false);
    }

    @Override
    @Transactional
    public void finish(Long id, String result) {
        PermissionChecker.require("ticket:finish");
        if (!StringUtils.hasText(result) || result.length() > 500) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "参数错误");
        }
        transition(id, TicketStatus.PROCESSING, TicketStatus.DONE, "FINISH", result,
                ticket -> {
                    ticket.setHandleResult(result);
                    ticket.setFinishTime(LocalDateTime.now());
                }, false);
    }

    @Override
    @Transactional
    public void forceCancel(Long id, String remark) {
        PermissionChecker.require("ticket:force-cancel");
        if (remark != null && remark.length() > 200) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "参数错误");
        }
        RepairTicket locked = repairTicketMapper.findByIdForUpdate(id);
        if (locked == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "资源不存在");
        }
        if (TicketStatus.TERMINAL.contains(locked.getStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT, "工单状态不允许此操作");
        }
        String fromStatus = locked.getStatus();
        RepairTicket update = buildUpdate(locked, fromStatus, TicketStatus.CANCELLED);
        update.setCancelTime(LocalDateTime.now());
        applyUpdate(update);
        writeLog(id, UserContext.getUserId(), fromStatus, TicketStatus.CANCELLED, "CANCEL", remark);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        PermissionChecker.require("ticket:delete");
        RepairTicket ticket = repairTicketMapper.findById(id);
        if (ticket == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "资源不存在");
        }
        repairTicketLogMapper.deleteByTicketId(id);
        repairTicketMapper.deleteById(id);
    }

    private void transition(Long id, String fromStatus, String toStatus, String action, String content,
                            java.util.function.Consumer<RepairTicket> extraSetter, boolean ownerOnly) {
        RepairTicket locked = repairTicketMapper.findByIdForUpdate(id);
        if (locked == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "资源不存在");
        }
        if (ownerOnly && !UserContext.getUserId().equals(locked.getUserId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "资源不存在");
        }
        if (!fromStatus.equals(locked.getStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT, "工单状态不允许此操作");
        }
        RepairTicket update = buildUpdate(locked, fromStatus, toStatus);
        extraSetter.accept(update);
        applyUpdate(update);
        writeLog(id, UserContext.getUserId(), fromStatus, toStatus, action, content);
    }

    private RepairTicket buildUpdate(RepairTicket locked, String fromStatus, String toStatus) {
        Long id = locked.getId();
        RepairTicket update = new RepairTicket();
        update.setId(id);
        update.setFromStatus(fromStatus);
        update.setToStatus(toStatus);
        update.setStatus(toStatus);
        update.setRejectReason(locked.getRejectReason());
        update.setHandleResult(locked.getHandleResult());
        update.setHandlerId(locked.getHandlerId());
        update.setAcceptTime(locked.getAcceptTime());
        update.setFinishTime(locked.getFinishTime());
        update.setCancelTime(locked.getCancelTime());
        return update;
    }

    private void applyUpdate(RepairTicket update) {
        int rows = repairTicketMapper.updateStatus(update);
        if (rows != 1) {
            throw new BusinessException(ErrorCode.CONFLICT, "工单状态不允许此操作");
        }
    }

    private void writeLog(Long ticketId, Long operatorId, String fromStatus, String toStatus, String action, String content) {
        RepairTicketLog log = new RepairTicketLog();
        log.setTicketId(ticketId);
        log.setOperatorId(operatorId);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setAction(action);
        log.setContent(content);
        repairTicketLogMapper.insert(log);
    }

    private Map<String, Object> toListVo(RepairTicket ticket) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", ticket.getId());
        map.put("categoryId", ticket.getCategoryId());
        map.put("categoryName", ticket.getCategoryName());
        map.put("title", ticket.getTitle());
        map.put("location", ticket.getLocation());
        map.put("status", ticket.getStatus());
        map.put("createTime", DateTimeUtil.format(ticket.getCreateTime()));
        map.put("updateTime", DateTimeUtil.format(ticket.getUpdateTime()));
        return map;
    }

    private Map<String, Object> toDetailVo(RepairTicket ticket) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", ticket.getId());
        map.put("userId", ticket.getUserId());
        map.put("categoryId", ticket.getCategoryId());
        map.put("categoryName", ticket.getCategoryName());
        map.put("title", ticket.getTitle());
        map.put("description", ticket.getDescription());
        map.put("location", ticket.getLocation());
        map.put("contactPhone", ticket.getContactPhone());
        map.put("imageUrl", ticket.getImageUrl());
        map.put("status", ticket.getStatus());
        map.put("rejectReason", ticket.getRejectReason());
        map.put("handleResult", ticket.getHandleResult());
        map.put("handlerId", ticket.getHandlerId());
        map.put("handlerName", ticket.getHandlerName());
        map.put("acceptTime", DateTimeUtil.format(ticket.getAcceptTime()));
        map.put("finishTime", DateTimeUtil.format(ticket.getFinishTime()));
        map.put("cancelTime", DateTimeUtil.format(ticket.getCancelTime()));
        map.put("createTime", DateTimeUtil.format(ticket.getCreateTime()));
        map.put("updateTime", DateTimeUtil.format(ticket.getUpdateTime()));
        return map;
    }

    private Map<String, Object> toAdminListVo(RepairTicket ticket) {
        Map<String, Object> map = toListVo(ticket);
        map.put("userId", ticket.getUserId());
        map.put("username", ticket.getUsername());
        map.put("nickname", ticket.getNickname());
        return map;
    }

    private Map<String, Object> toAdminDetailVo(RepairTicket ticket) {
        Map<String, Object> map = toDetailVo(ticket);
        map.put("username", ticket.getUsername());
        map.put("nickname", ticket.getNickname());
        return map;
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException ex) {
            // 非法数字视为参数错误，由上层参数校验统一返回 40001
            return null;
        }
    }
}

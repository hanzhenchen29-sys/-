package org.example.demo1.service.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.demo1.common.ErrorCode;
import org.example.demo1.exception.BusinessException;
import org.example.demo1.mapper.RepairCategoryMapper;
import org.example.demo1.pojo.PageResult;
import org.example.demo1.pojo.entity.RepairCategory;
import org.example.demo1.service.CategoryService;
import org.example.demo1.service.cache.CategoryCacheService;
import org.example.demo1.util.DateTimeUtil;
import org.example.demo1.util.PermissionChecker;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final RepairCategoryMapper repairCategoryMapper;
    private final CategoryCacheService categoryCacheService;

    public CategoryServiceImpl(RepairCategoryMapper repairCategoryMapper,
                               CategoryCacheService categoryCacheService) {
        this.repairCategoryMapper = repairCategoryMapper;
        this.categoryCacheService = categoryCacheService;
    }

    @Override
    public List<Map<String, Object>> listEnabled() {
        PermissionChecker.require("category:view");
        return categoryCacheService.getEnabledCategories().stream()
                .map(this::toSimpleVo)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<Map<String, Object>> adminPage(int page, int size, String name, Integer status) {
        PermissionChecker.require("category:manage");
        PageHelper.startPage(page, size);
        List<RepairCategory> list = repairCategoryMapper.findByCondition(name, status);
        PageInfo<RepairCategory> pageInfo = new PageInfo<>(list);
        List<Map<String, Object>> voList = list.stream().map(this::toAdminVo).collect(Collectors.toList());
        return new PageResult<>(voList, pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal());
    }

    @Override
    public Long create(String name, Integer sortNo) {
        PermissionChecker.require("category:manage");
        if (!StringUtils.hasText(name)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "参数错误");
        }
        RepairCategory category = new RepairCategory();
        category.setName(name);
        category.setSortNo(sortNo == null ? 0 : sortNo);
        try {
            repairCategoryMapper.insert(category);
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.CONFLICT, "分类名称已存在");
        }
        categoryCacheService.evictEnabledCache();
        return category.getId();
    }

    @Override
    @Transactional
    public void update(Long id, String name, Integer status, Integer sortNo) {
        PermissionChecker.require("category:manage");
        RepairCategory existing = repairCategoryMapper.findById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "资源不存在");
        }
        if (!StringUtils.hasText(name) || status == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "参数错误");
        }
        RepairCategory category = new RepairCategory();
        category.setId(id);
        category.setName(name);
        category.setStatus(status);
        category.setSortNo(sortNo == null ? existing.getSortNo() : sortNo);
        repairCategoryMapper.update(category);
        evictCacheAfterCommit();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        PermissionChecker.require("category:manage");
        // 行锁分类，与“创建工单”互斥，防止删除分类时仍有新工单引用该分类
        RepairCategory existing = repairCategoryMapper.findByIdForUpdate(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "资源不存在");
        }
        if (repairCategoryMapper.countTicketsByCategoryId(id) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "该分类下仍有关联工单，无法删除");
        }
        repairCategoryMapper.deleteById(id);
        evictCacheAfterCommit();
    }

    private void evictCacheAfterCommit() {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                categoryCacheService.evictEnabledCache();
            }
        });
    }

    private Map<String, Object> toSimpleVo(RepairCategory category) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", category.getId());
        map.put("name", category.getName());
        map.put("status", category.getStatus());
        map.put("sortNo", category.getSortNo());
        return map;
    }

    private Map<String, Object> toAdminVo(RepairCategory category) {
        Map<String, Object> map = toSimpleVo(category);
        map.put("createTime", DateTimeUtil.format(category.getCreateTime()));
        map.put("updateTime", DateTimeUtil.format(category.getUpdateTime()));
        return map;
    }
}

package org.example.demo1.service.Impl;

import org.example.demo1.mapper.RepairTicketMapper;
import org.example.demo1.pojo.entity.TicketStatus;
import org.example.demo1.service.DashboardService;
import org.example.demo1.util.PermissionChecker;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {
    private final RepairTicketMapper repairTicketMapper;

    public DashboardServiceImpl(RepairTicketMapper repairTicketMapper) {
        this.repairTicketMapper = repairTicketMapper;
    }

    @Override
    public Map<String, Object> overview() {
        PermissionChecker.require("dashboard:view");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("ticketTotal", repairTicketMapper.countAll());
        data.put("pendingCount", 0L);
        data.put("acceptedCount", 0L);
        data.put("processingCount", 0L);
        data.put("doneCount", 0L);
        data.put("rejectedCount", 0L);
        data.put("cancelledCount", 0L);

        List<Map<String, Object>> groups = repairTicketMapper.countGroupByStatus();
        for (Map<String, Object> group : groups) {
            String status = String.valueOf(group.get("status"));
            long count = ((Number) group.get("cnt")).longValue();
            switch (status) {
                case TicketStatus.PENDING -> data.put("pendingCount", count);
                case TicketStatus.ACCEPTED -> data.put("acceptedCount", count);
                case TicketStatus.PROCESSING -> data.put("processingCount", count);
                case TicketStatus.DONE -> data.put("doneCount", count);
                case TicketStatus.REJECTED -> data.put("rejectedCount", count);
                case TicketStatus.CANCELLED -> data.put("cancelledCount", count);
                default -> {
                }
            }
        }
        data.put("todayNewCount", repairTicketMapper.countTodayNew());
        return data;
    }
}

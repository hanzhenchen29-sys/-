package org.example.demo1.service;

import org.example.demo1.pojo.PageResult;

import java.util.Map;

public interface TicketService {
    Map<String, Object> create(Map<String, Object> request);

    PageResult<Map<String, Object>> myTickets(int page, int size, String status);

    Map<String, Object> getMyTicketDetail(Long id);

    void cancelMyTicket(Long id);

    PageResult<Map<String, Object>> adminTickets(int page, int size, String status, Long categoryId, String keyword);

    Map<String, Object> adminTicketDetail(Long id);

    void accept(Long id);

    void reject(Long id, String reason);

    void start(Long id);

    void finish(Long id, String result);

    void forceCancel(Long id, String remark);

    void delete(Long id);
}

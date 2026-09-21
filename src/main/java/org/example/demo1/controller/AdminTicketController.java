package org.example.demo1.controller;

import org.example.demo1.pojo.Result;
import org.example.demo1.service.TicketService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AdminTicketController {
    private final TicketService ticketService;

    public AdminTicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/api/admin/tickets")
    public Result page(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) String keyword) {
        return Result.success(ticketService.adminTickets(page, size, status, categoryId, keyword));
    }

    @GetMapping("/api/admin/tickets/{id}")
    public Result detail(@PathVariable Long id) {
        return Result.success(ticketService.adminTicketDetail(id));
    }

    @PostMapping("/api/admin/tickets/{id}/accept")
    public Result accept(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        ticketService.accept(id);
        return Result.success();
    }

    @PostMapping("/api/admin/tickets/{id}/reject")
    public Result reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        ticketService.reject(id, body.get("reason"));
        return Result.success();
    }

    @PostMapping("/api/admin/tickets/{id}/start")
    public Result start(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        ticketService.start(id);
        return Result.success();
    }

    @PostMapping("/api/admin/tickets/{id}/finish")
    public Result finish(@PathVariable Long id, @RequestBody Map<String, String> body) {
        ticketService.finish(id, body.get("result"));
        return Result.success();
    }

    @PostMapping("/api/admin/tickets/{id}/force-cancel")
    public Result forceCancel(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String remark = body == null ? null : body.get("remark");
        ticketService.forceCancel(id, remark);
        return Result.success();
    }

    @DeleteMapping("/api/admin/tickets/{id}")
    public Result delete(@PathVariable Long id) {
        ticketService.delete(id);
        return Result.success();
    }
}

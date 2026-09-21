package org.example.demo1.controller;

import org.example.demo1.pojo.Result;
import org.example.demo1.service.TicketService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/api/tickets")
    public Result create(@RequestBody Map<String, Object> body) {
        return Result.success(ticketService.create(body));
    }

    @GetMapping("/api/tickets/me")
    public Result myTickets(@RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(required = false) String status) {
        return Result.success(ticketService.myTickets(page, size, status));
    }

    @GetMapping("/api/tickets/{id}")
    public Result detail(@PathVariable Long id) {
        return Result.success(ticketService.getMyTicketDetail(id));
    }

    @PostMapping("/api/tickets/{id}/cancel")
    public Result cancel(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        ticketService.cancelMyTicket(id);
        return Result.success();
    }
}

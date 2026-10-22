package com.example.demo.controller;

import com.example.demo.dto.WorkOrderHistoryPage;
import com.example.demo.dto.WorkOrderHistoryQuery;
import com.example.demo.service.WorkOrderHistoryService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/work-order-history")
public class WorkOrderHistoryController {
    private final WorkOrderHistoryService service;
    public WorkOrderHistoryController(WorkOrderHistoryService service) { this.service = service; }

    @GetMapping
    public WorkOrderHistoryPage list(@ModelAttribute WorkOrderHistoryQuery query) { return service.list(query); }

    @GetMapping("/options")
    public Map<String, List<String>> options() { return service.options(); }

    @GetMapping(value = "/export", produces = "text/csv;charset=UTF-8")
    public void export(@ModelAttribute WorkOrderHistoryQuery query, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=work-order-history.csv");
        service.export(query, response.getWriter());
    }
}

package com.example.demo.dto;

import java.util.List;

public record WorkOrderHistoryPage(List<WorkOrderHistoryItem> items, long total, int page, int pageSize) {}

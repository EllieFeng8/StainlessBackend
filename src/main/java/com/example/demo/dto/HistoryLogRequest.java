package com.example.demo.dto;

import java.time.LocalDateTime;

public record HistoryLogRequest(
        String title,
        LocalDateTime time,
        Integer workOrderCode,
        String log) {
}

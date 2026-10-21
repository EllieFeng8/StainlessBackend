package com.example.demo.dto;

import java.time.LocalDateTime;

public record HeadMachineHistoryLogRequest(
        String title,
        LocalDateTime time,
        Integer machineId,
        String log) {
}

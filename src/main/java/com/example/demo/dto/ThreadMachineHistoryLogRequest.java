package com.example.demo.dto;

import java.util.Date;

public record ThreadMachineHistoryLogRequest(
        String title,
        Date time,
        Integer machineId,
        String log,
        @tools.jackson.databind.annotation.JsonDeserialize(using = MachineHistoryStateDeserializer.class)
        Integer state) {
}

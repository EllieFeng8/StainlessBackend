package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;


@Getter
@Setter
public class HeadMachineHistoryLog {
    int id;
    int machineId;
    String title;
    LocalDateTime time;
    String log;
}

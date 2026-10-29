package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
public class ThreadMachineHistoryLog {
    int id;
    int machineId;
    String title;
    Date time;
    String log;
    Integer state;
}

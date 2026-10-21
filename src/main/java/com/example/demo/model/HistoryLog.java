package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class HistoryLog {
    int id;
    String title;
    LocalDateTime time;
    int workOrderCode;
    String log;
}

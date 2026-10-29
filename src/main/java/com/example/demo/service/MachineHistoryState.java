package com.example.demo.service;

import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** 0=斷線, 1=異常紅燈, 2=自動運行綠燈, 3=待機黃燈. */
final class MachineHistoryState {
    private MachineHistoryState() {}
    private static final Set<String> STANDBY = Set.of("換料", "換針", "目標產量設定", "目標產量修改",
            "等待", "待料", "待單", "待沖模", "待維修", "待調車", "無法續做", "模具/牙板異常", "盤元異常換料續做");

    static void validate(Integer state) {
        if (state != null && (state < 0 || state > 3)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "state must be an integer from 0 to 3");
        }
    }

    static Integer forProductionStatus(Object status, Integer fallback) {
        validate(fallback);
        if ("生產中".equals(status)) return 2;
        if (status instanceof String text && STANDBY.contains(text)) return 3;
        return fallback;
    }
}

package com.example.demo.service;

import com.example.demo.dao.WorkOrderHistoryDao;
import com.example.demo.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkOrderHistoryServiceTest {
    @TestFactory Stream<DynamicTest> statusesMatchFrontend() {
        List<DynamicTest> tests = new ArrayList<>();
        for (Integer closed : Arrays.asList(null, 0, 1, 2, 3, 4, 9))
            for (Boolean approved : Arrays.asList(null, false, true))
                for (String production : Arrays.asList(null, "", "正式生產", "等待材料核對", " "))
                    for (boolean started : List.of(false, true)) {
                        String expected = Objects.equals(closed, 2) && Boolean.TRUE.equals(approved) ? "異常結單"
                                : Map.of(1, "正常結單", 2, "異常結單．待主管審核", 3, "異常結單", 4, "異常退回．待補正")
                                .getOrDefault(closed == null ? 0 : closed,
                                        "正式生產".equals(production) ? "生產中" : production != null && !production.isEmpty()
                                                ? production : started ? "生產中" : "等待生產");
                        tests.add(DynamicTest.dynamicTest(closed + "/" + approved + "/" + production + "/" + started,
                                () -> assertEquals(expected, WorkOrderHistoryService.status(closed, approved, production, started))));
                    }
        return tests.stream();
    }
    @Test void queryValidationAndDefaults() {
        var q = new WorkOrderHistoryQuery("  Ab  ", null, null, null, null, null, null, null);
        assertEquals("Ab", q.workOrderId()); assertEquals(1, q.page()); assertEquals(10, q.pageSize());
        assertThrows(ResponseStatusException.class, () -> new WorkOrderHistoryQuery(null,null,null,null,null,null,0,10));
        assertThrows(ResponseStatusException.class, () -> new WorkOrderHistoryQuery(null,null,null,null,null,null,1,201));
        assertThrows(ResponseStatusException.class, () -> new WorkOrderHistoryQuery(null,null,null,null,
                LocalDate.of(2026,2,2),LocalDate.of(2026,2,1),1,10));
    }
    @Test void optionsUseEntireDaoResultAndNaturalOrder() {
        var dao = mock(WorkOrderHistoryDao.class);
        when(dao.options(anyString())).thenReturn(List.of("機台 10", "機台 2", "委外廠商"));
        var options = new WorkOrderHistoryService(dao).options();
        assertTrue(options.get("machineName").contains("委外廠商"));
        assertTrue(options.get("machineName").indexOf("機台 2") < options.get("machineName").indexOf("機台 10"));
        verify(dao, times(4)).options(anyString()); verifyNoMoreInteractions(dao);
    }
}

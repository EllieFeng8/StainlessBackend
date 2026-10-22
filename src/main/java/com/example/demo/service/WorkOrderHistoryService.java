package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.dao.WorkOrderHistoryDao;
import com.example.demo.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.text.Collator;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkOrderHistoryService {
    // Matches the repository's explicit JDBC serverTimezone=Asia/Taipei, not JVM/browser defaults.
    private static final ZoneId ZONE = ZoneId.of("Asia/Taipei");
    private final WorkOrderHistoryDao dao;
    @Autowired
    public WorkOrderHistoryService(WorkOrderHistoryDao dao) { this.dao = dao; }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public WorkOrderHistoryPage list(WorkOrderHistoryQuery query) {
        long total = dao.count(query);
        return new WorkOrderHistoryPage(dao.page(query).stream().map(this::map).toList(),
                total, query.page(), query.pageSize());
    }
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public Map<String, List<String>> options() {
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (String field : List.of("workOrderId", "machineName", "heatNumber", "productCode")) {
            result.put(field, dao.options(field).stream().sorted(naturalOrder()).toList());
        }
        return result;
    }
    public WorkOrderHistoryItem map(WorkOrderHistoryRow row) {
        String key = row.stage() + "-" + row.id();
        String time = row.timestamp() == null ? null : row.timestamp().atZone(ZONE)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        boolean outsource = "outsourcing".equals(row.stage());
        String detail = outsource ? text(row.changeReason()) : Arrays.stream(new String[]{row.reason(), row.remark(), row.opinion()})
                .filter(s -> s != null && !s.isEmpty()).collect(Collectors.joining("；"));
        return new WorkOrderHistoryItem(key, key, row.stage(), text(row.workOrderId()), text(row.machineName()),
                text(row.heatNumber()), text(row.productCode()), time, time, row.canNavigate(),
                outsource ? text(row.state()) : status(row.orderClosed(), row.approved(), row.productionStatus(), row.startedAt() != null),
                detail, row.targetQuantity(), row.producedQuantity(), outsource ? null : row.oee(), null);
    }
    public static String status(Integer closed, Boolean approved, String production, boolean started) {
        if (Objects.equals(closed, 2) && Boolean.TRUE.equals(approved)) return "異常結單";
        if (closed != null) {
            switch (closed) {
                case 1: return "正常結單";
                case 2: return "異常結單．待主管審核";
                case 3: return "異常結單";
                case 4: return "異常退回．待補正";
                default: break;
            }
        }
        if ("正式生產".equals(production)) return "生產中";
        if (production != null && !production.isEmpty()) return production;
        return started ? "生產中" : "等待生產";
    }
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public void export(WorkOrderHistoryQuery query, Writer writer) throws IOException {
        writer.write('\ufeff');
        writeCsv(writer, List.of("工單", "爐號", "料號", "日期時間", "製程", "機台／委外廠商", "狀態", "目標產量", "已生產", "OEE", "故障率", "內容"));
        try {
            dao.export(query, raw -> {
                WorkOrderHistoryItem item = map(raw);
                try {
                    writeCsv(writer, Arrays.asList(item.workOrderId(), item.heatNumber(), item.productCode(), item.timestamp(),
                            switch (item.stage()) { case "head" -> "打頭"; case "thread" -> "搓牙"; default -> "委外"; },
                            item.machineName(), item.currentStatus(), item.targetQuantity(), item.producedQuantity(),
                            item.oee(), item.failureRate(), item.detail()));
                } catch (IOException ex) { throw new UncheckedIOException(ex); }
            });
        } catch (UncheckedIOException ex) { throw ex.getCause(); }
        writer.flush();
    }
    private static void writeCsv(Writer writer, List<?> cells) throws IOException {
        List<String> escaped = new ArrayList<>();
        for (Object cell : cells) {
            String value = cell == null ? "" : cell.toString();
            // Prevent spreadsheet formula execution in user-entered text; numbers stay numeric.
            if (cell instanceof String && value.stripLeading().matches("(?s)^[=+@-].*")) value = "'" + value;
            escaped.add("\"" + value.replace("\"", "\"\"") + "\"");
        }
        writer.write(String.join(",", escaped) + "\r\n");
    }
    private static String text(String value) { return value == null ? "" : value; }
    static Comparator<String> naturalOrder() {
        Collator collator = Collator.getInstance(Locale.TAIWAN);
        return (a, b) -> {
            int i = 0, j = 0;
            while (i < a.length() && j < b.length()) {
                int ai = i, bj = j;
                boolean an = a.charAt(i) >= '0' && a.charAt(i) <= '9';
                boolean bn = b.charAt(j) >= '0' && b.charAt(j) <= '9';
                while (i < a.length() && (a.charAt(i) >= '0' && a.charAt(i) <= '9') == an) i++;
                while (j < b.length() && (b.charAt(j) >= '0' && b.charAt(j) <= '9') == bn) j++;
                String ac = a.substring(ai, i), bc = b.substring(bj, j);
                int compared = an && bn ? new java.math.BigInteger(ac).compareTo(new java.math.BigInteger(bc)) : collator.compare(ac, bc);
                if (compared != 0) return compared;
            }
            int remainder = Integer.compare(a.length() - i, b.length() - j);
            return remainder != 0 ? remainder : a.compareTo(b);
        };
    }
}

package com.example.demo.dao;

import com.example.demo.dto.WorkOrderHistoryQuery;
import com.example.demo.dto.WorkOrderHistoryRow;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class WorkOrderHistoryDao {
    private final NamedParameterJdbcTemplate jdbc;
    public WorkOrderHistoryDao(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    // Identifiers below are constants, never request input. LEFT JOIN retains orphan assignments.
    private static String assignment(String stage, String table, String machines, String name) {
        return "SELECT a.id, '" + stage + "' AS stage, "
                + "a.work_order_code AS orderRef, "
                + "CASE WHEN OCTET_LENGTH(m." + name + ") > 0 THEN m." + name
                + " ELSE COALESCE(CONCAT('機台 ', a.machineId), '—') END AS machineName, "
                + "COALESCE(a.end_at, a.started_at) AS eventTime, "
                + "a.started_at AS startedAt, a.order_closed AS orderClosed, a.approved, "
                + "a.production_status AS productionStatus, NULL AS state, a.reason, a.remark, a.opinion, "
                + "NULL AS changeReason, a.target_quantity AS targetQuantity, a.current_quantity AS producedQuantity, a.oee "
                + "FROM mydb." + table + " a "
                + "LEFT JOIN mydb." + machines + " m ON m.id = a.machineId";
    }
    private static final String UNION = assignment("head", "work_order_heading_machine", "head_machine", "head_machine_name")
            + " UNION ALL " + assignment("thread", "work_order_threading_machine", "thread_machine", "thread_rolling_machine_name")
            + " UNION ALL " + """
            SELECT a.id, 'outsourcing' AS stage,
              a.work_order_id AS orderRef,
              COALESCE(a.outsource_position, '') AS machineName,
              COALESCE(a.end_at, a.started_at) AS eventTime,
              a.started_at AS startedAt, NULL AS orderClosed, NULL AS approved, NULL AS productionStatus,
              a.state, NULL AS reason, NULL AS remark, NULL AS opinion, a.change_reason AS changeReason,
              a.outsource_quantity AS targetQuantity, a.current_quantity AS producedQuantity, NULL AS oee
            FROM mydb.outsource_form a
            """;
    private static final String FROM = " FROM (SELECT r.*, "
            + "COALESCE(CAST(w.work_code AS CHAR), CAST(r.orderRef AS CHAR), '') AS workOrderId, "
            + "COALESCE(w.furnaceNo, '') AS heatNumber, COALESCE(w.model, '') AS productCode, "
            + "(w.id IS NOT NULL) AS canNavigate FROM (" + UNION
            + ") r LEFT JOIN mydb.work_order w ON w.id = r.orderRef) h ";
    // work_code and both fallback IDs are integer fields in this schema, so numeric ordering
    // exactly implements natural ordering over the supported domain (including 2 before 10).
    private static final String ORDER = " ORDER BY CASE WHEN workOrderId = '' THEN 0 ELSE 1 END, "
            + "CAST(NULLIF(workOrderId, '') AS DECIMAL(65,0)), workOrderId, "
            + "CASE WHEN eventTime IS NULL THEN 1 ELSE 0 END, eventTime DESC, stage, id";

    private record Filter(String sql, MapSqlParameterSource params) {}
    private Filter filter(WorkOrderHistoryQuery q) {
        StringBuilder sql = new StringBuilder(" WHERE 1=1");
        MapSqlParameterSource params = new MapSqlParameterSource();
        Map<String, String> texts = Map.of("workOrderId", q.workOrderId(), "machineName", q.machineName(),
                "heatNumber", q.heatNumber(), "productCode", q.productCode());
        texts.forEach((field, value) -> {
            if (!value.isEmpty()) {
                // LOCATE treats '%' and '_' literally, matching JS includes rather than SQL LIKE.
                sql.append(" AND LOCATE(CAST(LOWER(:").append(field)
                        .append(") AS BINARY), CAST(LOWER(").append(field).append(") AS BINARY)) > 0");
                params.addValue(field, value);
            }
        });
        if (q.startDate() != null) {
            sql.append(" AND eventTime >= :startTime");
            params.addValue("startTime", q.startDate() + " 00:00:00");
        }
        if (q.endDate() != null) {
            sql.append(" AND eventTime < :endTime");
            params.addValue("endTime", q.endDate().plusDays(1) + " 00:00:00");
        }
        return new Filter(sql.toString(), params);
    }
    public long count(WorkOrderHistoryQuery q) {
        Filter f = filter(q);
        Long count = jdbc.queryForObject("SELECT COUNT(*)" + FROM + f.sql(), f.params(), Long.class);
        return count == null ? 0 : count;
    }
    public List<WorkOrderHistoryRow> page(WorkOrderHistoryQuery q) {
        Filter f = filter(q);
        f.params().addValue("limit", q.pageSize()).addValue("offset", (long) (q.page() - 1) * q.pageSize());
        return jdbc.query("SELECT *" + FROM + f.sql() + ORDER + " LIMIT :limit OFFSET :offset", f.params(), MAPPER);
    }
    /** Stream using the MySQL driver's forward-only mode; no all-results Java list. */
    public void export(WorkOrderHistoryQuery q, java.util.function.Consumer<WorkOrderHistoryRow> consumer) {
        Filter f = filter(q);
        jdbc.execute("SELECT *" + FROM + f.sql() + ORDER, f.params(), ps -> {
            ps.setFetchSize(Integer.MIN_VALUE);
            try (var rs = ps.executeQuery()) {
                int row = 0;
                while (rs.next()) consumer.accept(MAPPER.mapRow(rs, row++));
            }
            return null;
        });
    }
    public List<String> options(String field) {
        if (!List.of("workOrderId", "machineName", "heatNumber", "productCode").contains(field))
            throw new IllegalArgumentException("Unknown history option");
        return jdbc.queryForList("SELECT DISTINCT CAST(" + field + " AS BINARY)" + FROM
                        + " WHERE OCTET_LENGTH(" + field + ") > 0",
                Map.of(), String.class);
    }
    private static final RowMapper<WorkOrderHistoryRow> MAPPER = (r, n) -> new WorkOrderHistoryRow(
            r.getLong("id"), r.getString("stage"), r.getString("workOrderId"), r.getString("machineName"),
            r.getString("heatNumber"), r.getString("productCode"), r.getBoolean("canNavigate"),
            r.getObject("eventTime", LocalDateTime.class), r.getObject("startedAt", LocalDateTime.class),
            r.getObject("orderClosed", Integer.class), r.getObject("approved", Boolean.class),
            r.getString("productionStatus"), r.getString("state"), r.getString("reason"), r.getString("remark"),
            r.getString("opinion"), r.getString("changeReason"), r.getBigDecimal("targetQuantity"),
            r.getBigDecimal("producedQuantity"), r.getBigDecimal("oee"));
}

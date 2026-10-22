package com.example.demo.dao;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.*;

/** Raw database rows are private to the integration layer, never returned by the controller. */
@Repository
public class WorkOrderDetailDao {
    private final NamedParameterJdbcTemplate jdbc;
    @Autowired
    public WorkOrderDetailDao(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Map<String, Object>> order(String code) {
        return jdbc.queryForList("SELECT * FROM mydb.work_order WHERE CAST(work_code AS CHAR) = :code", Map.of("code", code));
    }
    public List<Map<String, Object>> assignments(String stage, long id) {
        return jdbc.queryForList("SELECT * FROM mydb." + table(stage) + " WHERE "
                + (stage.equals("outsourcing") ? "work_order_id" : "work_order_code") + "=:id ORDER BY id", Map.of("id", id));
    }
    public List<Map<String, Object>> machines(String stage) {
        if (!List.of("head", "thread").contains(stage)) throw new IllegalArgumentException("stage");
        return jdbc.queryForList("SELECT * FROM mydb." + stage + "_machine ORDER BY id", Map.of());
    }
    public List<Map<String, Object>> materials() {
        return jdbc.queryForList("SELECT * FROM mydb.material_history_form ORDER BY material_id", Map.of());
    }
    public List<Map<String, Object>> firstArticles(long id) {
        return jdbc.queryForList("SELECT * FROM mydb.first_test_form WHERE work_order_code=:id ORDER BY id", Map.of("id", id));
    }
    public List<Map<String, Object>> states(long id) {
        return jdbc.queryForList("SELECT * FROM mydb.work_order_first_article_state WHERE work_order_id=:id", Map.of("id", id));
    }
    public List<Map<String, Object>> events(long id) {
        return jdbc.queryForList("SELECT * FROM mydb.history_log WHERE work_order_code=:id ORDER BY `time` DESC, id DESC", Map.of("id", id));
    }
    public static String table(String stage) {
        return switch (stage) {
            case "head" -> "work_order_heading_machine";
            case "thread" -> "work_order_threading_machine";
            case "outsourcing" -> "outsource_form";
            default -> throw new IllegalArgumentException("Unknown stage");
        };
    }
}

package com.example.demo.rowMapper;

import com.example.demo.model.FirstTestForm;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class FirstTestFormRowMapper implements RowMapper<FirstTestForm> {
    private static final String[] HEAD_COLUMNS = {
            "head_machineId", "heading_length", "head_dia", "head_angle_s1", "head_angle_s2",
            "punch_type_no", "punch_width", "punch_gauge_depth", "head_thickness_h1",
            "head_thickness_h2", "enlarged_shank_dia", "enlarged_length", "head_material_code"
    };
    private static final String[] THREAD_COLUMNS = {
            "thread_machineId", "thread_type", "thread_pitch", "major_diameter_d1",
            "major_diameter_d2", "thread_angle", "thread_length", "oblique_tooth_length",
            "thread_tail_angle", "minor_diameter", "oblique_tooth_diameter", "overall_length", "thread_material_code"
    };

    public static RowMapper<Map<String, Object>> forMachine(boolean includeHead, boolean includeThread) {
        if (!includeHead && !includeThread) {
            throw new IllegalArgumentException("At least one machine type is required");
        }
        return (rs, rowNum) -> {
            Map<String, Object> form = new LinkedHashMap<>();
            form.put("id", rs.getObject("id"));
            form.put("state", rs.getBoolean("state"));
            form.put("dateTime", rs.getObject("dateTime", java.time.LocalDateTime.class));
            form.put("work_order_code", rs.getObject("work_order_code"));
            form.put("record_by", rs.getString("record_by"));
            if (includeHead) {
                addColumns(rs, form, HEAD_COLUMNS);
            }
            if (includeThread) {
                addColumns(rs, form, THREAD_COLUMNS);
            }
            return form;
        };
    }

    private static void addColumns(ResultSet rs, Map<String, Object> form, String[] columns) throws SQLException {
        for (String column : columns) {
            form.put(column, column.endsWith("material_code") ? rs.getObject(column, Integer.class)
                    : column.endsWith("machineId") ? rs.getObject(column) : rs.getString(column));
        }
    }

    @Override
    public FirstTestForm mapRow(ResultSet rs, int rowNum) throws SQLException {
        FirstTestForm firstTestForm = new FirstTestForm();
        firstTestForm.setDateTime(rs.getObject("dateTime", java.time.LocalDateTime.class));
        firstTestForm.setId(rs.getInt("id"));
        firstTestForm.setState(rs.getBoolean("state"));
        firstTestForm.setWorkOrderCode(rs.getInt("work_order_code"));
        firstTestForm.setHeadMachineId(rs.getInt("head_machineId"));
        firstTestForm.setThreadMachineId(rs.getInt("thread_machineId"));
        firstTestForm.setHeadLength(rs.getString("heading_length"));
        firstTestForm.setHeadDia(rs.getString("head_dia"));
        firstTestForm.setHeadAngelS1(rs.getString("head_angle_s1"));
        firstTestForm.setHeadAngelS2(rs.getString("head_angle_s2"));
        firstTestForm.setPunchType(rs.getString("punch_type_no"));
        firstTestForm.setPunchWidth(rs.getString("punch_width"));
        firstTestForm.setPunchGaugeDepth(rs.getString("punch_gauge_depth"));
        firstTestForm.setHeadThicknessH1(rs.getString("head_thickness_h1"));
        firstTestForm.setHeadThicknessH2(rs.getString("head_thickness_h2"));
        firstTestForm.setEnlargedShankDia(rs.getString("enlarged_shank_dia"));
        firstTestForm.setEnlargedLength(rs.getString("enlarged_length"));
        firstTestForm.setThreadType(rs.getString("thread_type"));
        firstTestForm.setThreadPitch(rs.getString("thread_pitch"));
        firstTestForm.setMajorDiameterD1(rs.getString("major_diameter_d1"));
        firstTestForm.setMajorDiameterD2(rs.getString("major_diameter_d2"));
        firstTestForm.setThreadAngle(rs.getString("thread_angle"));
        firstTestForm.setThreadLength(rs.getString("thread_length"));
        firstTestForm.setObliqueToothLength(rs.getString("oblique_tooth_length"));
        firstTestForm.setThreadTailAngle(rs.getString("thread_tail_angle"));
        firstTestForm.setMinorDiameter(rs.getString("minor_diameter"));
        firstTestForm.setObliqueToothDiameter(rs.getString("oblique_tooth_diameter"));
        firstTestForm.setOverallLength(rs.getString("overall_length"));
        firstTestForm.setHeadMaterialId(rs.getObject("head_material_code", Integer.class));
        firstTestForm.setThreadMaterialId(rs.getObject("thread_material_code", Integer.class));
        firstTestForm.setRecordBy(rs.getString("record_by"));
        return firstTestForm;
    }
}

package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedHashMap;
import java.util.Map;

public record FirstTestFormRequest(
        @JsonProperty("work_order_code") Integer workOrderCode,
        @JsonProperty("head_machineId") @JsonAlias("headMachineId") Integer headMachineId,
        @JsonProperty("thread_machineId") @JsonAlias("threadMachineId") Integer threadMachineId,
        @JsonProperty("heading_length") String headLength,
        @JsonProperty("head_dia") String headDia,
        @JsonProperty("head_angle_s1") String headAngelS1,
        @JsonProperty("head_angle_s2") String headAngelS2,
        @JsonProperty("punch_type_no") String punchType,
        @JsonProperty("punch_width") String punchWidth,
        @JsonProperty("punch_gauge_depth") String punchGaugeDepth,
        @JsonProperty("head_thickness_h1") String headThicknessH1,
        @JsonProperty("head_thickness_h2") String headThicknessH2,
        @JsonProperty("enlarged_shank_dia") String enlargedShankDia,
        @JsonProperty("enlarged_length") String enlargedLength,
        @JsonProperty("thread_type") String threadType,
        @JsonProperty("thread_pitch") String threadPitch,
        @JsonProperty("major_diameter_d1") String majorDiameterD1,
        @JsonProperty("major_diameter_d2") String majorDiameterD2,
        @JsonProperty("thread_angle") String threadAngle,
        @JsonProperty("thread_length") String threadLength,
        @JsonProperty("oblique_tooth_length") String obliqueToothLength,
        @JsonProperty("thread_tail_angle") String threadTailAngle,
        @JsonProperty("minor_diameter") String minorDiameter,
        @JsonProperty("oblique_tooth_diameter") String obliqueToothDiameter,
        @JsonProperty("overall_length") String overallLength,
        @JsonProperty("head_material_code") @JsonAlias("head_material_id") Integer headMaterialId,
        @JsonProperty("thread_material_code") @JsonAlias("thread_material_id") Integer threadMaterialId,
        @JsonProperty("state") Boolean state,
        @JsonProperty("record_by") @JsonAlias("recordBy") String recordBy
) {
    // PATCH updates measurements, state and recorder; work order, machine and material IDs are ignored.
    public Map<String, Object> toUpdateParameters() {
        Map<String, Object> params = new LinkedHashMap<>();
        if (state != null) params.put("state", state);
        if (recordBy != null) params.put("record_by", recordBy);
        if (headLength != null) params.put("heading_length", headLength);
        if (headDia != null) params.put("head_dia", headDia);
        if (headAngelS1 != null) params.put("head_angle_s1", headAngelS1);
        if (headAngelS2 != null) params.put("head_angle_s2", headAngelS2);
        if (punchType != null) params.put("punch_type_no", punchType);
        if (punchWidth != null) params.put("punch_width", punchWidth);
        if (punchGaugeDepth != null) params.put("punch_gauge_depth", punchGaugeDepth);
        if (headThicknessH1 != null) params.put("head_thickness_h1", headThicknessH1);
        if (headThicknessH2 != null) params.put("head_thickness_h2", headThicknessH2);
        if (enlargedShankDia != null) params.put("enlarged_shank_dia", enlargedShankDia);
        if (enlargedLength != null) params.put("enlarged_length", enlargedLength);
        if (threadType != null) params.put("thread_type", threadType);
        if (threadPitch != null) params.put("thread_pitch", threadPitch);
        if (majorDiameterD1 != null) params.put("major_diameter_d1", majorDiameterD1);
        if (majorDiameterD2 != null) params.put("major_diameter_d2", majorDiameterD2);
        if (threadAngle != null) params.put("thread_angle", threadAngle);
        if (threadLength != null) params.put("thread_length", threadLength);
        if (obliqueToothLength != null) params.put("oblique_tooth_length", obliqueToothLength);
        if (threadTailAngle != null) params.put("thread_tail_angle", threadTailAngle);
        if (minorDiameter != null) params.put("minor_diameter", minorDiameter);
        if (obliqueToothDiameter != null) params.put("oblique_tooth_diameter", obliqueToothDiameter);
        if (overallLength != null) params.put("overall_length", overallLength);
        return params;
    }

    public Map<String, Object> toInsertParameters() {
        Map<String, Object> params = new LinkedHashMap<>();
        // Omission uses the database default on insert and preserves state on upsert.
        if (state != null) params.put("state", state);
        if (recordBy != null) params.put("record_by", recordBy);
        if (workOrderCode != null) params.put("work_order_code", workOrderCode);
        if (headMachineId != null) {
            params.put("head_machineId", headMachineId);
            if (headMaterialId != null) params.put("head_material_code", headMaterialId);
            if (headLength != null) params.put("heading_length", headLength);
            if (headDia != null) params.put("head_dia", headDia);
            if (headAngelS1 != null) params.put("head_angle_s1", headAngelS1);
            if (headAngelS2 != null) params.put("head_angle_s2", headAngelS2);
            if (punchType != null) params.put("punch_type_no", punchType);
            if (punchWidth != null) params.put("punch_width", punchWidth);
            if (punchGaugeDepth != null) params.put("punch_gauge_depth", punchGaugeDepth);
            if (headThicknessH1 != null) params.put("head_thickness_h1", headThicknessH1);
            if (headThicknessH2 != null) params.put("head_thickness_h2", headThicknessH2);
            if (enlargedShankDia != null) params.put("enlarged_shank_dia", enlargedShankDia);
            if (enlargedLength != null) params.put("enlarged_length", enlargedLength);
        }
        if (threadMachineId != null) {
            params.put("thread_machineId", threadMachineId);
            if (threadMaterialId != null) params.put("thread_material_code", threadMaterialId);
            if (threadType != null) params.put("thread_type", threadType);
            if (threadPitch != null) params.put("thread_pitch", threadPitch);
            if (majorDiameterD1 != null) params.put("major_diameter_d1", majorDiameterD1);
            if (majorDiameterD2 != null) params.put("major_diameter_d2", majorDiameterD2);
            if (threadAngle != null) params.put("thread_angle", threadAngle);
            if (threadLength != null) params.put("thread_length", threadLength);
            if (obliqueToothLength != null) params.put("oblique_tooth_length", obliqueToothLength);
            if (threadTailAngle != null) params.put("thread_tail_angle", threadTailAngle);
            if (minorDiameter != null) params.put("minor_diameter", minorDiameter);
            if (obliqueToothDiameter != null) params.put("oblique_tooth_diameter", obliqueToothDiameter);
            if (overallLength != null) params.put("overall_length", overallLength);
        }
        return params;
    }
}


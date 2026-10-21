package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
public class FirstTestForm {
    @JsonInclude(JsonInclude.Include.ALWAYS)
    java.time.LocalDateTime dateTime;
    int id;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    boolean state;
    int workOrderCode;
    int headMachineId;
    int threadMachineId;
    String headLength;
    String headDia;
    String headAngelS1;
    String headAngelS2;
    String punchType;
    String punchWidth;
    String punchGaugeDepth;
    String headThicknessH1;
    String headThicknessH2;
    String enlargedShankDia;
    String enlargedLength;
    String threadType;
    String threadPitch;
    String majorDiameterD1;
    String majorDiameterD2;
    String threadAngle;
    String threadLength;
    String obliqueToothLength;
    String threadTailAngle;
    String minorDiameter;
    String obliqueToothDiameter;
    String overallLength;
    @JsonProperty("head_material_code")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    Integer headMaterialId;
    @JsonProperty("thread_material_code")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    Integer threadMaterialId;
    @JsonProperty("record_by")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    String recordBy;

}

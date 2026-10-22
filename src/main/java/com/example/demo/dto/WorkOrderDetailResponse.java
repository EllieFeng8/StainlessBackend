package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record WorkOrderDetailResponse(Order order, Status summary, List<ProcessGroup> processGroups,
        Map<String, List<Choice>> machineChoices, List<Material> materialChoices,
        List<Event> events, List<DataIssue> dataIssues, String timeZone) {
    public record Order(long databaseId, String workOrderCode, BigDecimal quantity, String deadlineDate,
            String dueDateDisplay, String productName, String productCode, String process, Details details) {}
    public record Details(String customerName, String specification, String materialGrade, BigDecimal wireDiameter,
            BigDecimal wireWeightKg, String wireDrawingFactory, String heatNumber, String remark,
            String wireLength, String surfaceTreatment, String inspectionStandard) {}
    public record Status(String statusCode, String statusLabel, String statusTone) {}
    public record ProcessGroup(String stage, String name, List<Assignment> rows) {}
    public record Assignment(String rowId, long assignmentId, String stage, Long machineId, Long materialId,
            String name, String nameDisplay, BigDecimal targetQuantity, BigDecimal producedQuantity,
            String startedAt, String endedAt, String rawStatus, String statusCode, String statusLabel,
            String statusTone, Material material, FirstArticle firstArticle, Tolerance tolerance,
            Map<String, Action> actions) {}
    public record Material(Long id, String name, String specification, String barcode,
            boolean available, String unavailableReason) {}
    public record Choice(long id, String name) {}
    public record Tolerance(BigDecimal lowerOffsetQuantity, BigDecimal upperOffsetQuantity,
            BigDecimal targetOffsetLowerPercent, BigDecimal targetOffsetUpperPercent,
            BigDecimal minimumQuantity, BigDecimal maximumQuantity, String acceptableRangeDisplay,
            boolean valid, String reasonCode) {}
    public record FirstArticle(boolean firstArticleExists, boolean firstArticleCompleted,
            boolean requiresFirstArticle, String firstArticleCompletedAt, String firstArticleCompletedAtDisplay,
            List<String> missingInspectionFields, boolean hasFirstArticleClearance,
            Long recordId, String exemptionConfirmedAt, String exemptionReason, String additionalProductionAt,
            boolean ambiguous) {}
    public record Action(boolean visible, boolean enabled, String label, String reasonCode, String reasonMessage,
            boolean confirmationRequired, String confirmationMessage, String targetType, String workOrderCode,
            long workOrderDatabaseId, String stage, long assignmentId, Long machineId, Long firstArticleId) {}
    public record Event(long id, String type, String detail, String occurredAt, String occurredAtDisplay) {}
    public record DataIssue(String code, String rowId, String message) {}
}

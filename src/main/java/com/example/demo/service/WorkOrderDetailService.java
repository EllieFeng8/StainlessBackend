package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.dao.WorkOrderDetailDao;
import com.example.demo.dto.WorkOrderDetailRequest;
import com.example.demo.dto.WorkOrderDetailResponse;
import com.example.demo.dto.WorkOrderDetailResponse.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;
import java.math.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

@Service
public class WorkOrderDetailService {
    public static final ZoneId ZONE = ZoneId.of("Asia/Taipei");
    public static final String EXEMPTION_MESSAGE = "一般製程須先完成首件檢測。僅『其他製程／委外／盤元異常補領』可免首件檢測；請確認本次符合其中一項。免首檢仍須核對生產物料。";
    public static final List<String> HEAD_FIELDS = List.of("heading_length", "head_dia", "head_angle_s1", "head_angle_s2",
            "punch_type_no", "punch_width", "punch_gauge_depth", "head_thickness_h1", "head_thickness_h2", "enlarged_shank_dia", "enlarged_length");
    // Confirmed inspection field: major_diameter_d2 (D2), consistent with schema and frontend.
    public static final List<String> THREAD_FIELDS = List.of("thread_type", "thread_pitch", "major_diameter_d1",
            "oblique_tooth_length", "thread_angle", "thread_length", "oblique_tooth_diameter", "thread_tail_angle",
            "minor_diameter", "major_diameter_d2", "overall_length");
    private static final BigDecimal SAFE = new BigDecimal("9007199254740991");
    private final WorkOrderDetailDao dao;
    private final ObjectMapper json = new ObjectMapper();
    @Autowired
    public WorkOrderDetailService(WorkOrderDetailDao dao) { this.dao = dao; }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public WorkOrderDetailResponse detail(WorkOrderDetailRequest request) {
        var orders = dao.order(request.workOrderCode());
        if (orders.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "工單不存在");
        if (orders.size() != 1) throw new ResponseStatusException(HttpStatus.CONFLICT, "工單編號不唯一");
        var w = orders.getFirst();
        long id = id(w, "id");
        var issues = new ArrayList<DataIssue>();
        BigDecimal quantity = zero(decimal(w, "total_quantity"));
        String deadline = w.get("deadline_date") == null ? null : date(w.get("deadline_date"));
        var order = new Order(id, text(w, "work_code"), quantity, deadline,
                deadline == null ? "—" : LocalDate.parse(deadline).format(DateTimeFormatter.ofPattern("yyyy/M/d")),
                text(w, "product_name"), text(w, "model"), text(w, "flow"),
                new Details(text(w,"client_name"), text(w,"model"), text(w,"material"), decimal(w,"wire_dia"),
                        decimal(w,"kilogram"), text(w,"factory"), text(w,"furnaceNo"), text(w,"note"), "", "", ""));
        var machines = Map.of("head", index(dao.machines("head"), "id"), "thread", index(dao.machines("thread"), "id"));
        var materials = index(dao.materials(), "material_id");
        var firsts = dao.firstArticles(id);
        var states = dao.states(id);
        var groups = new ArrayList<ProcessGroup>();
        var statuses = new ArrayList<Status>();
        for (String stage : List.of("head", "outsourcing", "thread")) {
            var rows = new ArrayList<Assignment>();
            for (var row : dao.assignments(stage, id)) {
                long assignmentId = id(row,"id");
                String rowId = stage + "-" + assignmentId;
                boolean outsource = stage.equals("outsourcing");
                Long machineId = outsource ? null : nullableId(row,"machineId");
                Long materialId = outsource ? null : nullableId(row,"material_id");
                String name = outsource ? text(row,"outsource_position") : machineName(machines.get(stage).get(machineId), machineId, true);
                if (!outsource && !machines.get(stage).containsKey(machineId)) issues.add(new DataIssue("MACHINE_NOT_FOUND",rowId,"派工機台不存在"));
                BigDecimal target = decimal(row, outsource ? "outsource_quantity" : "target_quantity");
                if (outsource && target == null) target = quantity;
                BigDecimal produced = decimal(row,"current_quantity");
                if (outsource) produced = zero(produced);
                if (target == null || produced == null) issues.add(new DataIssue("QUANTITY_MISSING",rowId,"缺漏產量保留 null；缺目標時不計算允收範圍"));
                String rawStatus = text(row, outsource ? "state" : "production_status");
                if (outsource && rawStatus.isEmpty()) rawStatus = "有委外";
                Status status = outsource ? status(switch (rawStatus) {
                    case "完成" -> "完成"; case "開始", "生產中" -> "生產中"; default -> "尚未開始";
                }) : assignmentStatus(row);
                if (!outsource) statuses.add(status);
                Material material = outsource ? null : material(materialId, materials.get(materialId), order.productCode());
                if (material != null && !material.available()) issues.add(new DataIssue("MATERIAL_NOT_FOUND",rowId,material.unavailableReason()));
                FirstArticle first = outsource ? null : firstArticle(stage, machineId, firsts, states);
                if (first != null && first.ambiguous()) issues.add(new DataIssue("FIRST_ARTICLE_AMBIGUOUS",rowId,"同工單／製程／機台有多筆首件，未選取任何一筆；請合併"));
                if (first != null && first.requiresFirstArticle() && states.stream().noneMatch(s -> stage.equals(text(s,"stage")) && Objects.equals(machineId,nullableId(s,"machine_id"))))
                    issues.add(new DataIssue("FIRST_ARTICLE_STATE_UNMIGRATED",rowId,"首件無權威狀態，保守要求重新完成檢驗"));
                Tolerance tolerance = tolerance(target, decimal(row,outsource ? "outsource_down_offset_quantity" : "target_down_offset_quantity"),
                        decimal(row,outsource ? "outsource_up_offset_quantity" : "target_up_offset_quantity"));
                if (!tolerance.valid()) issues.add(new DataIssue(tolerance.reasonCode(),rowId,"產量允收範圍無法計算"));
                rows.add(new Assignment(rowId, assignmentId, stage, machineId, materialId, name,
                        outsource && name.isEmpty() ? "未填寫公司" : name, target, produced,
                        time(row.get("started_at")), time(row.get("end_at")), rawStatus,
                        status.statusCode(),status.statusLabel(),status.statusTone(),material,first,tolerance,
                        outsource ? Map.of() : actions(order,stage,assignmentId,machineId,status,first)));
            }
            groups.add(new ProcessGroup(stage, switch(stage) {case "head" -> "打頭機台"; case "thread" -> "搓牙機台"; default -> "委外";}, rows));
        }
        var choices = new LinkedHashMap<String,List<Choice>>();
        machines.forEach((stage, values) -> choices.put(stage, values.values().stream()
                .map(m -> new Choice(id(m,"id"), machineName(m,id(m,"id"),true))).toList()));
        // Existing application has no authenticated principal / material ACL. Preserve its catalog scope explicitly.
        issues.add(new DataIssue("MATERIAL_AUTHORIZATION_NOT_CONFIGURED",null,"沿用現有全物料可見範圍；專案未提供使用者／物料權限模型"));
        return new WorkOrderDetailResponse(order, summary(statuses), groups, choices,
                materials.values().stream().map(m -> material(id(m,"material_id"),m,order.productCode())).toList(),
                dao.events(id).stream().map(e -> event(e,machines)).toList(),issues,ZONE.getId());
    }

    public static Status assignmentStatus(Map<String,Object> r) {
        int closed = r.get("order_closed") instanceof Number n ? n.intValue() : 0;
        String label;
        if (closed == 2 && bool(r.get("approved"))) label = "異常結單";
        else label = switch(closed) {
            case 1 -> "正常結單"; case 2 -> "異常結單．待主管審核"; case 3 -> "異常結單"; case 4 -> "異常退回．待補正";
            default -> text(r,"production_status").isEmpty() ? (r.get("started_at") == null ? "等待生產" : "生產中") : text(r,"production_status");
        };
        return status(switch(label) {case "正式生產" -> "生產中"; case "等待材料核對" -> "等待生產"; case "待主管審核" -> "異常結單．待主管審核"; default -> label;});
    }
    public static Status status(String label) {
        String code = switch(label) {
            case "等待生產" -> "READY"; case "生產中" -> "RUNNING"; case "正常結單" -> "CLOSED";
            case "異常結單" -> "CLOSED_ABNORMAL"; case "異常結單．待主管審核" -> "PENDING_REVIEW";
            case "異常退回．待補正" -> "CORRECTION_REQUIRED"; case "完成" -> "OUTSOURCE_COMPLETED";
            case "尚未開始" -> "OUTSOURCE_NOT_STARTED"; case "無法續做" -> "CANNOT_CONTINUE";
            default -> "CUSTOM";
        };
        String tone = switch(label) {
            case "等待生產" -> "ready"; case "生產中" -> "running"; case "正常結單" -> "closed";
            case "異常結單" -> "closed-error"; default -> label.contains("異常") || label.equals("無法續做") ? "error" : "waiting";
        };
        return new Status(code,label,tone);
    }
    public static Status summary(List<Status> rows) {
        if (!rows.isEmpty() && rows.stream().allMatch(s -> List.of("CLOSED","CLOSED_ABNORMAL").contains(s.statusCode())))
            return status(rows.stream().anyMatch(s -> s.statusCode().equals("CLOSED_ABNORMAL")) ? "異常結單" : "正常結單");
        return status(rows.stream().anyMatch(s -> !s.statusLabel().isEmpty() && !s.statusLabel().equals("等待生產")) ? "生產中" : "等待生產");
    }
    public static Tolerance tolerance(BigDecimal target, BigDecimal down, BigDecimal up) {
        BigDecimal lower = zero(down), upper = zero(up);
        if (target == null) return invalid(down,up,"TARGET_MISSING");
        if (target.signum() < 0 || target.abs().compareTo(SAFE) > 0 || lower.abs().compareTo(SAFE)>0 || upper.abs().compareTo(SAFE)>0)
            return invalid(down,up,"QUANTITY_OUT_OF_RANGE");
        BigDecimal lp = target.signum() == 0 ? BigDecimal.ZERO : lower.multiply(BigDecimal.valueOf(100)).divide(target,16,RoundingMode.HALF_UP);
        BigDecimal hp = target.signum() == 0 ? BigDecimal.ZERO : upper.multiply(BigDecimal.valueOf(100)).divide(target,16,RoundingMode.HALF_UP);
        if (target.signum()>0 && (lower.signum()<0 || lower.compareTo(target)>0 || upper.signum()<0)) return invalid(down,up,"INVALID_OFFSET");
        BigDecimal min = (target.signum()==0 ? BigDecimal.ZERO : target.subtract(lower)).setScale(8,RoundingMode.HALF_UP).setScale(0,RoundingMode.CEILING);
        BigDecimal max = (target.signum()==0 ? BigDecimal.ZERO : target.add(upper)).setScale(8,RoundingMode.HALF_UP).setScale(0,RoundingMode.FLOOR);
        if (min.abs().compareTo(SAFE)>0 || max.abs().compareTo(SAFE)>0) return invalid(down,up,"QUANTITY_OUT_OF_RANGE");
        return new Tolerance(down,up,lp,hp,min,max,count(min)+"～"+count(max),true,null);
    }
    private static Tolerance invalid(BigDecimal down, BigDecimal up, String reason) {
        return new Tolerance(down,up,null,null,null,null,"—",false,reason);
    }
    public static List<String> missing(String stage, Map<String,Object> record) {
        return (stage.equals("head") ? HEAD_FIELDS : THREAD_FIELDS).stream().filter(f -> text(record,f).isBlank()).toList();
    }
    public static FirstArticle firstArticle(String stage, Long machine, List<Map<String,Object>> records, List<Map<String,Object>> states) {
        var matches = machine == null ? List.<Map<String,Object>>of() : records.stream().filter(r -> Objects.equals(machine,nullableId(r,stage+"_machineId"))).toList();
        var state = states.stream().filter(s -> stage.equals(text(s,"stage")) && Objects.equals(machine,nullableId(s,"machine_id"))).findFirst().orElse(Map.of());
        boolean ambiguous = matches.size()>1;
        var record = matches.size()==1 ? matches.getFirst() : Map.<String,Object>of();
        // No-record/no-state is eligible to ASK for exemption, not granted clearance.
        boolean requires = ambiguous || (state.isEmpty() ? !matches.isEmpty() : bool(state.get("requires_first_article")));
        var missing = missing(stage,record);
        boolean completed = matches.size()==1 && !requires && missing.isEmpty();
        String completedAt = completed ? time(state.get("completed_at") != null ? state.get("completed_at") : record.get("dateTime")) : null;
        String exemption = time(state.get("exemption_confirmed_at"));
        String additional = time(state.get("additional_production_at"));
        boolean clearance = !requires && (completedAt!=null || exemption!=null || additional!=null);
        return new FirstArticle(!matches.isEmpty(),completed,requires,completedAt,
                completedAt!=null ? display(completedAt) : completed ? "已完成" : "",missing,clearance,
                nullableId(record,"id"),exemption,text(state,"exemption_reason"),additional,ambiguous);
    }
    private static Map<String,Action> actions(Order order, String stage, long assignment, Long machine, Status status, FirstArticle first) {
        boolean confirmation = status.statusCode().equals("READY") && !first.hasFirstArticleClearance() && !first.requiresFirstArticle();
        var actions = new LinkedHashMap<String,Action>();
        actions.put("openFirstArticle",action(order,stage,assignment,machine,first,true,true,
                first.firstArticleCompleted() && !first.requiresFirstArticle() ? "完成首件檢測" : "首件檢測",false,"FIRST_ARTICLE",null));
        actions.put("openFieldStatus",action(order,stage,assignment,machine,first,true,true,
                status.statusCode().equals("READY") ? "正式生產" : status.statusLabel(),confirmation,"FIELD_STATUS",null));
        actions.put("confirmFirstArticleExemption",action(order,stage,assignment,machine,first,confirmation,confirmation,
                "確認免首件檢測",confirmation,"FIRST_ARTICLE_EXEMPTION",confirmation ? null : "EXEMPTION_NOT_APPLICABLE"));
        return actions;
    }
    private static Action action(Order o,String stage,long assignment,Long machine,FirstArticle first,boolean visible,boolean enabled,String label,boolean confirm,String type,String reason) {
        return new Action(visible,enabled,label,reason,reason==null ? "" : "目前狀態不符合免首檢確認條件",confirm,confirm ? EXEMPTION_MESSAGE : "",type,o.workOrderCode(),o.databaseId(),stage,assignment,machine,first.recordId());
    }
    private Event event(Map<String,Object> e, Map<String,Map<Long,Map<String,Object>>> machines) {
        String title = text(e,"title");
        String raw = text(e,"log");
        String detail = raw;
        try {
            var node = json.readTree(raw);
            if (node != null && node.isObject()) {
                Map<String,Object> data = json.convertValue(node,new tools.jackson.core.type.TypeReference<Map<String,Object>>() {});
                var parts = new ArrayList<String>();
                if (has(data,"machineId")) {
                    String stage = title.contains("打頭") ? "head" : title.contains("搓牙") ? "thread" : "";
                    Long mid = nullableId(data,"machineId");
                    parts.add("使用機台："+machineName(machines.getOrDefault(stage,Map.of()).get(mid),mid,false));
                }
                for (var key : List.of("assignedBy","outsourcePosition","outsourceCode","materialId"))
                    if (has(data,key)) parts.add(Map.of("assignedBy","派工員編","outsourcePosition","委外廠商","outsourceCode","委外單號","materialId","使用物料編號").get(key)+"："+data.get(key));
                if (has(data,"targetQuantity")) parts.add("預計生產 "+count(decimal(data,"targetQuantity"))+" 件");
                if (has(data,"outsourceQuantity")) parts.add("委外加工 "+count(decimal(data,"outsourceQuantity"))+" 件");
                if (has(data,"targetQuantity") && has(data,"targetDownOffsetQuantity") && has(data,"targetUpOffsetQuantity")) {
                    BigDecimal t = decimal(data,"targetQuantity");
                    parts.add("可接受的產量為 "+count(t.subtract(decimal(data,"targetDownOffsetQuantity")))+"～"+count(t.add(decimal(data,"targetUpOffsetQuantity")))+" 件");
                }
                if (has(data,"currentQuantity")) parts.add("目前已生產 "+count(decimal(data,"currentQuantity"))+" 件");
                for (String key : List.of("quantityChangeReason","reason","remark")) if (has(data,key))
                    parts.add(Map.of("quantityChangeReason","修改原因","reason","原因","remark","備註").get(key)+"："+data.get(key));
                if (!parts.isEmpty()) detail = String.join("；",parts)+"。";
            }
        } catch (RuntimeException ignored) { /* Invalid JSON / unusable values preserve the entire original log. */ }
        String at = time(e.get("time"));
        return new Event(id(e,"id"),title.replaceFirst("^主管派工","派工").replace("主管",""),detail.replace("主管",""),at,at==null ? "未提供" : display(at));
    }
    private static boolean has(Map<String,Object> m,String key) { return m.get(key)!=null && !m.get(key).toString().isEmpty(); }
    private static Material material(Long id,Map<String,Object> m,String specification) {
        if (m==null) return new Material(id,id==null ? "" : "物料 "+id,specification,"",false,id==null ? "派工未指定物料" : "綁定物料不存在");
        return new Material(id,text(m,"material_name").isEmpty() ? "物料 "+id : text(m,"material_name"),
                text(m,"remark").isEmpty() ? specification : text(m,"remark"),text(m,"material_code"),true,null);
    }
    private static String machineName(Map<String,Object> m,Long id,boolean suffix) {
        String name = m==null ? "" : text(m,"head_machine_name");
        if (name.isEmpty() && m!=null) name=text(m,"thread_rolling_machine_name");
        if (name.isEmpty() && m!=null) name=text(m,"thread_machine_name");
        if (suffix) return (name.isEmpty() ? "機台" : name)+"（"+(id==null ? "—" : id)+"）";
        return name.isEmpty() ? "機台編號 "+id : name;
    }
    private static Map<Long,Map<String,Object>> index(List<Map<String,Object>> rows,String key) {
        Map<Long,Map<String,Object>> result = new LinkedHashMap<>();
        rows.forEach(r -> result.put(id(r,key),r)); return result;
    }
    public static String text(Map<String,Object> r,String key) { return Objects.toString(r.get(key),""); }
    public static Long nullableId(Map<String,Object> r,String key) { return r.get(key)==null ? null : new BigDecimal(r.get(key).toString()).longValueExact(); }
    public static long id(Map<String,Object> r,String key) { return Objects.requireNonNull(nullableId(r,key)); }
    public static boolean bool(Object value) { return Boolean.TRUE.equals(value) || value instanceof Number n && n.intValue()!=0; }
    public static BigDecimal decimal(Map<String,Object> r,String key) { return r.get(key)==null ? null : new BigDecimal(r.get(key).toString()); }
    private static BigDecimal zero(BigDecimal value) { return value==null ? BigDecimal.ZERO : value; }
    private static String count(BigDecimal value) { return new DecimalFormat("#,##0.########",DecimalFormatSymbols.getInstance(Locale.TAIWAN)).format(value); }
    public static String time(Object value) {
        if (value==null) return null;
        LocalDateTime local;
        if (value instanceof LocalDateTime l) local=l;
        else if (value instanceof java.sql.Timestamp t) local=t.toLocalDateTime();
        else if (value instanceof java.sql.Date d) local=d.toLocalDate().atStartOfDay();
        else if (value instanceof LocalDate d) local=d.atStartOfDay();
        else throw new IllegalArgumentException("Unsupported database time type: "+value.getClass());
        return local.atZone(ZONE).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
    private static String date(Object value) {
        if (value instanceof java.sql.Date d) return d.toLocalDate().toString();
        if (value instanceof LocalDate d) return d.toString();
        return OffsetDateTime.parse(time(value)).toLocalDate().toString();
    }
    private static String display(String value) { return OffsetDateTime.parse(value).format(DateTimeFormatter.ofPattern("yyyy/M/d HH:mm:ss")); }
}

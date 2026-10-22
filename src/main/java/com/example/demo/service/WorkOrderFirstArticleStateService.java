package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.dao.WorkOrderDetailDao;
import com.example.demo.dto.FirstTestFormRequest;
import com.example.demo.dto.WorkOrderDetailCommandRequest;
import com.example.demo.dto.WorkOrderDetailRequest;
import com.example.demo.dto.WorkOrderDetailResponse.FirstArticle;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import static com.example.demo.service.WorkOrderDetailService.*;

/** All writers lock the work order before assignment/first-test/state rows. */
@Service
public class WorkOrderFirstArticleStateService {
    private final NamedParameterJdbcTemplate jdbc;
    private final WorkOrderDetailDao dao;
    @Autowired
    public WorkOrderFirstArticleStateService(NamedParameterJdbcTemplate jdbc, WorkOrderDetailDao dao) {
        this.jdbc=jdbc; this.dao=dao;
    }
    public record CommandResult(Long firstArticleId, FirstArticle firstArticle) {}
    public void lockOrder(long work) {
        if (jdbc.queryForList("SELECT id FROM mydb.work_order WHERE id=:id FOR UPDATE",Map.of("id",work)).isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"工單不存在");
    }
    public void lockAssignmentOrder(String stage,long assignment) {
        var rows=jdbc.queryForList("SELECT work_order_code FROM mydb."+WorkOrderDetailDao.table(stage)+" WHERE id=:id",Map.of("id",assignment));
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"派工不存在");
        lockOrder(id(rows.getFirst(),"work_order_code"));
    }
    @Transactional
    public CommandResult command(WorkOrderDetailRequest query,String stage,long assignment,String action,WorkOrderDetailCommandRequest request) {
        require(List.of("head","thread").contains(stage) && assignment>0,"stage 僅允許 head/thread；派工 ID 必須為正數");
        var orders=dao.order(query.workOrderCode());
        if (orders.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"工單不存在");
        conflict(orders.size()==1,"工單編號不唯一");
        long work=id(orders.getFirst(),"id"); lockOrder(work);
        var rows=jdbc.queryForList("SELECT * FROM mydb."+WorkOrderDetailDao.table(stage)+" WHERE id=:id AND work_order_code=:work FOR UPDATE",Map.of("id",assignment,"work",work));
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"工單派工不存在");
        var row=rows.getFirst(); Long machine=nullableId(row,"machineId");
        conflict(machine!=null,"派工缺少機台 ID");
        var records=dao.firstArticles(work);
        FirstArticle first=firstArticle(stage,machine,records,dao.states(work));
        conflict(!first.ambiguous(),"同工單同製程同機台已有多筆首件，請先合併");
        LocalDateTime now=LocalDateTime.now(ZONE);
        var key=new HashMap<String,Object>(Map.of("work",work,"stage",stage,"machine",machine,"now",now));
        switch(action) {
            case "prepare" -> {
                if (!first.firstArticleExists()) {
                    key.put("material",row.get("material_id"));
                    jdbc.update("INSERT INTO mydb.first_test_form (work_order_code,"+stage+"_machineId,"+stage+"_material_code,state) VALUES (:work,:machine,:material,FALSE)",key);
                    saveState(key,"requires_first_article=TRUE, completed_at=NULL, exemption_confirmed_at=NULL, exemption_reason=NULL, additional_production_at=NULL");
                }
            }
            case "rerun" -> {
                require(request!=null && request.reason()!=null && !request.reason().isBlank(),"重跑原因必填");
                saveState(key,"requires_first_article=TRUE, completed_at=NULL, exemption_confirmed_at=NULL, exemption_reason=NULL, additional_production_at=NULL");
            }
            case "exemption" -> {
                require(request!=null && Set.of("其他製程","委外","盤元異常補領").contains(Objects.toString(request.reason(),"")),"請提供免首檢原因：其他製程／委外／盤元異常補領");
                conflict(assignmentStatus(row).statusCode().equals("READY") && !first.hasFirstArticleClearance() && !first.requiresFirstArticle(),"目前狀態不符合免首檢條件");
                key.put("reason",request.reason());
                saveState(key,"requires_first_article=FALSE, exemption_confirmed_at=:now, exemption_reason=:reason");
            }
            case "additional-production" -> {
                require(request!=null && request.reason()!=null && !request.reason().isBlank(),"追加生產原因必填");
                BigDecimal target=request.targetQuantity(), previous=decimal(row,"target_quantity"), current=decimal(row,"current_quantity");
                require(target!=null && previous!=null && current!=null && target.compareTo(previous)>0 && target.compareTo(current)>0,"追加目標必須大於原目標及已生產量");
                require(tolerance(target,decimal(row,"target_down_offset_quantity"),decimal(row,"target_up_offset_quantity")).valid(),"目標產量超出允收範圍");
                conflict(List.of("CLOSED","CLOSED_ABNORMAL").contains(assignmentStatus(row).statusCode()),"僅已完成結單的派工可追加生產");
                key.put("id",assignment); key.put("target",target); key.put("reason",request.reason());
                jdbc.update("UPDATE mydb."+WorkOrderDetailDao.table(stage)+" SET previous_quantity=target_quantity,target_quantity=:target,quantity_change_reason=:reason,order_closed=0,approved=NULL,reviewer=NULL,opinion=NULL,end_at=NULL,production_status='待續產' WHERE id=:id",key);
                // A rerun requirement always survives additional production.
                key.put("requires",first.requiresFirstArticle());
                saveState(key,"requires_first_article=:requires, additional_production_at=:now");
            }
            default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND,"未知首件流程動作");
        }
        if (!action.equals("prepare")) {
            key.put("title","首件流程－"+stage+"－"+action);
            key.put("log","派工 "+assignment+"；原因："+request.reason());
            jdbc.update("INSERT INTO mydb.history_log (title,`time`,work_order_code,log) VALUES (:title,:now,:work,:log)",key);
        }
        var result=firstArticle(stage,machine,dao.firstArticles(work),dao.states(work));
        return new CommandResult(result.recordId(),result);
    }
    private void saveState(Map<String,Object> key,String assignments) {
        jdbc.update("INSERT IGNORE INTO mydb.work_order_first_article_state (work_order_id,stage,machine_id,requires_first_article) VALUES (:work,:stage,:machine,TRUE)",key);
        jdbc.update("UPDATE mydb.work_order_first_article_state SET "+assignments+" WHERE work_order_id=:work AND stage=:stage AND machine_id=:machine",key);
    }
    /** Called inside legacy first-test save transaction, after measurements have been saved. */
    public void synchronizeLegacy(long recordId, FirstTestFormRequest request) {
        var matches=jdbc.queryForList("SELECT * FROM mydb.first_test_form WHERE id=:id",Map.of("id",recordId));
        if(matches.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"首件不存在");
        var record=matches.getFirst(); long work=id(record,"work_order_code");
        var stages=new ArrayList<String>();
        if(request.headMachineId()!=null || HEAD_FIELDS.stream().anyMatch(request.toUpdateParameters()::containsKey)) stages.add("head");
        if(request.threadMachineId()!=null || THREAD_FIELDS.stream().anyMatch(request.toUpdateParameters()::containsKey)) stages.add("thread");
        if(stages.isEmpty() && request.state()!=null) {
            if(nullableId(record,"head_machineId")!=null && id(record,"head_machineId")>0) stages.add("head");
            if(nullableId(record,"thread_machineId")!=null && id(record,"thread_machineId")>0) stages.add("thread");
            conflict(stages.size()==1,"共用首件紀錄的 state 更新須提供製程機台 ID，避免影響另一製程");
        }
        for(String stage:stages) {
            Long machine=nullableId(record,stage+"_machineId");
            conflict(machine!=null && machine>0,"首件不存在指定製程機台");
            Integer requestedMachine=stage.equals("head")?request.headMachineId():request.threadMachineId();
            conflict(requestedMachine==null || requestedMachine.longValue()==machine,"機台 ID 與首件不一致");
            var all=dao.firstArticles(work);
            conflict(all.stream().filter(r -> Objects.equals(machine,nullableId(r,stage+"_machineId"))).count()==1,"首件重複，請先合併");
            var key=new HashMap<String,Object>(Map.of("work",work,"stage",stage,"machine",machine,"now",LocalDateTime.now(ZONE)));
            if(Boolean.TRUE.equals(request.state())) {
                require(missing(stage,record).isEmpty(),"完成首件須填寫全部製程檢驗欄位");
                saveState(key,"requires_first_article=FALSE, completed_at=:now, exemption_confirmed_at=NULL, exemption_reason=NULL, additional_production_at=NULL");
            } else if(Boolean.FALSE.equals(request.state()) || !missing(stage,record).isEmpty()) {
                saveState(key,"requires_first_article=TRUE, completed_at=NULL, exemption_confirmed_at=NULL, exemption_reason=NULL, additional_production_at=NULL");
            }
        }
    }
    public void lockRecordOrder(long record) {
        var rows=jdbc.queryForList("SELECT work_order_code FROM mydb.first_test_form WHERE id=:id",Map.of("id",record));
        if(rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"首件不存在");
        lockOrder(id(rows.getFirst(),"work_order_code"));
    }
    public void validateProductionClearance(String stage,Map<String,Object> row) {
        long work=id(row,"work_order_code");
        var first=firstArticle(stage,nullableId(row,"machineId"),dao.firstArticles(work),dao.states(work));
        conflict(first.hasFirstArticleClearance(),"尚未取得首件放行資格，請完成首件或確認符合免首檢條件");
    }
    private static void require(boolean ok,String message) { if(!ok) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,message); }
    private static void conflict(boolean ok,String message) { if(!ok) throw new ResponseStatusException(HttpStatus.CONFLICT,message); }
}

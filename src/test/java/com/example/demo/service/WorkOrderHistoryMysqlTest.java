package com.example.demo.service;

import com.example.demo.controller.WorkOrderHistoryController;
import com.example.demo.dao.WorkOrderHistoryDao;
import com.example.demo.dto.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.io.StringWriter;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Opt-in: creates only connection-local TEMPORARY tables, never updates persistent tables. */
@EnabledIfSystemProperty(named="history.mysql.tests", matches="true")
class WorkOrderHistoryMysqlTest {
    Connection connection;
    JdbcTemplate jdbc;
    WorkOrderHistoryService service;
    MockMvc mvc;
    @BeforeEach void setup() throws Exception {
        Properties p = new Properties();
        try (var in = Files.newInputStream(Path.of("src/main/resources/application.properties"))) { p.load(in); }
        connection = DriverManager.getConnection(p.getProperty("spring.datasource.url"),
                p.getProperty("spring.datasource.username"),p.getProperty("spring.datasource.password"));
        var ds = new SingleConnectionDataSource(connection, true);
        jdbc = new JdbcTemplate(ds);
        // Shadow production names for this connection only. Nullable IDs also test defensive fallbacks.
        jdbc.execute("CREATE TEMPORARY TABLE mydb.work_order (id INT PRIMARY KEY, work_code INT, furnaceNo TEXT, model TEXT)");
        jdbc.execute("CREATE TEMPORARY TABLE mydb.head_machine (id INT PRIMARY KEY, head_machine_name VARCHAR(255))");
        jdbc.execute("CREATE TEMPORARY TABLE mydb.thread_machine (id INT PRIMARY KEY, thread_rolling_machine_name VARCHAR(255))");
        for (String table : List.of("work_order_heading_machine", "work_order_threading_machine"))
            jdbc.execute("CREATE TEMPORARY TABLE mydb." + table + " (id INT PRIMARY KEY, work_order_code INT, machineId INT, "
                    + "target_quantity FLOAT, current_quantity FLOAT, oee FLOAT, started_at DATETIME(6), end_at DATETIME(6), "
                    + "order_closed INT, approved INT, production_status TEXT, reason TEXT, remark TEXT, opinion TEXT)");
        jdbc.execute("CREATE TEMPORARY TABLE mydb.outsource_form (id INT PRIMARY KEY, work_order_id INT, outsource_position TEXT, "
                + "outsource_quantity FLOAT, current_quantity INT, started_at DATETIME(6), end_at DATETIME(6), state TEXT, change_reason TEXT)");
        jdbc.update("INSERT INTO mydb.work_order VALUES (1,2,'Heat_A','M%1'),(2,10,'Heat_B','m2')");
        jdbc.update("INSERT INTO mydb.head_machine VALUES (1,'Head 2')");
        jdbc.update("INSERT INTO mydb.thread_machine VALUES (1,'Thread 10')");
        jdbc.update("INSERT INTO mydb.work_order_heading_machine VALUES "
                + "(1,1,1,100,20,90,'2026-10-20 12:00:00','2026-10-21 00:00:00',2,1,'正式生產','原因','','意見'),"
                + "(2,1,999,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),"
                + "(3,999,999,NULL,NULL,NULL,NULL,NULL,0,0,'',NULL,NULL,NULL),"
                + "(4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL)");
        jdbc.update("INSERT INTO mydb.work_order_threading_machine VALUES "
                + "(1,1,1,100,30,80,'2026-10-21 00:00:00',NULL,0,0,'正式生產',NULL,'備註',NULL),"
                + "(2,2,1,100,40,70,'2026-10-22 00:00:00',NULL,1,0,'正式生產',NULL,NULL,NULL)");
        jdbc.update("INSERT INTO mydb.outsource_form VALUES "
                + "(1,1,'Vendor',50,10,'2026-10-20 00:00:00','2026-10-21 23:59:59.999999','委外中','變更'),"
                + "(2,888,NULL,NULL,NULL,NULL,NULL,NULL,NULL),"
                + "(3,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL)");
        service = new WorkOrderHistoryService(new WorkOrderHistoryDao(new NamedParameterJdbcTemplate(ds)));
        mvc = MockMvcBuilders.standaloneSetup(new WorkOrderHistoryController(service)).build();
    }
    @AfterEach void close() throws Exception { if (connection != null) connection.close(); }
    WorkOrderHistoryQuery q(String order, String machine, String heat, String product, String start, String end, int page, int size) {
        return new WorkOrderHistoryQuery(order,machine,heat,product,start == null ? null : LocalDate.parse(start),
                end == null ? null : LocalDate.parse(end),page,size);
    }
    WorkOrderHistoryPage all() { return service.list(q(null,null,null,null,null,null,1,200)); }
    WorkOrderHistoryItem item(String id) { return all().items().stream().filter(r -> r.recordId().equals(id)).findFirst().orElseThrow(); }
    @Test void mergesAndMapsAllStages() {
        assertEquals(9, all().total());
        assertEquals(Set.of("head","thread","outsourcing"), new HashSet<>(all().items().stream().map(WorkOrderHistoryItem::stage).toList()));
        var head = item("head-1");
        assertEquals("2", head.workOrderId()); assertEquals("Head 2",head.machineName());
        assertEquals("Heat_A",head.heatNumber()); assertEquals("M%1",head.productCode());
        assertEquals("異常結單",head.currentStatus()); assertEquals("原因；意見",head.detail());
        assertEquals("head-1",head.combinationId()); assertTrue(head.canNavigate());
        assertEquals(100,head.targetQuantity().intValue()); assertEquals(20,head.producedQuantity().intValue());
        assertEquals("生產中",item("thread-1").currentStatus());
        var out = item("outsourcing-1");
        assertEquals("委外中",out.currentStatus()); assertEquals("Vendor",out.machineName());
        assertEquals("變更",out.detail()); assertNull(out.oee()); assertNull(out.failureRate()); assertTrue(out.canNavigate());
    }
    @Test void missingRelationsAndNullNumbersSurvive() {
        assertEquals("999",item("head-3").workOrderId()); assertFalse(item("head-3").canNavigate());
        assertEquals("機台 999",item("head-2").machineName()); assertNull(item("head-2").targetQuantity());
        assertEquals("—",item("head-4").machineName()); assertEquals("",item("head-4").workOrderId());
        assertEquals("888",item("outsourcing-2").workOrderId()); assertFalse(item("outsourcing-2").canNavigate());
        assertEquals("",item("outsourcing-2").machineName()); assertEquals("",item("outsourcing-2").currentStatus());
        assertEquals("",item("outsourcing-3").workOrderId()); assertNull(item("outsourcing-3").timestamp());
    }
    @Test void datePreferenceBoundariesAndNullExclusion() {
        assertEquals("2026-10-21T00:00:00+08:00",item("head-1").timestamp());
        assertEquals(item("thread-1").timestamp(),item("thread-1").at());
        var rows=service.list(q(null,null,null,null,"2026-10-21","2026-10-21",1,200));
        assertEquals(List.of("outsourcing-1","head-1","thread-1"),rows.items().stream().map(WorkOrderHistoryItem::recordId).toList());
        assertEquals(3,rows.total());
        assertEquals(4,service.list(q(null,null,null,null,"2026-10-21",null,1,200)).total());
        assertEquals(0,service.list(q(null,null,null,null,null,"2026-10-20",1,200)).total());
    }
    @Test void globalNaturalStableOrderPagingAndTotal() {
        var expected=List.of("head-4","outsourcing-3","outsourcing-1","head-1","thread-1","head-2","thread-2","outsourcing-2","head-3");
        assertEquals(expected,all().items().stream().map(WorkOrderHistoryItem::recordId).toList());
        var page=service.list(q(null,null,null,null,null,null,2,3));
        assertEquals(9,page.total()); assertEquals(expected.subList(3,6),page.items().stream().map(WorkOrderHistoryItem::recordId).toList());
        assertEquals(page,service.list(q(null,null,null,null,null,null,2,3)));
        assertTrue(service.list(q(null,null,null,null,null,null,99,3)).items().isEmpty());
    }
    @Test void includesFiltersCaseWhitespaceAndLiteralWildcards() {
        assertEquals(1,service.list(q(null," veND ",null,null,null,null,1,200)).total());
        assertEquals(4,service.list(q(null,null,"heat_a","%",null,null,1,200)).total());
        assertEquals(0,service.list(q(null,null,"heat%",null,null,null,1,200)).total());
        assertEquals(0,service.list(q("' OR 1=1 --",null,null,null,null,null,1,200)).total());
    }
    @Test void optionsAndCsvIncludeOutsourcingIgnorePaginationAndEscape() throws Exception {
        assertTrue(service.options().get("machineName").contains("Vendor"));
        var out=new StringWriter(); service.export(q(null,null,null,null,null,null,99,1),out);
        assertEquals(11,out.toString().split("\r\n",-1).length);
        assertTrue(out.toString().contains("\"委外中\"")); assertTrue(out.toString().contains("\"變更\""));
        jdbc.update("UPDATE mydb.outsource_form SET change_reason=? WHERE id=1","=SUM(1,2)\n\"quoted\"");
        out=new StringWriter(); service.export(q(null,"Vendor",null,null,null,null,1,1),out);
        assertTrue(out.toString().contains("'=SUM(1,2)\n\"\"quoted\"\""));
        assertTrue(out.toString().startsWith("\ufeff"));
    }
    @Test void httpBindingValidationAndContract() throws Exception {
        var response=mvc.perform(get("/work-order-history").param("pageSize","1")).andExpect(status().isOk()).andReturn();
        assertTrue(response.getResponse().getContentAsString().contains("\"total\":9"));
        mvc.perform(get("/work-order-history").param("startDate","not-a-date")).andExpect(status().isBadRequest());
        mvc.perform(get("/work-order-history").param("page","0")).andExpect(status().isBadRequest());
        mvc.perform(get("/work-order-history").param("pageSize","201")).andExpect(status().isBadRequest());
        mvc.perform(get("/work-order-history").param("startDate","2026-10-22").param("endDate","2026-10-21")).andExpect(status().isBadRequest());
        mvc.perform(get("/work-order-history/options")).andExpect(status().isOk());
        var csv=mvc.perform(get("/work-order-history/export").param("machineName","Vendor")).andExpect(status().isOk()).andReturn();
        assertTrue(csv.getResponse().getContentType().contains("text/csv"));
    }
    @Test void optionsPreserveCaseAndWhitespaceLikeJavascriptSet() {
        jdbc.update("INSERT INTO mydb.outsource_form(id,outsource_position) VALUES (10,'vendor'),(11,' ')");
        var names=service.options().get("machineName");
        assertTrue(names.containsAll(List.of("vendor","Vendor"," ")));
        jdbc.update("UPDATE mydb.head_machine SET head_machine_name=' ' WHERE id=1");
        assertEquals(" ",item("head-1").machineName());
    }
    @Test void productionSchemaReadOnlySmoke() throws Exception {
        Properties p = new Properties();
        try (var in=Files.newInputStream(Path.of("src/main/resources/application.properties"))) { p.load(in); }
        try (var actual=DriverManager.getConnection(p.getProperty("spring.datasource.url"),
                p.getProperty("spring.datasource.username"),p.getProperty("spring.datasource.password"))) {
            actual.setReadOnly(true);
            var dao=new WorkOrderHistoryDao(new NamedParameterJdbcTemplate(new SingleConnectionDataSource(actual,true)));
            var query=q(null,null,null,null,null,null,1,10);
            assertTrue(dao.count(query)>=0);
            assertTrue(dao.page(query).size()<=10);
        }
    }
    @Test
    @EnabledIfSystemProperty(named="history.frontend.tests", matches="true")
    void originalFrontendFunctionMatchesHeadAndThread() throws Exception {
        // Execute the actual source functions, not a handwritten port of the JS algorithm.
        String script="""
            const fs=require('node:fs');
            const source=fs.readFileSync(process.argv[1],'utf8');
            const functionSource=(start,end)=>source.slice(source.indexOf(start),source.indexOf(end)).replace('export ','');
            const list=x=>x;
            const fixtures={
              '/workOrder/getWorkOrder':[{id:1,workOrder:2,furaceNo:'Heat_A',model:'M%1'}],
              '/workOrderHeadingMachine/getWorkOrderHeadingMachine':[{id:1,workOrderCode:1,machineId:1,targetQuantity:100,currentQuantity:20,oee:90,
                startedAt:'2026-10-20T12:00:00+08:00',endAt:'2026-10-21T00:00:00+08:00',orderClosed:2,approved:true,productionStatus:'正式生產',reason:'原因',remark:'',opinion:'意見'}],
              '/workOrderThreadingMachine/getWorkOrderThreadingMachine':[{id:1,workOrderCode:1,machineId:1,targetQuantity:100,currentQuantity:30,oee:80,
                startedAt:'2026-10-21T00:00:00+08:00',orderClosed:0,approved:false,productionStatus:'正式生產',remark:'備註'}],
              '/headMachine/getMachines':[{id:1,headMachineName:'Head 2'}],
              '/threadMachine/getMachines':[{id:1,threadRollingMachineName:'Thread 10'}]
            };
            const get=async path=>fixtures[path];
            eval(functionSource('export function assignmentStatus','export function normalizeOrder')+
                 functionSource('export function normalizeOrder','export async function resolveWorkOrderBarcode')+
                 functionSource('export async function getWorkOrderEventHistory','let productionRowsPromise')+
                 ';getWorkOrderEventHistory().then(x=>console.log(JSON.stringify(x)));');
            """;
        var process=new ProcessBuilder("node","-e",script,
                "C:/0_working/5-datacanvas/datacanvas/src/services/manufacturingApi.js").redirectErrorStream(true).start();
        String json=new String(process.getInputStream().readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);
        assertEquals(0,process.waitFor(),json);
        var mapper=tools.jackson.databind.json.JsonMapper.builder().build();
        var expected=mapper.readTree(json);
        var actual=mapper.valueToTree(List.of(item("head-1"),item("thread-1")));
        assertEquals(expected.size(),actual.size());
        for (int i=0;i<expected.size();i++) {
            for (var field:expected.get(i).properties()) {
                var value=actual.get(i).get(field.getKey());
                if (field.getValue().isNumber()) assertEquals(field.getValue().doubleValue(),value.doubleValue());
                else assertEquals(field.getValue(),value,field.getKey());
            }
        }
    }
}


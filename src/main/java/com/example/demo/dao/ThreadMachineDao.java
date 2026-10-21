package com.example.demo.dao;

import com.example.demo.rowMapper.ThreadMachineRowMapper;
import com.example.demo.dto.ThreadMachineRequest;
import com.example.demo.model.ThreadMachine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ThreadMachineDao {

    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public int addThreadMachine(ThreadMachineRequest request) {
        String sql = """
                INSERT INTO mydb.thread_machine
                    (thread_rolling_machine_code, thread_rolling_machine_name)
                VALUES (:threadRollingMachineCode, :threadRollingMachineName)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("threadRollingMachineCode", request.threadRollingMachineCode())
                .addValue("threadRollingMachineName", request.threadRollingMachineName());
        return namedParameterJdbcTemplate.update(sql, params);
    }

    public int updateThreadMachine(int id, ThreadMachineRequest request) {
        String sql = """
                UPDATE mydb.thread_machine
                SET thread_rolling_machine_code = :threadRollingMachineCode,
                    thread_rolling_machine_name = :threadRollingMachineName
                WHERE id = :id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("threadRollingMachineCode", request.threadRollingMachineCode())
                .addValue("threadRollingMachineName", request.threadRollingMachineName());
        return namedParameterJdbcTemplate.update(sql, params);
    }

    public int deleteThreadMachine(int id) {
        String sql = "DELETE FROM mydb.thread_machine WHERE id = :id";
        return namedParameterJdbcTemplate.update(sql, Map.of("id", id));
    }

    public List<ThreadMachine> getThreadMachines() {
        String sql = "SELECT t.* FROM mydb.thread_machine t";
        return namedParameterJdbcTemplate.query(sql, new ThreadMachineRowMapper());
    }

    public ThreadMachine getThreadMachine(int id) {
        String sql = "SELECT t.* FROM mydb.thread_machine t WHERE t.id = :id";
        List<ThreadMachine> threadMachines = namedParameterJdbcTemplate.query(
                sql, Map.of("id", id), new ThreadMachineRowMapper());
        if (threadMachines.isEmpty()) {
            return new ThreadMachine();
        }
        return threadMachines.getFirst();
    }
}

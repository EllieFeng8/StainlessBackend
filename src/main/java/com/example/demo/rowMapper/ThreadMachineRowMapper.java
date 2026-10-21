package com.example.demo.rowMapper;

import com.example.demo.model.ThreadMachine;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ThreadMachineRowMapper implements RowMapper<ThreadMachine> {

    @Override
    public ThreadMachine mapRow(ResultSet rs, int rowNum) throws SQLException {
        ThreadMachine threadMachine = new ThreadMachine();
        threadMachine.setId(rs.getInt("id"));
        threadMachine.setThreadRollingMachineCode(rs.getString("thread_rolling_machine_code"));
        threadMachine.setThreadRollingMachineName(rs.getString("thread_rolling_machine_name"));
        return threadMachine;
    }
}

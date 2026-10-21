package com.example.demo.rowMapper;

import com.example.demo.model.HeadMachine;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;


public class HeadMachineRowMapper implements RowMapper<HeadMachine> {

    @Override
    public HeadMachine mapRow(ResultSet rs, int rowNum) throws SQLException {
        HeadMachine headMachine = new HeadMachine();
        headMachine.setId(rs.getInt("id"));
        headMachine.setHeadMachineCode(rs.getInt("head_machine_code"));
        headMachine.setHeadMachineName(rs.getString("head_machine_name"));
        return headMachine;
    }
}

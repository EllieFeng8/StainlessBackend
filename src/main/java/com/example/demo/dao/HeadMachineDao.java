package com.example.demo.dao;

import com.example.demo.rowMapper.HeadMachineRowMapper;
import com.example.demo.dto.HeadMachineRequest;
import com.example.demo.model.HeadMachine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Component
public class HeadMachineDao {

    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public int addHeadMachine(HeadMachineRequest request) {
        String sql = """
                INSERT INTO mydb.head_machine (head_machine_code, head_machine_name)
                VALUES (:headMachineCode, :headMachineName)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("headMachineCode", request.headMachineCode())
                .addValue("headMachineName", request.headMachineName());
        return namedParameterJdbcTemplate.update(sql, params);
    }

    public int updateHeadMachine(int id, HeadMachineRequest request) {
        String sql = """
                UPDATE mydb.head_machine
                SET head_machine_code = COALESCE(:headMachineCode, head_machine_code),
                    head_machine_name = COALESCE(:headMachineName, head_machine_name)
                WHERE id = :id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("headMachineCode", request.headMachineCode())
                .addValue("headMachineName", request.headMachineName());
        return namedParameterJdbcTemplate.update(sql, params);
    }

    public int deleteHeadMachine(int id) {
        String sql = "DELETE FROM mydb.head_machine WHERE id = :id";
        return namedParameterJdbcTemplate.update(sql, Map.of("id", id));
    }


    public List<HeadMachine> getHeadMachines() {
        String sql = "SELECT t.* FROM mydb.head_machine t ";
        List<HeadMachine> headMachines = namedParameterJdbcTemplate.query(sql,new HeadMachineRowMapper());
        return headMachines;
    }

    public Optional<HeadMachine> getHeadMachine(int _id) {
        String sql = "SELECT t.* FROM mydb.head_machine t WHERE id = :id";
        Map<String,Object> _map = new HashMap<>();
        _map.put("id",_id);
        List<HeadMachine> headMachines = namedParameterJdbcTemplate.query(sql,_map,new HeadMachineRowMapper());
        return headMachines.stream().findFirst();
    }
}

package com.example.demo.dao;

import com.example.demo.rowMapper.MemberRowMapper;
import com.example.demo.dto.MemberPatchRequest;
import com.example.demo.model.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MemberDao {

    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public List<Member> getMembers(){
        String sql = "SELECT t.* FROM mydb.member t ";
        List<Member> members = namedParameterJdbcTemplate.query(sql,new MemberRowMapper());
        return members;
    }

    public Member getMember(int _id){
        String sql = "SELECT t.* FROM mydb.member t WHERE employee_code = :id";
        Map<String,Object> _map = new HashMap<>();
        _map.put("id",_id);
        List<Member> members = namedParameterJdbcTemplate.query(sql,_map,new MemberRowMapper());
        if(members.isEmpty()){
            return new Member();
        }
        return members.getFirst();
    }

    public int deleteMember(int _id){
        String sql = "DELETE FROM mydb.member WHERE employee_code = :id";
        Map<String,Object> _map = new HashMap<>();
        _map.put("id",_id);
        return namedParameterJdbcTemplate.update(sql,_map);
    }

    public int addMember(Member member){
        String sql = "INSERT INTO mydb.member (employee_code, name, role) VALUES (:id, :name, :role)";
        Map<String,Object> _map = new HashMap<>();
        _map.put("id",member.getEmployeeCode());
        _map.put("name",member.getName());
        _map.put("role",member.getRole());
        return namedParameterJdbcTemplate.update(sql,_map);
    }

    public int updateMember(int employeeCode, MemberPatchRequest request) {
        String sql = """
                UPDATE mydb.member
                SET name = COALESCE(:name, name),
                    role = COALESCE(:role, role)
                WHERE employee_code = :employeeCode
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("employeeCode", employeeCode);
        params.put("name", request.name());
        params.put("role", request.role());
        return namedParameterJdbcTemplate.update(sql, params);
    }

    public boolean existsByEmployeeCode(int employeeCode) {
        String sql = "SELECT COUNT(*) FROM mydb.member WHERE employee_code = :employeeCode";
        Integer count = namedParameterJdbcTemplate.queryForObject(sql,
                Map.of("employeeCode", employeeCode), Integer.class);
        return count != null && count > 0;
    }

}

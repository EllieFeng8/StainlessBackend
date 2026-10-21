package com.example.demo.rowMapper;

import com.example.demo.model.Member;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MemberRowMapper implements RowMapper<Member> {

    @Override
    public Member mapRow(ResultSet rs, int rowNum) throws SQLException {
        Member member = new Member();
        member.setId(rs.getInt("id"));
        member.setEmployeeCode(rs.getString("employee_code"));
        member.setName(rs.getString("name"));
        member.setRole(rs.getString("role"));
        return member;
    }
}

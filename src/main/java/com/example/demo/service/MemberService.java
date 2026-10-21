package com.example.demo.service;

import com.example.demo.dao.MemberDao;
import com.example.demo.dto.MemberPatchRequest;
import com.example.demo.model.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MemberService {
    @Autowired
    MemberDao memberDao;

    public List<Member> membersList(){return memberDao.getMembers();}
    public Member singleMember(int employcode){return memberDao.getMember(employcode);}
    public int addMember(Member _m){return memberDao.addMember(_m);}
    public int deleteMember(int employcode){return memberDao.deleteMember(employcode);}

    public void updateMember(int employeeCode, MemberPatchRequest request) {
        if (request.name() == null && request.role() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "At least one of name or role is required");
        }
        if ((request.name() != null && request.name().isBlank())
                || (request.role() != null && request.role().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "name and role must not be blank");
        }
        int updated = memberDao.updateMember(employeeCode, request);
        if (updated == 0 && !memberDao.existsByEmployeeCode(employeeCode)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found");
        }
    }
}

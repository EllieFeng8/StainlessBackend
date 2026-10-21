package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.MemberPatchRequest;
import com.example.demo.model.Member;
import com.example.demo.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/members")
public class MemberController {

    @Autowired
    MemberService memberService;

    @GetMapping("/getMembers")
    public List<Member> employees() {
        List<Member> members = memberService.membersList();
        return members;
    }
    @GetMapping("/getMember/{id}")
    public Member employee(@PathVariable("id") int id) {
        Member member = memberService.singleMember(id);
        return member;
    }


    // The path id is employee_code, matching the existing member endpoints.
    @PatchMapping("/updateMember/{id}")
    public ResponseEntity<ApiResponse<Void>> updateMember(@PathVariable("id") int id,
                                                         @RequestBody MemberPatchRequest request) {
        memberService.updateMember(id, request);
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }

    @DeleteMapping("/deleteMember/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMember(@PathVariable("id") int id) {
        if (memberService.deleteMember(id) < 1) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail(333, "111"));
        } else {
            return ResponseEntity.ok(ApiResponse.success("OK"));
        }
    }

    @PostMapping("/addMember")
    public ResponseEntity<ApiResponse<Void>> addEmployee(@RequestBody Member _m) {
//        List<Member> members =
        if (memberService.addMember(_m) < 1) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail(333, "111"));
        } else {
            return ResponseEntity.ok(ApiResponse.success("OK"));
        }
    }
}

package com.fitlife.controller;

import com.fitlife.dto.ApiResponse;
import com.fitlife.dto.GymPackageResponse;
import com.fitlife.dto.MemberCreationRequest;
import com.fitlife.dto.MemberResponse;
import com.fitlife.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<ApiResponse<MemberResponse>> createMember(@Valid @RequestBody MemberCreationRequest request) {
        MemberResponse result = memberService.createMember(request);

        ApiResponse<MemberResponse> response = ApiResponse.<MemberResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Member created successfully")
                .data(result)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
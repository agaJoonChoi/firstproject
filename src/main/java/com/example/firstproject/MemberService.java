package com.example.firstproject;

import com.example.firstproject.dto.MemberFormDTO;

public interface MemberService {

    Long join(MemberFormDTO memberFormDTO);
}
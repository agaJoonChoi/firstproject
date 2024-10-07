package com.example.firstproject;

import com.example.firstproject.dto.MemberFormDTO;
import com.example.firstproject.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service			// 내가 서비스다
@RequiredArgsConstructor	// 밑에 MemberRepository의 생성자를 쓰지 않기 위해서
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    @Override
    public Long join(MemberFormDTO memberFormDTO) {
        Member member = Member.builder()
                .id(Long.valueOf(memberFormDTO.getId()))
                .username(memberFormDTO.getName())
                .password(memberFormDTO.getPw())
                .build();

        return memberRepository.save(member).getId();
    }
}
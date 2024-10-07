package com.example.firstproject.repository;

import com.example.firstproject.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

}
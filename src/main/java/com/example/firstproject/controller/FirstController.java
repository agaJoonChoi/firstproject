package com.example.firstproject.controller;

import com.example.firstproject.CreateToken;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class FirstController {

    @GetMapping("/hi")
    public String niceToMeetYou(Model model) {
        model.addAttribute("username", "cjy");

        String accessToken = CreateToken.accessToken();

        log.info(accessToken);
        return "greetings"; // templates/greetings.mustache -> 브라우저로 전송
    }

    @GetMapping("/bye")
    public String seeYouNext(Model model) {
        model.addAttribute("nickname", "홍길동");
        return "goodbye";
    }
}


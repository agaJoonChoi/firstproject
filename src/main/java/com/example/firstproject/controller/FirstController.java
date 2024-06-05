package com.example.firstproject.controller;

import com.example.firstproject.CreateToken;
import com.example.firstproject.MusicSearch;

import lombok.extern.slf4j.Slf4j;
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

        MusicSearch search = new MusicSearch();

        String response = search.search(accessToken, "해야");
//        log.info(response);

//        JsonObject jsonElement = (JsonObject)JsonParser.parseString(response);

        //log.info(jsonElement.getClass() + ":" + jsonElement + "");

//        log.info(jsonElement.get("tracks")+"");

        return "greetings"; // templates/greetings.mustache -> 브라우저로 전송
    }

    @GetMapping("/bye")
    public String seeYouNext(Model model) {
        model.addAttribute("nickname", "홍길동");
        return "goodbye";
    }

    @GetMapping("/search")
    public String SearchView(Model model) {
        MusicSearch search1 = new MusicSearch();

        String accessToken1 = CreateToken.accessToken();
        String response1 = search1.search(accessToken1, "keyword");
        model.addAttribute("response1", response1);
        log.info(response1);
        return "musicResult";

    }
}


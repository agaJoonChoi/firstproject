package com.example.firstproject.controller;

import com.example.firstproject.CreateToken;
import com.example.firstproject.service.MusicSearch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class FirstController {

    @GetMapping("/hi")
    public String niceToMeetYou(Model model) {
        model.addAttribute("username", "cjy");

        String accessToken = CreateToken.accessToken();
        log.info(accessToken);

        MusicSearch search = new MusicSearch();

//        String response = search.search(accessToken, "해야");
//        log.info(response);

//        JsonObject jsonElement = (JsonObject)JsonParser.parseString(response);

        //log.info(jsonElement.getClass() + ":" + jsonElement + "");

//        log.info(jsonElement.get("tracks")+"");

        return "greetings"; // templates/greetings.mustache -> 브라우저로 전송
    }

    @GetMapping("/hello")
    public String seeYouNext(Model model) {
        model.addAttribute("nickname", "홍길동");
        return "hello";
    }

    @GetMapping("/search")
    public String SearchView(Model model, @RequestParam String keyword) throws Exception {
        MusicSearch search = new MusicSearch();

        String accessToken = CreateToken.accessToken();
        List<Map<String, String>> searchResults = search.search(accessToken, keyword, 100, 0);
        model.addAttribute("searchResults", searchResults);
        model.addAttribute("totalTracks", searchResults.size()); // 총 곡 수를 모델에 추가
        log.info("Keyword: " + keyword);
        log.info("Search Results: " + searchResults.toString());
        return "musicResult"; // Mustache 템플릿 이름
    }

    @GetMapping("/join")
    public String Home() {

        return "join";
    }





}


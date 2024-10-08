package com.example.firstproject.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Service
public class BillboardService {

    public List<Map<String, Object>> getBillboardTop100() {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:5000/api/billboard/top100";

        try {
            return restTemplate.getForObject(url, List.class);
        } catch (RestClientException e) {
            // 예외 발생 시 콘솔에 에러 메시지 출력
            System.err.println("Error calling Billboard API: " + e.getMessage());
            return null;
        }
    }
}


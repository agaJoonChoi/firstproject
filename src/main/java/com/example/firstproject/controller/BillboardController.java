package com.example.firstproject.controller;

import com.example.firstproject.service.BillboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class BillboardController {

    @Autowired
    private BillboardService billboardService;

    @GetMapping("/billboard/top100")
    public String getBillboardTop100(Model model) {
        List<Map<String, Object>> top100 = billboardService.getBillboardTop100();

        if (top100 == null) {
            model.addAttribute("error", "Unable to fetch Billboard Top 100.");
            return "error";
        }

        model.addAttribute("top100", top100);
        return "billboard";
    }

}

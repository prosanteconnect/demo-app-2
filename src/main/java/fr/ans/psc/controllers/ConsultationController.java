package fr.ans.psc.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/secure/consultation")
public class ConsultationController {

    @GetMapping()
    public String navigate() {
        return "/new-consultation";
    }
}

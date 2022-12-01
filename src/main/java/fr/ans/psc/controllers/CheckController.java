package fr.ans.psc.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CheckController {
    @GetMapping("/insecure/check")
    public String check() {
        return "demo app 2 is alive";
    }
}

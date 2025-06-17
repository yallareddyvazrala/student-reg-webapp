package com.rushi.controller;



import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RegistrationApiController {

    @GetMapping("/api/registrations")
    
    public List<HomeController.RegistrationForm> getRegistrations() {
        return HomeController.REGISTRATIONS;
    }
}

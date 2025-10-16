package com.ufg.hemoubiquitous_monitor.controller;

import com.ufg.hemoubiquitous_monitor.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NotificationController {
    @Autowired
    private WebSocketService webSocketService;

    @GetMapping("/test")
    public void receiveMessage(Object message) {
     this.webSocketService.sendMessage("teste", "teste");
    }
}

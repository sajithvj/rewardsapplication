package com.charter.rewards.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class AuthController {

  @GetMapping("/admin/auth")
  public String auth() {
    return "Auth successful";
  }

}

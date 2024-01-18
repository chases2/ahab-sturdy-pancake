package com.example.ahab.pollingsub.controllers;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class PingController {
  @GetMapping("/ping")
  public String ping(@RequestParam(required = false) String name)
  {
    if (Strings.isNullOrEmpty(name)) {
      return "Hello, world!\n";
    }
    return String.format("Hello, %s!\n", name);
  }
}

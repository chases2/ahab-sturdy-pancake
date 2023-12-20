package com.google.cloud.releng.ahabpublisher.controllers;

import com.google.cloud.releng.ahabpublisher.services.Publisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
@RequiredArgsConstructor
public class StartStopController {

  private final Publisher messagingGateway;

  @PostMapping("/start")
  public String start() {
    log.info("Received start request");
    boolean prevState = messagingGateway.start();
    if (prevState) {
      return "Already started.\n";
    }
    return "Starting!\n";
  }

  @PostMapping("/stop")
  public String stop() {
    log.info("Received stop request");
    boolean prevState = messagingGateway.stop();

    if (!prevState) {
      return "Already stopped.\n";
    }
    return "Stopping!\n";
  }


}

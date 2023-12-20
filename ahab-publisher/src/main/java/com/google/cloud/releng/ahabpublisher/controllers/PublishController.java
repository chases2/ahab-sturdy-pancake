package com.google.cloud.releng.ahabpublisher.controllers;

import com.google.cloud.releng.ahabpublisher.services.Publisher;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Slf4j
@RequiredArgsConstructor
public class PublishController {

  private final Publisher messagingGateway;

  @PostMapping("/publishMessage")
  public String publishMessage(@RequestParam String message,
      @RequestParam(required = false) Long quantity) {
    if (Objects.isNull(quantity)) {
      quantity = 1L;
    }
    if (quantity <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Quantity must be a postive number");
    }

    log.info("Received Pub/Sub Message request for {} x{}", message, quantity);
    for (int i = 0; i < quantity; i++) {
      try {
        messagingGateway.sendToPubsub(message);
      } catch (Exception e) {
        log.error("publishMessage: {}", e);
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }
    return "Sent!\n";
  }

}

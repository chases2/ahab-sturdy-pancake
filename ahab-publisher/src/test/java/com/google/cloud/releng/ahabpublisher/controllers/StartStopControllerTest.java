package com.google.cloud.releng.ahabpublisher.controllers;

import org.apache.coyote.Response;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StartStopControllerTest {

  @Autowired
  private TestRestTemplate webClient;

  @Test
  public void startRequest_starts() {
    ResponseEntity<String> response = webClient.postForEntity("/start", "", String.class);
    assertThat(response.getBody()).isEqualTo("Starting!\n");
    assertThat(response.getStatusCode().is2xxSuccessful());
  }

  @Test
  public void startRequest_reportsIfAlreadyStarted() {
    webClient.postForEntity("/start", "", String.class);
    ResponseEntity<String> response = webClient.postForEntity("/start", "", String.class);
    assertThat(response.getBody()).isEqualTo("Already started.\n");
    assertThat(response.getStatusCode().is2xxSuccessful());
  }

  @Test
  public void stopRequest_stops() {
    webClient.postForEntity("/start", "", String.class);
    ResponseEntity<String> response = webClient.postForEntity("/stop", "", String.class);
    assertThat(response.getBody()).isEqualTo("Stopping!\n");
    assertThat(response.getStatusCode().is2xxSuccessful());
  }

  @Test
  public void stopRequest_reportsIfAlreadyStopped() {
    ResponseEntity<String> response = webClient.postForEntity("/stop", "", String.class);
    assertThat(response.getBody()).isEqualTo("Already stopped.\n");
    assertThat(response.getStatusCode().is2xxSuccessful());
  }
}

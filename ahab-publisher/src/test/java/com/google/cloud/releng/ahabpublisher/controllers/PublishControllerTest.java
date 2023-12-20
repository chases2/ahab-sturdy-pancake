package com.google.cloud.releng.ahabpublisher.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PublishControllerTest {

  @Autowired
  private TestRestTemplate webClient;

  @Test
  public void postMessage_returnsSent() {
    ResponseEntity<String> response = webClient.postForEntity("/publishMessage?message=me", "", String.class);
    assertThat(response.getBody()).isEqualTo("Sent!\n");
    assertThat(response.getStatusCode().is2xxSuccessful());
  }

  @Test
  public void postWithNoMessage_returnsClientError() {
    ResponseEntity<String> response = webClient.postForEntity("/publishMessage", "", String.class);
    assertThat(response.getStatusCode().is4xxClientError());
  }

  @Test
  public void postWithQuantity_returnsSent() {
    ResponseEntity<String> response = webClient.postForEntity("/publishMessage?quantity=1000&message=me", "", String.class);
    assertThat(response.getBody()).isEqualTo("Sent!\n");
    assertThat(response.getStatusCode().is2xxSuccessful());
  }

  @Test
  public void postWithInvalidQuantity_returnsClientError() {
    ResponseEntity<String> response = webClient.postForEntity("/publishMessage?quantity=-1000&message=me", "", String.class);
    assertThat(response.getStatusCode().is4xxClientError());
  }

}

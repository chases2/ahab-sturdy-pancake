package com.example.ahab.pollingsub.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PingControllerTest {

  @Autowired
  private TestRestTemplate webClient;

  @Test
  public void ping_expectGenericGreeting() {
    ResponseEntity<String> response = webClient.getForEntity("/ping", String.class);
    assertThat(response.getBody()).isEqualTo("Hello, world!\n");
    assertThat(response.getStatusCode().is2xxSuccessful());
  }

  @Test
  public void pingWithGreeting_expectCustomGreeting() {
    ResponseEntity<String> response = webClient.getForEntity("/ping?name=there", String.class);
    assertThat(response.getBody()).isEqualTo("Hello, there!\n");
    assertThat(response.getStatusCode().is2xxSuccessful());
  }
}

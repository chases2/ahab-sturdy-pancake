package com.google.cloud.releng.ahabpublisher.serviceimplementations;

import static org.mockito.ArgumentMatchers.anyString;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import java.time.Duration;
import java.util.Random;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@ExtendWith(MockitoExtension.class)
@RunWith(SpringRunner.class)
@SpringBootTest
public class PublisherImplementationTest {
  @InjectMocks private PublisherImplementation publisher;
  @Mock private PubSubTemplate cloudPubSubProvider;

  // Use real, pre-seeded RNG. Exact seed is unimportant.
  @Spy private Random random = new Random(525600);

  @Test
  public void sendToPubsub_works(){
    publisher.sendToPubsub("test-message!");
    verify(cloudPubSubProvider).publish("ahab", "test-message!");
  }

  @Test
  public void startAndStop_sendsPeriodicMessages() {
    publisher.start();
    await()
        .atMost(Duration.ofSeconds(2))
        .untilAsserted(
            () -> {
              verify(cloudPubSubProvider, atLeastOnce()).publish(eq("ahab"), anyString());
            });

    publisher.stop();
    reset(cloudPubSubProvider);
    await().pollDelay(Duration.ofSeconds(2)).untilAsserted(() -> {
      verify(cloudPubSubProvider, never()).publish(anyString(), anyString());
    });

    publisher.start();
    await()
        .atMost(Duration.ofSeconds(2))
        .untilAsserted(
            () -> {
              verify(cloudPubSubProvider, atLeastOnce()).publish(eq("ahab"), anyString());
            });
  }
}

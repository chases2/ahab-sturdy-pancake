package com.example.ahab.pollingsub.serviceimplementations;

import com.google.common.base.Strings;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.util.Objects;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import com.google.cloud.spring.pubsub.reactive.PubSubReactiveFactory;
import reactor.core.Disposable;
import reactor.util.retry.Retry;
import com.google.cloud.spring.pubsub.support.AcknowledgeablePubsubMessage;

@Component
@RequiredArgsConstructor
@Slf4j
public class PollingListener {

  public static final String SUBSCRIPTION_ENV_KEY = "AHAB_SUBSCRIPTION";

  private final PubSubReactiveFactory reactiveFactory;

  /** A hook to cancel the current thread. */
  private Disposable ingestionThread;

  /**
   * Run the ingestion polling thread. This will cancel any existing thread already running.
   *
   * <p>This is run as a scheduled event when the application is ready, but we could make
   * it @Scheduled instead if the existing system is not robust enough. We could also maintain a
   * pool of multiple threads, maybe.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void runPolling() {
    if (Objects.nonNull(ingestionThread)) {
      log.info("runPolling-> disposing of old thread");
      ingestionThread.dispose();
    }

    final String subscription = System.getenv(SUBSCRIPTION_ENV_KEY);
    if (Strings.isNullOrEmpty(subscription)) {
      log.error("Subscription not set; will not subscribe");
      return;
    }

    ingestionThread =
        reactiveFactory
            .poll(subscription, 5000)
            .retryWhen(
                // exponential backoff up to retrying every day, retrying indefinitely
                Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(5))
                    .maxBackoff(Duration.ofDays(1))
                    .doBeforeRetry(
                        signal -> {
                          log.error(
                              "EventIngestionListener retry #{} after polling failure: {}",
                              signal.totalRetriesInARow() + 1,
                              signal.failure());
                        }))
            // form groups of <= 100, but don't wait more than 250ms to complete a group
            .bufferTimeout(100, Duration.ofMillis(250))
            .subscribe(this::pollStep);

    log.info("runPolling-> subscribed to {}", subscription);
  }

  private void pollStep(List<AcknowledgeablePubsubMessage> ackMessages) {
    log.info("Will ack {} messages", ackMessages.size());
    ackMessages.stream().forEach(msg -> {
      try {
        log.info("Acking pubsub message {}, id: {}",
            msg.getPubsubMessage().getData().toString("UTF-8"),
            msg.getPubsubMessage().getMessageId());
      } catch (UnsupportedEncodingException e) {
        log.error("You misspelled 'UTF-8', I guess: {}", e);
        return;
      }
      msg.ack();
    });
  }
}

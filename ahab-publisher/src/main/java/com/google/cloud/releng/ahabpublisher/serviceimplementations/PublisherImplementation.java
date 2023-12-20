package com.google.cloud.releng.ahabpublisher.serviceimplementations;

import com.google.cloud.releng.ahabpublisher.services.Publisher;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PublisherImplementation implements Publisher {

  private static final String TOPIC_NAME = "ahab";
  private static final long PUBLISH_PERIOD_IN_MILLISECONDS = 1000;
  private final PubSubTemplate pubSubTemplate;
  private final TaskScheduler periodicScheduler;

  private final Random random;
  private ScheduledFuture taskState;

  // This constructor creates its own TaskScheduler pool so that it doesn't have to deal with
  // differentiating between other TaskScheduler Beans floating out there, such as the PubSub ones.
  // Could have done this with qualified Beans, but didn't bother
  public PublisherImplementation(PubSubTemplate pubSubTemplate, Random random){
    this.pubSubTemplate = pubSubTemplate;
    this.random = random;
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.initialize();
    periodicScheduler = scheduler;
  }

  @Override
  public void sendToPubsub(@NonNull String message){
    log.debug("Sending Pub/Sub Message {}", message);
    pubSubTemplate.publish(TOPIC_NAME, message);
  }

  @Override
  public boolean start() {
    if (!Objects.isNull(this.taskState) && !this.taskState.isCancelled()) {
      return true;
    }
    this.taskState = periodicScheduler.scheduleAtFixedRate(this::sendRandomBurstOfTimestamps,
        PUBLISH_PERIOD_IN_MILLISECONDS);
    return false;
  }

  @Override
  public boolean stop() {
    if (Objects.isNull(this.taskState)) {
      return false;
    }
    if (this.taskState.isCancelled()) {
      return false;
    }
    taskState.cancel(false);
    return true;
  }

  public void sendTimestamp() {
    log.debug("tick");
    pubSubTemplate.publish(TOPIC_NAME, java.time.LocalDateTime.now().toString());
  }

  public void sendRandomBurstOfTimestamps() {
    final int numPackets = 1 + random.nextInt(20);
    final String timestamp = java.time.LocalDateTime.now().toString();
    log.debug("{} is sending {} packets", timestamp, numPackets);
    for (var i = 1; i < numPackets + 1; i++) {
      pubSubTemplate.publish(TOPIC_NAME, String.format("%s - %d/%d", timestamp, i, numPackets));
    }
  }

}

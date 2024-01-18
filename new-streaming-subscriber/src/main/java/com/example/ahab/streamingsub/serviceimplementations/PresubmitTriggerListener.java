package com.example.ahab.streamingsub.serviceimplementations;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.integration.AckMode;
import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.messaging.MessageChannel;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class PresubmitTriggerListener {
  public static final String SUBSCRIPTION_ENV_KEY = "AHAB_SUBSCRIPTION";

  // private final MetricsUtil metricsUtil; // Include??

  @Bean
  public MessageChannel presubmitExecutionsChannel() {
    return new DirectChannel();
  }

  @Bean
  public PubSubInboundChannelAdapter presubmitExecutionsAdapter(
      @Qualifier("presubmitExecutionsChannel") MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    final String subscription = System.getenv(SUBSCRIPTION_ENV_KEY);
    if (Objects.isNull(subscription)) {
      log.error("Subscription not set; will not subscribe");
      return null;
    }
    log.info("Subscribing to {}", subscription);

    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(
            pubSubTemplate, subscription);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    adapter.setPayloadType(String.class);
    return adapter;
  }

  @ServiceActivator(inputChannel = "presubmitExecutionsChannel")
  private void handler(
      String contents,
      @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage message) {
    log.info(
        "Got presubmitTriggerMessage: {}. Pubsub message id: {}",
        contents,
        message.getPubsubMessage().getMessageId());

    // NOTE(b/301515122): There _was_ a synchronous block here in presubmit_trigger.

    // try {
    //   metricsUtil
    //       .getTriggerLatencySloMetricTimer()
    //       .record(() -> presubmitService.startPresubmit(presubmitTriggerMessage));
    // } catch (Exception exception) {
    //   log.error(exception.getMessage());
    // }
    log.info("Acking pubsub message id: {}", message.getPubsubMessage().getMessageId());
    message.ack();
  }
}

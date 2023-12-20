package com.google.cloud.releng.ahabpublisher.services;

import org.springframework.lang.NonNull;

public interface Publisher {
  void sendToPubsub(@NonNull String message);

  boolean start();
  boolean stop();
}

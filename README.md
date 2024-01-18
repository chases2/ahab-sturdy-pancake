# Disconnection Example with Spring PubSub Library

If you boot a PubSub consumer under these circumstances...

- There are a lot of messages already on the queue
- "Exactly Once" delivery is enabled
- Your Spring Cloud GCP version is <5.0.0

... then the consumer will fail to consume with no errors, potentially for hours.

REFERENCE ISSUE: https://github.com/GoogleCloudPlatform/spring-cloud-gcp/issues/2491

## Instructions to Recreate

These instructions are for using GCP. This hasn't been tested on other cloud providers yet.

### (One-Time) Project Setup

You will need:

- A project to put all this in. My example is `slchase-canary`, which you will need to replace.
- A kubernetes cluster to put all these workloads in
- A topic, "ahab"
- Two to Four subscriptions:
  - "ahab.stream-1" is a default subscription with default settings.
  - "ahab.once-stream-1" is a subscription with "Exactly once delivery" enabled. Other settings are default
    - Note: by default, the "Ack Deadline" is increased from 10s -> 60s when enable exactly-once delivery.

Note: Other subscriptions are used for the variants in this repo; ex: "ahab.stream-21" and "ahab.once-stream-21"
for the Java 21 variant. This allows you to run them in parallel and compare.

Then, you should be able to deploy the publisher by running "skaffold run" in the "ahab-publisher" directory.
This publisher exists to publish messages; it is not part of the queue setup.

Give the service account permission to read and write from PubSub. All of these components use the same KSA.
Workload Identity is pretty good.

Finally, set up port forwarding to the publisher. You do not need to send commands to the subscribers.

### Queue Setup 

To recreate:

1. Put a bunch of messages on the queue. This can be done by sending this command to the publisher:

`curl -X POST -i "http://localhost:8080/publishMessage?quantity=100000&message=preseed-msg"`

1. Start publishing messages to the queues.

`curl -X POST -i "http://localhost:8080/start"`

1. Now that there are messages on the queue, run "skaffold run" in the "streaming-subscriber" directory. This will
deploy two copies of the same binary, each subscribed to one of the subscriptions.

1. Observe the queues. "ahab.stream-1" should drain quickly, while "ahab.once-stream-1" will not.

1. Observe the logs. One will be acking messages (noted at "info" level); the other has stopped with no errors.

1. When you're done, stop publishing messages to the queues.

`curl -X POST -i "http://localhost:8080/stop"`

## Known Resolutions

- Update your Spring Cloud GCP version to 5.0.0 or later. This project demonstrates that the error doesn't occur at this version.
- Remove "Exactly Once" delivery. This project demonstrates that the error doesn't occur if "Exactly Once" delivery is disabled.
- Use a Polling Subscripton base. Example code is in "polling-subscription"
- Restarting the service often fixes the issue. Configure health checks to reboot your service if the queue climbs regularly.

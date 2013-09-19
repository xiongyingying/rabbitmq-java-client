package com.rabbitmq.client.test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;
import java.util.concurrent.*;

public class DuplicateQueueingConsumerTests extends BrokerTestCase {

    public void test3ConsumersWithInterleavingCancelNotifications() throws Exception {
        final Channel channel = connection.createChannel();
        final QueueingConsumer consumer = new QueueingConsumer(channel);
        final String queue = channel.queueDeclare().getQueue();

        channel.basicConsume(queue, consumer);
        final BlockingQueue<Exception> result =
                new LinkedBlockingQueue<Exception>(1);

        new Runnable() {
            @Override
            public void run() {
                try {
                    channel.basicConsume(queue, consumer);
                    consumer.nextDelivery();
                } catch (Exception e) {
                    result.add(e);
                }
            }
        }.run();

        final Exception ex = result.poll(5, TimeUnit.SECONDS);
        final QueueingConsumer.DuplicateQueueingConsumerException actualException =
                (QueueingConsumer.DuplicateQueueingConsumerException) ex;

        final String originalConsumerTag = actualException.getOriginalConsumerTag();
        final String duplicateConsumerTag = actualException.getDuplicateConsumerTag();

        assertFalse(originalConsumerTag.equals(duplicateConsumerTag));

    }

}

package com.sweelam.rmq.consumer;

import static com.sweelam.rmq.config.AmqpConfig.*;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.sweelam.rmq.config.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class RmqConsumer {

	public static void main(String[] args) throws IOException, TimeoutException {
		var amqp = new AMQP();

		amqp.connect(USERNAME, PASSWORD, 5672);
		Channel channel = amqp.getChannel();

		ExecutorService ex = Executors.newFixedThreadPool(5);

		for (int i = 0; i < 5; i++) {
			ex.submit(() -> {
				consumerMessage(channel);
			});
		}

	}

	private static void consumerMessage(Channel channel) {
		try {
			var tag = channel.basicConsume(QUEUE_NAME, false, "tagTest-" + UUID.randomUUID(), 
					new DefaultConsumer(channel) {

				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties,
						byte[] body) throws IOException {
					
					var mssg = new String(body);
					System.out.println(String.format("[Thread # %d] with mssg %s", Thread.currentThread().getId(), mssg));
					
					
//					var mssgArr = mssg.split(" ");
//					if (mssgArr[2].charAt(0) == '3') {
//						System.out.println(String.format("Negative Ack for mssg %s", mssg));
////						channel.basicNack(envelope.getDeliveryTag(), false, false);
//					} else {
//						channel.basicAck(envelope.getDeliveryTag(), true);
//					}
					
					
					channel.basicAck(envelope.getDeliveryTag(), true);
					
				}

			});
			
			System.out.println("============&&&&&&&&&&************================");
			System.out.println(tag);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

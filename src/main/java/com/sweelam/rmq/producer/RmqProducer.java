package com.sweelam.rmq.producer;

import static com.sweelam.rmq.config.AmqpConfig.*;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.BuiltinExchangeType;
import com.sweelam.rmq.config.AMQP;
import com.sweelam.rmq.config.ExchangeBuilder;

public class RmqProducer {
	private static AMQP amqp = new AMQP();

	public static void main(String[] args) throws Exception {

		try {
			amqp.connect(USERNAME, PASSWORD, 5672);
			amqp.enableDeliveryAck();
		} catch (Exception e) {
			e.getStackTrace();
		}

		
		
		String DEAD_LETTER_EXCHANGE = EXCHANGE + "_dead_letter";
		var exchange = ExchangeBuilder.builder()
				.exchangeName(DEAD_LETTER_EXCHANGE)
				.type(BuiltinExchangeType.FANOUT.name().toLowerCase());
		
		amqp.createInfra(
				exchange,
				QUEUE_NAME + "_dlx", 
				Map.of("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE,"x-message-ttl", 60000),
				ROUTING_K);
		
		
		var mainExhange = ExchangeBuilder.builder()
				.exchangeName(EXCHANGE)
				.type(BuiltinExchangeType.DIRECT.name().toLowerCase());
		
		amqp.createInfra(mainExhange, QUEUE_NAME, 
				Map.of("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE), ROUTING_K);
		amqp.getChannel().queueBind(QUEUE_NAME, DEAD_LETTER_EXCHANGE, ROUTING_K);
		
		sendMessage();

		System.exit(0);

	}

	private static void sendMessage() throws IOException, InterruptedException, TimeoutException {
		var channel = amqp.getChannel();
		for (int i = 0; i < 1000; i++) {
			channel.basicPublish(
					EXCHANGE, ROUTING_K, 
					new BasicProperties()
					.builder()
					.deliveryMode(2)
					.contentType("text/plain")
					.timestamp(Date.from(Instant.now()))
					.build(),
					new String("Test Message " + i + " from Sweelam").getBytes());

			channel.waitForConfirmsOrDie(50l);
		}
	}

}

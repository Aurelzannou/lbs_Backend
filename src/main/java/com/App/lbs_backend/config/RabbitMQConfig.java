package com.App.lbs_backend.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String NOTIFICATION_EXCHANGE = "lbs.notification.exchange";
    public static final String EMAIL_QUEUE = "lbs.email.notification.queue";
    public static final String EMAIL_DLQ = "lbs.email.notification.dlq";
    public static final String EMAIL_ROUTING_KEY = "email.dossier.notification";

    public static final String PROFESSEUR_ACTIVATION_QUEUE = "lbs.professeur.activation.queue";
    public static final String PROFESSEUR_ACTIVATION_DLQ = "lbs.professeur.activation.dlq";
    public static final String PROFESSEUR_ACTIVATION_ROUTING_KEY = "email.professeur.activation";

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(NOTIFICATION_EXCHANGE, true, false);
    }

    @Bean
    public Queue emailNotificationDlq() {
        return QueueBuilder.durable(EMAIL_DLQ).build();
    }

    @Bean
    public Queue emailNotificationQueue() {
        return QueueBuilder.durable(EMAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", EMAIL_DLQ)
                .build();
    }

    @Bean
    public Binding emailNotificationBinding(Queue emailNotificationQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(emailNotificationQueue).to(notificationExchange).with(EMAIL_ROUTING_KEY);
    }

    @Bean
    public Queue professeurActivationDlq() {
        return QueueBuilder.durable(PROFESSEUR_ACTIVATION_DLQ).build();
    }

    @Bean
    public Queue professeurActivationQueue() {
        return QueueBuilder.durable(PROFESSEUR_ACTIVATION_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", PROFESSEUR_ACTIVATION_DLQ)
                .build();
    }

    @Bean
    public Binding professeurActivationBinding(Queue professeurActivationQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(professeurActivationQueue).to(notificationExchange).with(PROFESSEUR_ACTIVATION_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}

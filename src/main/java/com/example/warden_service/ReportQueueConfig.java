package com.example.warden_service;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReportQueueConfig {

    @Bean
    Queue reportQueue(@Value("${report.queue.name}") String queueName) {
        return new Queue(queueName, true);
    }
}

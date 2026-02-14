package com.clinica.auditoria.infrastructure.config;

import com.clinica.shared.config.RabbitMQConfig;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditoriaRabbitConfig {

    @Value("${auditoria.queue.name:clinica.auditoria.queue}")
    private String queueName;

    @Bean
    public Queue auditoriaQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding bindingAuth(Queue auditoriaQueue, TopicExchange clinicaEventsExchange) {
        return BindingBuilder.bind(auditoriaQueue)
                .to(clinicaEventsExchange)
                .with("auth.#");
    }

    @Bean
    public Binding bindingHistoria(Queue auditoriaQueue, TopicExchange clinicaEventsExchange) {
        return BindingBuilder.bind(auditoriaQueue)
                .to(clinicaEventsExchange)
                .with("historia.#");
    }

    @Bean
    public Binding bindingTranscripcion(Queue auditoriaQueue, TopicExchange clinicaEventsExchange) {
        return BindingBuilder.bind(auditoriaQueue)
                .to(clinicaEventsExchange)
                .with("transcripcion.#");
    }

    @Bean
    public Binding bindingExportacion(Queue auditoriaQueue, TopicExchange clinicaEventsExchange) {
        return BindingBuilder.bind(auditoriaQueue)
                .to(clinicaEventsExchange)
                .with("exportacion.#");
    }
}

package com.clinica.shared.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "clinica.events";

    public static final String ROUTING_AUTH_PREFIX = "auth.usuario.";
    public static final String ROUTING_TRANSCRIPCION_COMPLETADA = "transcripcion.completada";
    public static final String ROUTING_HISTORIA_PREFIX = "historia.";
    public static final String ROUTING_EXPORTACION_PREFIX = "exportacion.";

    @Bean
    public TopicExchange clinicaEventsExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

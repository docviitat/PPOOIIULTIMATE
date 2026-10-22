package com.PPOOII.Laboratorio.Config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Configuration
@ConditionalOnProperty(name="mensajeria.enabled",havingValue="true",matchIfMissing=true)
public class MensajeriaConfig {
    @Bean
    Queue mensajesQueue(@Value("${mensajeria.cola:ppooii.mensajes}") String nombre) {
        return new Queue(nombre,true,false,false);
    }
}

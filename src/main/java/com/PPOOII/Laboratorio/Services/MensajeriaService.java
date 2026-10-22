package com.PPOOII.Laboratorio.Services;

import java.util.*;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** Productor y consumidor independientes, unidos exclusivamente por una cola durable. */
@Service
@ConditionalOnProperty(name="mensajeria.enabled",havingValue="true",matchIfMissing=true)
public class MensajeriaService {
    private final RabbitTemplate rabbit;
    private final AmqpAdmin admin;
    private final JdbcTemplate jdbc;
    private final RabbitListenerEndpointRegistry registry;
    private final String cola;
    public MensajeriaService(RabbitTemplate rabbit,AmqpAdmin admin,JdbcTemplate jdbc,
        RabbitListenerEndpointRegistry registry,@Value("${mensajeria.cola:ppooii.mensajes}") String cola) {
        this.rabbit=rabbit; this.admin=admin; this.jdbc=jdbc; this.registry=registry; this.cola=cola;
    }
    public Map<String,String> publicar(String contenido) {
        if(Validaciones.vacio(contenido) || contenido.length()>10000)
            throw Validaciones.error(HttpStatus.BAD_REQUEST,"El contenido es obligatorio y admite hasta 10000 caracteres.");
        String id=UUID.randomUUID().toString();
        try {
            rabbit.invoke(ops -> {
                ops.convertAndSend("",cola,contenido,message -> {
                    message.getMessageProperties().setMessageId(id);
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return message;
                });
                ops.waitForConfirmsOrDie(5000); return null;
            });
        } catch(AmqpException e) { throw Validaciones.error(HttpStatus.SERVICE_UNAVAILABLE,"RabbitMQ no confirmó el mensaje. Consulte la cola antes de reintentar."); }
        return Map.of("id",id,"estado","PUBLICADO");
    }
    @RabbitListener(id="consumidorPpooii",queues="${mensajeria.cola:ppooii.mensajes}",
        autoStartup="${mensajeria.consumidor.enabled:true}")
    public void recibir(Message message) {
        // Se confirma al broker solo al terminar este método. Si la BD falla, se reencola.
        String id=message.getMessageProperties().getMessageId();
        if(id==null) throw new org.springframework.amqp.AmqpRejectAndDontRequeueException("Falta messageId");
        jdbc.update("INSERT INTO mensaje_recibido(id,contenido) VALUES(?,?) ON DUPLICATE KEY UPDATE id=VALUES(id)",
            id,new String(message.getBody(),java.nio.charset.StandardCharsets.UTF_8));
    }
    public List<Map<String,Object>> recibidos() {
        return jdbc.queryForList("SELECT id,contenido,recibido FROM mensaje_recibido ORDER BY recibido DESC,id LIMIT 100");
    }
    public Map<String,Object> estado() {
        try {
            var info=admin.getQueueInfo(cola);
            if(info==null) throw new AmqpException("Cola inexistente");
            return Map.of("cola",cola,"pendientes",info.getMessageCount(),"consumidores",info.getConsumerCount(),
                "consumidorActivo",registry.getListenerContainer("consumidorPpooii").isRunning());
        } catch(AmqpException e) { throw Validaciones.error(HttpStatus.SERVICE_UNAVAILABLE,"RabbitMQ no disponible."); }
    }
    public void activar(boolean activo) {
        var container=registry.getListenerContainer("consumidorPpooii");
        if(activo) container.start(); else container.stop();
    }
}

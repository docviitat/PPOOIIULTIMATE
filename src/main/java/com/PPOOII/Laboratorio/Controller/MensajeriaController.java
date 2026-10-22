package com.PPOOII.Laboratorio.Controller;

import java.util.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.PPOOII.Laboratorio.Services.MensajeriaService;
import com.PPOOII.Laboratorio.Services.Validaciones;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/LaboratorioV4/mensajes")
@Tag(name="RabbitMQ",description="Prueba de concepto: productor, consumidor y monitoreo independientes")
@SecurityRequirement(name="bearerAuth")
@ConditionalOnProperty(name="mensajeria.enabled",havingValue="true",matchIfMissing=true)
public class MensajeriaController {
    private final MensajeriaService servicio;
    public MensajeriaController(MensajeriaService servicio) { this.servicio=servicio; }
    @Schema(description="Mensaje que se publica en RabbitMQ")
    public record Publicacion(@Schema(description="Texto del mensaje",example="Trayecto registrado") String contenido) {}
    @Schema(description="Control del consumidor independiente del productor")
    public record Control(@Schema(description="true inicia; false detiene el consumidor") Boolean activo) {}
    @PostMapping
    @Operation(summary="Publica un mensaje persistente, incluso con el consumidor detenido")
    public ResponseEntity<Map<String,String>> publicar(@RequestBody Publicacion body) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(servicio.publicar(body.contenido()));
    }
    @GetMapping("/recibidos")
    @Operation(summary="Consulta los últimos 100 mensajes consumidos y guardados")
    public List<Map<String,Object>> recibidos() { return servicio.recibidos(); }
    @GetMapping("/estado")
    @Operation(summary="Monitorea mensajes pendientes y consumidores en RabbitMQ")
    public Map<String,Object> estado() { return servicio.estado(); }
    @PutMapping("/consumidor")
    @Operation(summary="Detiene o inicia el consumidor para demostrar el desacoplamiento")
    public Map<String,Boolean> consumidor(@RequestBody Control control) {
        if(control.activo()==null) throw Validaciones.error(HttpStatus.BAD_REQUEST,"activo es obligatorio.");
        servicio.activar(control.activo()); return Map.of("activo",control.activo());
    }
}

package com.PPOOII.Laboratorio.Services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CargueIdService {
    private final JdbcTemplate jdbc;
    public CargueIdService(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    public long reservar(String tipo) {
        long id=Long.parseLong(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")));
        try { jdbc.update("INSERT INTO cargue_archivo(tipo,id_cargue) VALUES (?,?)",tipo,id); }
        catch (DuplicateKeyException e) {
            throw Validaciones.error(HttpStatus.CONFLICT,
                "Ya existe un cargue de " + tipo + " en este minuto. Espere al siguiente minuto para enviar otro archivo.");
        }
        return id;
    }
}

package com.PPOOII.Laboratorio.Services.Interfaces;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.PPOOII.Laboratorio.Entities.Coordenadas;

public interface ICoordenadasService {

	List<Coordenadas> consultarAllCoordenadas(Pageable pageable);
}

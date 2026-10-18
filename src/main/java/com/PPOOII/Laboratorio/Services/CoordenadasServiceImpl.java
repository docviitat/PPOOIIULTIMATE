package com.PPOOII.Laboratorio.Services;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.PPOOII.Laboratorio.Entities.Coordenadas;
import com.PPOOII.Laboratorio.Repository.CoordenadasRepository;
import com.PPOOII.Laboratorio.Services.Interfaces.ICoordenadasService;

@Service("CoordenadasService")
public class CoordenadasServiceImpl implements ICoordenadasService{

	// ========= INYECCIÓN DE DEPENDENCIAS ==========
	@Autowired
	@Qualifier("ICoordenadasRepository")
	private CoordenadasRepository ICoordenadaRepository;
	//==================== LOGS ============================
	//LOGS DE ERROR
	private static final Logger logger = org.apache.logging.log4j.LogManager.getLogger(PersonaServiceImpl.class);
		
	@Override
	public List<Coordenadas> consultarAllCoordenadas(Pageable pageable) {
		return  ICoordenadaRepository.findAll(pageable).getContent();
	}

}

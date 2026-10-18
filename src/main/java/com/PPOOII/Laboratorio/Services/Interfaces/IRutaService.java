package com.PPOOII.Laboratorio.Services.Interfaces;

import java.util.List;

import com.PPOOII.Laboratorio.DTO.RutaDTO;
import com.PPOOII.Laboratorio.DTO.RutasConductorDTO;
import com.PPOOII.Laboratorio.DTO.RutasVehiculoDTO;
import com.PPOOII.Laboratorio.Entities.Trayecto;

public interface IRutaService {
	RutaDTO consultarRuta(String codigoRuta);
	RutasConductorDTO consultarRutasPorConductor(String identificacion);
	RutasVehiculoDTO consultarRutasPorVehiculo(String placa);
	List<RutaDTO> consultarRutasConRestricciones();
	Trayecto crearTrayecto(Trayecto trayecto);
}

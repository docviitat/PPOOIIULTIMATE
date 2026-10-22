package com.PPOOII.Laboratorio.Services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.PPOOII.Laboratorio.DTO.RutaDTO;
import com.PPOOII.Laboratorio.DTO.RutasConductorDTO;
import com.PPOOII.Laboratorio.DTO.RutasVehiculoDTO;
import com.PPOOII.Laboratorio.DTO.TrayectoRutaDTO;
import com.PPOOII.Laboratorio.Entities.Trayecto;
import com.PPOOII.Laboratorio.Exception.CargueException;
import com.PPOOII.Laboratorio.Repository.RutaRepository;
import com.PPOOII.Laboratorio.Repository.TrayectoRepository;
import com.PPOOII.Laboratorio.Services.Interfaces.IRutaService;

@Service("RutaService")
public class RutaServiceImpl implements IRutaService {

	@Autowired @Qualifier("IRutaRepository") private RutaRepository rutaRepository;
	@Autowired @Qualifier("ITrayectoRepository") private TrayectoRepository trayectoRepository;

	@Override
	public RutaDTO consultarRuta(String codigoRuta) {
		List<TrayectoRutaDTO> trayectos = rutaRepository.trayectosDeRuta(codigoRuta);
		if (trayectos.isEmpty()) {
			throw new CargueException(HttpStatus.NOT_FOUND, "No existe la ruta con código " + codigoRuta + ".");
		}
		return new RutaDTO(codigoRuta, trayectos);
	}

	@Override
	public RutasConductorDTO consultarRutasPorConductor(String identificacion) {
		if (!rutaRepository.existeConductor(identificacion)) {
			throw new CargueException(HttpStatus.NOT_FOUND, "No existe un conductor con identificación " + identificacion + ".");
		}
		RutasConductorDTO r = new RutasConductorDTO();
		r.identificacion = identificacion;
		r.nombre = rutaRepository.nombreConductor(identificacion);
		r.codigosRuta = rutaRepository.codigosRutaDeConductor(identificacion);
		return r;
	}

	@Override
	public RutasVehiculoDTO consultarRutasPorVehiculo(String placa) {
		if (!rutaRepository.existeVehiculo(placa)) {
			throw new CargueException(HttpStatus.NOT_FOUND, "No existe un vehículo con placa " + placa + ".");
		}
		Map<String, RutasVehiculoDTO.RutaConductores> porRuta = new LinkedHashMap<>();
		for (String[] f : rutaRepository.rutasYConductoresDeVehiculo(placa)) {
			RutasVehiculoDTO.RutaConductores rc = porRuta.computeIfAbsent(f[0], k -> {
				RutasVehiculoDTO.RutaConductores n = new RutasVehiculoDTO.RutaConductores();
				n.codigoRuta = k;
				n.conductores = new ArrayList<>();
				return n;
			});
			RutasVehiculoDTO.Conductor c = new RutasVehiculoDTO.Conductor();
			c.identificacion = f[1];
			c.nombre = f[2];
			rc.conductores.add(c);
		}
		RutasVehiculoDTO r = new RutasVehiculoDTO();
		r.placa = placa;
		r.rutas = new ArrayList<>(porRuta.values());
		return r;
	}

	@Override
	public List<RutaDTO> consultarRutasConRestricciones() {
		Map<String, List<TrayectoRutaDTO>> porRuta = new LinkedHashMap<>();
		for (Object[] fila : rutaRepository.trayectosRestringidos()) {
			TrayectoRutaDTO t = (TrayectoRutaDTO) fila[1];
			List<String> motivos = new ArrayList<>();
			if (!t.vehiculoHabilitado) motivos.add("Vehículo NO habilitado (documentos no habilitados o vehículo inexistente)");
			if ("RO".equals(t.estadoConductor)) motivos.add("Conductor Restringido para Operar (RO)");
			t.motivo = String.join("; ", motivos);
			porRuta.computeIfAbsent((String) fila[0], k -> new ArrayList<>()).add(t);
		}
		List<RutaDTO> rutas = new ArrayList<>();
		porRuta.forEach((codigo, ts) -> rutas.add(new RutaDTO(codigo, ts)));
		return rutas;
	}

	/** Un trayecto solo se crea si los documentos del vehículo están HABILITADO y el conductor puede operar (PO). */
	@Override
	@org.springframework.transaction.annotation.Transactional
	public Trayecto crearTrayecto(Trayecto t) {
		if (t == null || blank(t.getCodigoRuta()) || t.getOrden() == null || t.getOrden() < 0 || blank(t.getUbicacion())
				|| blank(t.getIdentificacionConductor()) || blank(t.getPlaca())) {
			throw new CargueException(HttpStatus.BAD_REQUEST,
					"codigoRuta, orden (>= 0), ubicacion, identificacionConductor y placa son obligatorios.");
		}
		if (t.getCodigoRuta().trim().length()>30 || t.getUbicacion().trim().length()>255 || t.getOrden()>99999)
            throw new CargueException(HttpStatus.BAD_REQUEST,"Código de ruta, ubicación u orden exceden la longitud permitida.");
        if ((t.getLatitud()==null)!=(t.getLongitud()==null)
            || (t.getLatitud()!=null && (!Double.isFinite(t.getLatitud()) || Math.abs(t.getLatitud())>90))
            || (t.getLongitud()!=null && (!Double.isFinite(t.getLongitud()) || Math.abs(t.getLongitud())>180)))
            throw new CargueException(HttpStatus.BAD_REQUEST,"Envíe ambas coordenadas válidas o ninguna.");
        t.setId(null);
		t.setCodigoRuta(t.getCodigoRuta().trim());
		t.setPlaca(t.getPlaca().trim().toUpperCase());
		t.setIdentificacionConductor(t.getIdentificacionConductor().trim());
		if (!rutaRepository.existeVehiculo(t.getPlaca())) {
			throw new CargueException(HttpStatus.BAD_REQUEST, "El vehículo con placa " + t.getPlaca() + " no existe.");
		}
		if (!rutaRepository.vehiculoConDocumentosHabilitados(t.getPlaca())) {
			throw new CargueException(HttpStatus.CONFLICT, "El vehículo tiene documentos que no están en estado Habilitado.");
		}
		if (!rutaRepository.existeConductor(t.getIdentificacionConductor())) {
			throw new CargueException(HttpStatus.BAD_REQUEST, "El conductor no existe o la persona no es de tipo Conductor.");
		}
		if (!rutaRepository.conductorPuedeOperar(t.getIdentificacionConductor(), t.getPlaca())) {
			throw new CargueException(HttpStatus.CONFLICT, "El conductor no tiene estado PO (Puede Operar) con este vehículo.");
		}
		if (rutaRepository.existeTrayecto(t.getCodigoRuta(), t.getOrden())) {
			throw new CargueException(HttpStatus.CONFLICT, "Ya existe un trayecto con ese código de ruta y orden.");
		}
		t.setIdPersona(rutaRepository.idConductor(t.getIdentificacionConductor()));
		t.setIdVehiculo(rutaRepository.idVehiculo(t.getPlaca()));
		String login = "api";
		org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.getName() != null) login = auth.getName();
		t.setLoginRegistro(login);
		return trayectoRepository.save(t);
	}

	private boolean blank(String s) { return s == null || s.trim().isEmpty(); }
}

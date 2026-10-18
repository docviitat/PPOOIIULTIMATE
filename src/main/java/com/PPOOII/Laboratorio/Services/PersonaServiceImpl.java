package com.PPOOII.Laboratorio.Services;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.PPOOII.Laboratorio.Entities.Persona;
import com.PPOOII.Laboratorio.Repository.PersonaRepository;
import com.PPOOII.Laboratorio.Services.Interfaces.IPersonaService;

@Service("PersonaService")
public class PersonaServiceImpl implements IPersonaService{
	// ========= INYECCIÓN DE DEPENDENCIAS ==========
	@Autowired
	@Qualifier("IPersonaRepository")
	private PersonaRepository IPersonaRepository;
	//==================== LOGS ============================
	//LOGS DE ERROR
	private static final Logger logger = org.apache.logging.log4j.LogManager.getLogger(PersonaServiceImpl.class);

	// ========= Entrega 2: persona + usuario automatico =========
	@Autowired
	private UsuarioGestionService usuarioGestion;

	@Override
	@org.springframework.transaction.annotation.Transactional
	public com.PPOOII.Laboratorio.DTO.PersonaCreadaDTO crear(Persona p) {
		normalizar(p);
		if (IPersonaRepository.existsByIdentificacion(p.getIdentificacion())) {
			throw Validaciones.error(org.springframework.http.HttpStatus.CONFLICT, "Ya existe una persona con la identificación " + p.getIdentificacion() + ".");
		}
		p.setId(0);
		Persona g = IPersonaRepository.save(p);
		com.PPOOII.Laboratorio.DTO.PersonaCreadaDTO r = new com.PPOOII.Laboratorio.DTO.PersonaCreadaDTO();
		r.persona = g;
		// Una persona ADMINISTRATIVO nunca queda sin usuario (si falla, se revierte todo)
		if ("A".equals(g.getTipoPersona())) r.usuario = usuarioGestion.crearParaPersona(g);
		return r;
	}

	@Override
	@org.springframework.transaction.annotation.Transactional
	public com.PPOOII.Laboratorio.DTO.PersonaCreadaDTO modificar(Persona p) {
		if (p == null || p.getId() == 0) {
			throw Validaciones.error(org.springframework.http.HttpStatus.BAD_REQUEST, "El id de la persona es obligatorio y distinto de 0.");
		}
		Persona actual = IPersonaRepository.findById(p.getId());
		if (actual == null) {
			throw Validaciones.error(org.springframework.http.HttpStatus.NOT_FOUND, "No existe la persona con id " + p.getId() + ".");
		}
		normalizar(p);
		Persona otra = IPersonaRepository.findByIdentificacion(p.getIdentificacion());
		if (otra != null && otra.getId() != p.getId()) {
			throw Validaciones.error(org.springframework.http.HttpStatus.CONFLICT, "Ya existe otra persona con la identificación " + p.getIdentificacion() + ".");
		}
		if (p.getLicencia() == null) p.setLicencia(actual.getLicencia()); // no borrar la licencia si no se envía
		Persona g = IPersonaRepository.save(p);
		com.PPOOII.Laboratorio.DTO.PersonaCreadaDTO r = new com.PPOOII.Laboratorio.DTO.PersonaCreadaDTO();
		r.persona = g;
		if ("A".equals(g.getTipoPersona()) && !usuarioGestion.tieneUsuario(g.getId())) {
			r.usuario = usuarioGestion.crearParaPersona(g);
		}
		return r;
	}

	/** Valida y normaliza los campos de la persona (tipo de identificación y tipo de persona). */
	private void normalizar(Persona p) {
		if (p == null) throw Validaciones.error(org.springframework.http.HttpStatus.BAD_REQUEST, "El cuerpo es obligatorio.");
		if (Validaciones.vacio(p.getIdentificacion()) || Validaciones.vacio(p.getNombres())
				|| Validaciones.vacio(p.getApellidos()) || Validaciones.vacio(p.getCorreo())) {
			throw Validaciones.error(org.springframework.http.HttpStatus.BAD_REQUEST, "identificacion, nombres, apellidos y correo son obligatorios.");
		}
		p.setIdentificacion(p.getIdentificacion().trim());
		String ti = Validaciones.canon(p.getTipoIdentificacion(), "CC", "CE", "PAS");
		if (ti == null) throw Validaciones.error(org.springframework.http.HttpStatus.BAD_REQUEST, "tipoIdentificacion debe ser CC, CE o PAS.");
		p.setTipoIdentificacion(ti);
		String tp = null;
		if (Validaciones.canon(p.getTipoPersona(), "C", "CONDUCTOR") != null) tp = "C";
		else if (Validaciones.canon(p.getTipoPersona(), "A", "ADMINISTRATIVO") != null) tp = "A";
		if (tp == null) throw Validaciones.error(org.springframework.http.HttpStatus.BAD_REQUEST, "tipoPersona debe ser CONDUCTOR (C) o ADMINISTRATIVO (A).");
		p.setTipoPersona(tp);
		if (!p.getCorreo().contains("@")) throw Validaciones.error(org.springframework.http.HttpStatus.BAD_REQUEST, "El correo no es válido.");
		if (p.getEdad() != null && (p.getEdad() < 0 || p.getEdad() > 120)) {
			throw Validaciones.error(org.springframework.http.HttpStatus.BAD_REQUEST, "La edad no es válida.");
		}
		if (Validaciones.vacio(p.getPnombre())) p.setPnombre(p.getNombres().trim().split("\\s+")[0]);
	}

	//INSERT
	@Override
	public boolean guardar(Persona persona) {
		try {
			if (persona == null) {
				logger.error("ERROR AGREGAR_PERSONA: LA PERSONA ES NULO!");
				return false;				
			}
			else {
				IPersonaRepository.save(persona);
				return true;
			}
		}catch(Exception e) {
			logger.error("ERROR AGREGAR_PERSONA: LA PERSONA NO SE HA GUARDADO!");
			return false;
		}
	}
	//UPDATE
	@Override
	public boolean actualizar(Persona persona) {
		try {
			if ((persona == null) || (persona.getId() == 0)) {
				logger.error("ERROR EDITAR_PERSONA:  LA PERSONA ES NULO O EL ID ES 0!");		
				return false;
			}
			else {	
				IPersonaRepository.save(persona);
				return true;
			}
		}catch(Exception e) {
			logger.error("ERROR EDITAR_PERSONA: LA PERSONA NO SE HA EDITADO!");		
			return false;
		}
	}
	//DELETE
	@Override
	public boolean eliminar(int id) {
		try {
			if ((id == 0)) {
				logger.error("ERROR ELIMINAR_PERSONA: EL ID DEL PERSONA ES 0!");
				return false;
			}
			else {
				Persona persona = IPersonaRepository.findById(id);
				IPersonaRepository.delete(persona);
				return true;
			}
		}catch(Exception e) {
			logger.error("ERROR ELIMINAR_PERSONA: LA PERSONA NO SE HA ELIMINADO!");
			return false;
		} 
	}
	//LISTA DE PRODUCTOS
	@Override
	public List<Persona> consultarPersona(Pageable pageable) {
		return  IPersonaRepository.findAll(pageable).getContent(); 
	}

	//================ METODOS DE BUSQUEDA =============================
	//PERSONA POR ID | VALOR UNICO
	@Override
	public Persona findById(int id) {
		return IPersonaRepository.findById(id);
	}

	//LISTA DE PERSONAS POR NOMBRE
	@Override
	public List<Persona> findByNombre(String pnombre) {
		return IPersonaRepository.findByPnombre(pnombre); 
	}

	//LISTA DE PERSONAS POR EDAD
	@Override
	public List<Persona> findByEdad(int edad) {
		return IPersonaRepository.findByEdad(edad); 
	}

}

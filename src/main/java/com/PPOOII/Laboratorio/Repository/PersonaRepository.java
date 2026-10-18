package com.PPOOII.Laboratorio.Repository;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.PPOOII.Laboratorio.Entities.Persona;
import com.PPOOII.Laboratorio.Entities.Usuario;

@Repository("IPersonaRepository")
@org.springframework.data.rest.core.annotation.RepositoryRestResource(exported = false)
public interface PersonaRepository extends JpaRepository<Persona, Integer>, CrudRepository<Persona, Integer>{
	
	//Hay Métodos que JPA ya los tiene desarrollados, se pueden crear para tener
	//una manipulación más especifica a la hora de usarlos en el service	

	public abstract Persona findById(int id);

	public abstract boolean existsByIdentificacion(String identificacion);

	public abstract Persona findByIdentificacion(String identificacion);

	public abstract List<Persona> findByPnombre(String pnombre);

	public abstract List<Persona> findByEdad(int edad);

	public abstract Page<Persona> findAll(Pageable pageable);

	@Query("SELECT per FROM persona per WHERE per.ubicacion is not null")
	public abstract List<Persona> getPersonas();
	
	@Transactional
	@Procedure(procedureName = "CARGAR_DATOS_EXCEL")
	public abstract void CARGAR_DATOS_EXCEL(String pnombre, String edad, String ubicacion, Double idCargue);
	
	@Transactional
	@Procedure(procedureName = "VALIDAR_DATOS_CARGUE")
	public abstract void VALIDAR_DATOS_CARGUE(Double idCargue);
	
	@Transactional
	@Procedure(procedureName = "PROCESAR_DATOS_EXCEL")
	public abstract void PROCESAR_DATOS_EXCEL(Double idCargue);

}

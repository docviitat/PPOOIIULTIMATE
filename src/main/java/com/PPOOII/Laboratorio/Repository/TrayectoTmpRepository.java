package com.PPOOII.Laboratorio.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.PPOOII.Laboratorio.Entities.TrayectoTmp;

/**
 * Repositorio de la entidad temporal. Invoca los tres procedimientos
 * almacenados del cargue de trayectos. No se expone por Spring Data REST.
 */
@RepositoryRestResource(exported = false)
@Repository("ITrayectoTmpRepository")
public interface TrayectoTmpRepository extends JpaRepository<TrayectoTmp, Long> {

	// ---------- Procedimientos almacenados ----------
	@Transactional
	@Procedure(procedureName = "CARGAR_TRAYECTOS_EXCEL")
	void CARGAR_TRAYECTOS_EXCEL(String codigoRuta, String orden, String ubicacion,
			String identificacionConductor, String placa, String nombreConductor, String edadConductor, String loginRegistro, Long idCargue);

	@Transactional
	@Procedure(procedureName = "VALIDAR_TRAYECTOS_CARGUE")
	void VALIDAR_TRAYECTOS_CARGUE(Long idCargue);

	@Transactional
	@Procedure(procedureName = "PROCESAR_TRAYECTOS_EXCEL")
	void PROCESAR_TRAYECTOS_EXCEL(Long idCargue, String login);

	// ---------- Consultas de cargues ----------
	boolean existsByIdCargue(Long idCargue);

	Page<TrayectoTmp> findByIdCargue(Long idCargue, Pageable pageable);

	@Query("SELECT t.estado, COUNT(t) FROM TrayectoTmp t WHERE t.idCargue = :idCargue GROUP BY t.estado")
	List<Object[]> contarPorEstado(@Param("idCargue") Long idCargue);
}

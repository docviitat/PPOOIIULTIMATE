package com.PPOOII.Laboratorio;

import com.PPOOII.Laboratorio.DTO.*;
import com.PPOOII.Laboratorio.Entities.Persona;
import com.PPOOII.Laboratorio.Exception.CargueException;
import com.PPOOII.Laboratorio.Repository.*;
import com.PPOOII.Laboratorio.Services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServiciosRequisitosTests {
    VehiculoService vehiculos = new VehiculoService();
    VehiculoRepository repo = mock(VehiculoRepository.class);
    DocumentoRepository docs = mock(DocumentoRepository.class);
    VehiculoDTO vehiculo = new VehiculoDTO();
    DocumentoVehiculoDTO documento = new DocumentoVehiculoDTO();

    @BeforeEach void preparar() {
        ReflectionTestUtils.setField(vehiculos, "repo", repo);
        ReflectionTestUtils.setField(vehiculos, "docRepo", docs);
        vehiculo.idVehiculo = 7;
        documento.idDocumento = 1;
        documento.id = 99L;
        documento.fechaVencimiento = LocalDate.now().plusDays(5);
        vehiculo.documentos = List.of(documento);
        when(repo.porPlaca("ABC123")).thenReturn(vehiculo);
        when(repo.porId(7)).thenReturn(vehiculo);
    }

    @Test void habilitaSoloDocumentoVigenteDelVehiculoSolicitado() {
        assertThat(vehiculos.cambiarEstadoDocumento(" abc123 ", 1, "HABILITADO")).isSameAs(vehiculo);
        verify(repo).cambiarEstadoDocumento(99L, "Habilitado");
    }

    @Test void rechazaDocumentoVencidoSinActualizarEstado() {
        documento.fechaVencimiento = LocalDate.now().minusDays(1);
        assertThatThrownBy(() -> vehiculos.cambiarEstadoDocumento("ABC123", 1, "Habilitado"))
                .isInstanceOfSatisfying(CargueException.class, e -> assertThat(e.getStatus().value()).isEqualTo(409));
        verify(repo, never()).cambiarEstadoDocumento(anyLong(), anyString());
    }

    @Test void rechazaEstadoInvalidoYDocumentoDeOtroVehiculo() {
        assertThatThrownBy(() -> vehiculos.cambiarEstadoDocumento("ABC123", 1, "APROBADO"))
                .isInstanceOfSatisfying(CargueException.class, e -> assertThat(e.getStatus().value()).isEqualTo(400));
        assertThatThrownBy(() -> vehiculos.cambiarEstadoDocumento("ABC123", 2, "Habilitado"))
                .isInstanceOfSatisfying(CargueException.class, e -> assertThat(e.getStatus().value()).isEqualTo(404));
        verify(repo, never()).cambiarEstadoDocumento(anyLong(), anyString());
    }

    @Test void noPermiteCambiarAplicabilidadDeDocumentoEnUsoAIncompatible() {
        var service = new DocumentoService();
        ReflectionTestUtils.setField(service, "repo", docs);
        DocumentoDTO dto = new DocumentoDTO();
        dto.idDocumento = 1; dto.codigoDocumento = "SOAT"; dto.nombreDocumento = "Seguro";
        dto.tiposVehiculo = "M"; dto.obligatoriedad = "RM";
        when(docs.porId(1)).thenReturn(dto);
        when(docs.tieneVehiculosIncompatibles(1, "M")).thenReturn(true);
        assertThatThrownBy(() -> service.actualizar(dto)).isInstanceOfSatisfying(CargueException.class,
                e -> assertThat(e.getStatus().value()).isEqualTo(409));
        verify(docs, never()).actualizar(any());
    }

    @Test void consultaPersonaInexistenteDevuelve404() {
        var service = new PersonaServiceImpl();
        var personas = mock(PersonaRepository.class);
        ReflectionTestUtils.setField(service, "IPersonaRepository", personas);
        assertThatThrownBy(() -> service.findById(123)).isInstanceOfSatisfying(CargueException.class,
                e -> assertThat(e.getStatus().value()).isEqualTo(404));
        Persona persona = new Persona();
        when(personas.findById(123)).thenReturn(persona);
        assertThat(service.findById(123)).isSameAs(persona);
    }
}

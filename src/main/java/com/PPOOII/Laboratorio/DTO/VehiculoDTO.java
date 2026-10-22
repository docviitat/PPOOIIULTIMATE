package com.PPOOII.Laboratorio.DTO;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Vehículo con sus documentos asociados.")
public class VehiculoDTO {
	@Schema(description="Identificador: se genera en POST y se requiere en PUT", example = "1")
	public Integer idVehiculo;
	@Schema(description = "Automovil o Motocicleta", example = "Automovil", allowableValues = {"Automovil", "Motocicleta"}, requiredMode = Schema.RequiredMode.REQUIRED)
	public String tipoVehiculo;
	@Schema(description = "Placa de 6 caracteres alfanuméricos", example = "ABC123")
	public String placa;
	@Schema(description = "Tipo de servicio: A, M o AM", example = "A", allowableValues = {"A", "M", "AM"}, requiredMode = Schema.RequiredMode.REQUIRED)
	public String tipoServicio;
	@Schema(description = "Gasolina, Diesel, Gas, Electrico o Hibrido", example = "Gasolina", allowableValues = {"Gasolina", "Diesel", "Gas", "Electrico", "Hibrido"}, requiredMode = Schema.RequiredMode.REQUIRED)
	public String tipoCombustible;
	@Schema(example = "5")
	public Integer capacidadPasajeros;
	@Schema(example = "Negro")
	public String color;
	@Schema(example = "2022")
	public Integer modelo;
	@Schema(example = "Toyota")
	public String marca;
	@Schema(example = "Corolla")
	public String linea;
	@Schema(description = "Documentos asociados. Al crear el vehículo es obligatorio enviar al menos uno; quedan En Verificacion.")
	public List<DocumentoVehiculoDTO> documentos;
}

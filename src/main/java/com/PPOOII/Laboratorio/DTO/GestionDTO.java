package com.PPOOII.Laboratorio.DTO;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;
public final class GestionDTO {
    private GestionDTO() {}
    @Schema(description="Nueva contraseña del usuario")
    public record Password(@Schema(example="NuevaClave123",minLength=6) String password) {}
    @Schema(description="Vehículos que se asocian al conductor")
    public record Placas(@Schema(example="[\"ABC123\",\"TST001\"]") List<String> placas) {}
    @Schema(description="Estado del conductor en relación con el vehículo")
    public record Estado(@Schema(allowableValues={"PO","EA","RO"},example="PO") String estado) {}

    @Schema(description="Resultado de la verificación del documento asociado a un vehículo")
    public record EstadoDocumento(@Schema(allowableValues={"Habilitado","Vencido","En Verificacion"},example="Habilitado") String estado) {}
}

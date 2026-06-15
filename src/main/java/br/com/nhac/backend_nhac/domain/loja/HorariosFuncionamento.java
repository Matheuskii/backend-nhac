package br.com.nhac.backend_nhac.domain.loja;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HorariosFuncionamento {

    @Schema(description = "Horário de funcionamento no Domingo", example = "10:00-20:00")
    @Column(name = "horario_domingo", length = 20)
    private String domingo;

    @Schema(description = "Horário de funcionamento na Segunda-feira", example = "08:00-22:00")
    @Column(name = "horario_segunda", length = 20)
    private String segunda;

    @Schema(description = "Horário de funcionamento na Terça-feira", example = "08:00-22:00")
    @Column(name = "horario_terca", length = 20)
    private String terca;

    @Schema(description = "Horário de funcionamento na Quarta-feira", example = "08:00-22:00")
    @Column(name = "horario_quarta", length = 20)
    private String quarta;

    @Schema(description = "Horário de funcionamento na Quinta-feira", example = "08:00-22:00")
    @Column(name = "horario_quinta", length = 20)
    private String quinta;

    @Schema(description = "Horário de funcionamento na Sexta-feira", example = "08:00-23:00")
    @Column(name = "horario_sexta", length = 20)
    private String sexta;

    @Schema(description = "Horário de funcionamento no Sábado", example = "09:00-23:00")
    @Column(name = "horario_sabado", length = 20)
    private String sabado;
}
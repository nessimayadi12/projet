package com.banque.abc.tpe.dto.panne;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PanneDiagnosticIaRequest {

    @NotBlank(message = "La description est obligatoire")
    @Size(max = 2000, message = "La description ne doit pas depasser 2000 caracteres")
    private String description;
}

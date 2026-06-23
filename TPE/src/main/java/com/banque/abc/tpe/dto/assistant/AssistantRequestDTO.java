package com.banque.abc.tpe.dto.assistant;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssistantRequestDTO {

    @NotBlank(message = "La question est obligatoire")
    private String question;
}

package com.banque.abc.tpe.dto.panne;

import com.banque.abc.tpe.entity.enums.TypePanne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PanneDiagnosticIaSource {

    private Long id;
    private String titre;
    private TypePanne typePanne;
    private Integer score;
    private String extrait;
    private List<String> indices;
}

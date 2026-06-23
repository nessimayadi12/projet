package com.banque.abc.tpe.dto.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditFieldChange {

    private String field;
    private Object oldValue;
    private Object newValue;
}

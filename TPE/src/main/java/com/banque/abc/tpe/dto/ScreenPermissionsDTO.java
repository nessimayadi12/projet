package com.banque.abc.tpe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreenPermissionsDTO {
    private Boolean canView;
    private Boolean canCreate;
    private Boolean canEdit;
    private Boolean canDelete;
    private Boolean canExport;
}

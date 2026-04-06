package com.banque.abc.tpe.dto.tpe;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TPEImportResult {

    private int totalRows;
    private int storedRows;
    private int importedRows;
    private int updatedRows;
    private int affectedRows;
    private int skippedRows;

    private List<String> errors = new ArrayList<>();
}
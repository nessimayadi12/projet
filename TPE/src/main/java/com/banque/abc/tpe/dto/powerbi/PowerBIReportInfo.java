package com.banque.abc.tpe.dto.powerbi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PowerBIReportInfo {
    private String id;
    private String name;
    private String embedUrl;
    private String webUrl;
    private String datasetId;
}

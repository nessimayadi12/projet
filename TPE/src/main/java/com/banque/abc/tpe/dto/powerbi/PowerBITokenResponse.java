package com.banque.abc.tpe.dto.powerbi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PowerBITokenResponse {
    private String token;
    private String tokenId;
    private String expiration;
}

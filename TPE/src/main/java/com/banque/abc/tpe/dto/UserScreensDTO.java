package com.banque.abc.tpe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserScreensDTO {
    private String username;
    private String role;
    private List<ScreenDTO> screens;
}

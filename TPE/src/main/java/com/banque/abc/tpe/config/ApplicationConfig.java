package com.banque.abc.tpe.config;

import com.banque.abc.tpe.dto.demande.DemandeResponse;
import com.banque.abc.tpe.entity.Demande;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setAmbiguityIgnored(true);
        modelMapper.typeMap(Demande.class, DemandeResponse.class)
                .addMappings(mapper -> mapper.skip(DemandeResponse::setDateAffectation));
        return modelMapper;
    }
}

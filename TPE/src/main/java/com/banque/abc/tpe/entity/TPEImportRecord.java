package com.banque.abc.tpe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "tpe_import_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TPEImportRecord extends BaseEntity {

    @Column(name = "n_affiliation", nullable = false)
    private String nAffiliation;

    @Column(name = "source_row_number", nullable = false)
    private Integer sourceRowNumber;

    @Column(name = "source_file_name")
    private String sourceFileName;

    @Column(name = "type_tpe")
    private String typeTPE;

    @Column(name = "numero_serie")
    private String numeroSerie;

    @Column(name = "numero_terminal")
    private String numeroTerminal;

    @Column(name = "raison_sociale")
    private String raisonSociale;

    @Column(name = "activite")
    private String activite;

    @Column(name = "mcc")
    private String mcc;

    @Column(name = "numero_compte")
    private String numeroCompte;

    @Column(name = "code_agence")
    private String codeAgence;

    @Column(name = "adresse")
    private String adresse;

    @Column(name = "code_postal")
    private String codePostal;

    @Column(name = "telephone")
    private String telephone;

    @Column(name = "email")
    private String email;

    @Column(name = "privilege_secteur")
    private String privilegeSecteur;

    @Column(name = "taux_commission")
    private String tauxCommission;

    @Column(name = "taux_commission_inter")
    private String tauxCommissionInter;

    @Column(name = "loyer")
    private String loyer;

    @Column(name = "n_compte_intern")
    private String nCompteIntern;

    @Column(name = "groupe")
    private String groupe;

    @Column(name = "num_seq")
    private String numSeq;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "value_date")
    private LocalDate valueDate;

    @Column(name = "date_affiliation")
    private LocalDate dateAffiliation;

    @Lob
    @Column(name = "raw_data_json", columnDefinition = "TEXT")
    private String rawDataJson;
}
package com.banque.abc.tpe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "TPE_POSTING_comp")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class                                                                                                                                                                                                                                TPEPostingComp extends BaseEntity {

    @Column(name = "BRANCH", length = 10)
    private String branch;

    @Column(name = "PROFIT_CENTER", length = 10)
    private String profitCenter;

    @Column(name = "CLIENT", length = 20)
    private String client;

    @Column(name = "ACCOUNT", length = 50)
    private String account;

    @Column(name = "RB_GL", length = 10)
    private String rbGl;

    @Column(name = "ccy", length = 10)
    private String ccy;

    @Column(name = "SEQ_NO", length = 10)
    private String seqNo;

    @Column(name = "REF", length = 50)
    private String ref;

    @Column(name = "TRAN_TYPE", length = 10)
    private String tranType;

    @Column(name = "DATE")
    private LocalDate date;

    @Column(name = "AMOUNT", precision = 18, scale = 3)
    private BigDecimal amount;

    @Column(name = "CR_DR", length = 2)
    private String crDr;

    @Column(name = "NARRATIVE", length = 255)
    private String narrative;

    @Column(name = "sessiondate", length = 8)
    private String sessionDate;
}

package com.tpe.management.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "TPE_POSTING_comp")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TPEPosting {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "branch")
    private String branch;
    
    @Column(name = "profit_centre")
    private String profitCentre;
    
    @Column(name = "client_id")
    private String clientId;
    
    @Column(name = "account_no")
    private String accountNo;
    
    @Column(name = "account_name")
    private String accountName;
    
    @Column(name = "account_type")
    private String accountType;
    
    @Column(name = "ccy")
    private String ccy;
    
    @Column(name = "seq_no")
    private String seqNo;
    
    @Column(name = "reference_no")
    private String referenceNo;
    
    @Column(name = "rb_tran_type")
    private String rbTranType;
    
    @Column(name = "value_date")
    private String valueDate;
    
    @Column(name = "amount", precision = 15, scale = 3)
    private BigDecimal amount;
    
    @Column(name = "dc")
    private String dc;
    
    @Column(name = "narrative")
    private String narrative;
    
    @Column(name = "tran_type")
    private String tranType;
    
    @Column(name = "rb_gl")
    private String rbGl;
    
    @Column(name = "session_date")
    private String sessionDate;
    
    @Column(name = "session_user")
    private String sessionUser;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

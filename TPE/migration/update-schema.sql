CREATE TABLE affectations
(
    id                   BIGINT AUTO_INCREMENT NOT NULL,
    created_date         datetime NOT NULL,
    last_modified_date   datetime NULL,
    created_by           VARCHAR(255) NULL,
    last_modified_by     VARCHAR(255) NULL,
    version              BIGINT NULL,
    tpe_id               BIGINT   NOT NULL,
    commercant_id        BIGINT   NOT NULL,
    demande_id           BIGINT NULL,
    date_affectation     date     NOT NULL,
    date_mise_en_service date NULL,
    date_fin             date NULL,
    actif                BIT(1) NULL,
    bon_livraison_path   VARCHAR(255) NULL,
    contrat_path         VARCHAR(255) NULL,
    commentaire          TEXT NULL,
    affecte_par_id       BIGINT NULL,
    CONSTRAINT pk_affectations PRIMARY KEY (id)
);

CREATE TABLE audit_logs
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    date_action datetime     NOT NULL,
    username    VARCHAR(255) NOT NULL,
    action      VARCHAR(255) NOT NULL,
    entity_type VARCHAR(255) NOT NULL,
    entity_id   VARCHAR(255) NULL,
    details     TEXT NULL,
    ip_address  VARCHAR(255) NULL,
    user_agent  VARCHAR(255) NULL,
    statut      VARCHAR(255) NULL,
    CONSTRAINT pk_audit_logs PRIMARY KEY (id)
);

CREATE TABLE commentaires
(
    id                 BIGINT AUTO_INCREMENT NOT NULL,
    created_date       datetime NOT NULL,
    last_modified_date datetime NULL,
    created_by         VARCHAR(255) NULL,
    last_modified_by   VARCHAR(255) NULL,
    version            BIGINT NULL,
    demande_id         BIGINT   NOT NULL,
    auteur_id          BIGINT   NOT NULL,
    contenu            TEXT     NOT NULL,
    interne            BIT(1) NULL,
    CONSTRAINT pk_commentaires PRIMARY KEY (id)
);

CREATE TABLE commercants
(
    id                    BIGINT AUTO_INCREMENT NOT NULL,
    created_date          datetime     NOT NULL,
    last_modified_date    datetime NULL,
    created_by            VARCHAR(255) NULL,
    last_modified_by      VARCHAR(255) NULL,
    version               BIGINT NULL,
    raison_sociale        VARCHAR(255) NOT NULL,
    activite              VARCHAR(255) NOT NULL,
    numero_compte         VARCHAR(255) NOT NULL,
    adresse               VARCHAR(255) NULL,
    localite              VARCHAR(255) NULL,
    code_postal           VARCHAR(255) NULL,
    code_agence           VARCHAR(255) NOT NULL,
    telephone             VARCHAR(255) NULL,
    email                 VARCHAR(255) NULL,
    statut                VARCHAR(255) NOT NULL,
    loyer DOUBLE NULL,
    rne_file_path         VARCHAR(255) NULL,
    email_notification    VARCHAR(255) NULL,
    type_commerce         VARCHAR(255) NULL,
    url_site_marchand     VARCHAR(255) NULL,
    webhook_url           VARCHAR(255) NULL,
    webmaster             VARCHAR(255) NULL,
    contact_technique     VARCHAR(255) NULL,
    type_cartes_acceptees VARCHAR(255) NULL,
    mode_test             BIT(1) NULL,
    CONSTRAINT pk_commercants PRIMARY KEY (id)
);

CREATE TABLE demandes
(
    id                     BIGINT AUTO_INCREMENT NOT NULL,
    created_date           datetime     NOT NULL,
    last_modified_date     datetime NULL,
    created_by             VARCHAR(255) NULL,
    last_modified_by       VARCHAR(255) NULL,
    version                BIGINT NULL,
    `reference`            VARCHAR(255) NOT NULL,
    type_demande           VARCHAR(255) NOT NULL,
    statut                 VARCHAR(255) NOT NULL,
    commercant_id          BIGINT       NOT NULL,
    demandeur_id           BIGINT       NOT NULL,
    valideur_id            BIGINT NULL,
    inputer_id             BIGINT NULL,
    date_saisie_taux       datetime NULL,
    date_validation        datetime NULL,
    date_cloture           datetime NULL,
    `description`          TEXT NULL,
    commentaire_validation TEXT NULL,
    urgence                VARCHAR(255) NULL,
    raison_sociale         VARCHAR(255) NULL,
    activite               VARCHAR(255) NULL,
    numero_compte          VARCHAR(255) NULL,
    adresse                VARCHAR(255) NULL,
    code_postal            VARCHAR(255) NULL,
    code_agence            VARCHAR(255) NULL,
    telephone              VARCHAR(255) NULL,
    rne_file_path          VARCHAR(255) NULL,
    email_notification     VARCHAR(255) NULL,
    mcc                    VARCHAR(255) NULL,
    taux_commission DOUBLE NULL,
    taux_commission_inter DOUBLE NULL,
    loyer DOUBLE NULL,
    serie_tpe              VARCHAR(255) NULL,
    numero_terminal        VARCHAR(255) NULL,
    value_date             datetime NULL,
    localite               VARCHAR(255) NULL,
    rib                    VARCHAR(255) NULL,
    webmaster              VARCHAR(255) NULL,
    contact_technique      VARCHAR(255) NULL,
    url_site_marchand      VARCHAR(255) NULL,
    CONSTRAINT pk_demandes PRIMARY KEY (id)
);

CREATE TABLE historique_statuts
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    tpe_id          BIGINT       NOT NULL,
    ancien_statut   VARCHAR(255) NULL,
    nouveau_statut  VARCHAR(255) NOT NULL,
    date_changement datetime     NOT NULL,
    change_par      VARCHAR(255) NULL,
    commentaire     TEXT NULL,
    CONSTRAINT pk_historique_statuts PRIMARY KEY (id)
);

CREATE TABLE pannes
(
    id                     BIGINT AUTO_INCREMENT NOT NULL,
    created_date           datetime     NOT NULL,
    last_modified_date     datetime NULL,
    created_by             VARCHAR(255) NULL,
    last_modified_by       VARCHAR(255) NULL,
    version                BIGINT NULL,
    `reference`            VARCHAR(255) NOT NULL,
    tpe_id                 BIGINT       NOT NULL,
    statut                 VARCHAR(255) NOT NULL,
    `description`          TEXT         NOT NULL,
    date_declaration       datetime     NOT NULL,
    date_diagnostic        datetime NULL,
    date_reparation        datetime NULL,
    date_resolution        datetime NULL,
    declarant_id           BIGINT       NOT NULL,
    technicien_id          BIGINT NULL,
    diagnostic             TEXT NULL,
    action_corrective      TEXT NULL,
    commentaire_technicien TEXT NULL,
    tpe_remplacement_id    BIGINT NULL,
    cout_reparation DOUBLE NULL,
    sous_garantie          BIT(1) NULL,
    CONSTRAINT pk_pannes PRIMARY KEY (id)
);

CREATE TABLE pieces_detachees
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    panne_id    BIGINT       NOT NULL,
    designation VARCHAR(255) NOT NULL,
    `reference` VARCHAR(255) NULL,
    quantite    INT          NOT NULL,
    prix_unitaire DOUBLE NULL,
    prix_total DOUBLE NULL,
    CONSTRAINT pk_pieces_detachees PRIMARY KEY (id)
);

CREATE TABLE pieces_jointes
(
    id                 BIGINT AUTO_INCREMENT NOT NULL,
    created_date       datetime     NOT NULL,
    last_modified_date datetime NULL,
    created_by         VARCHAR(255) NULL,
    last_modified_by   VARCHAR(255) NULL,
    version            BIGINT NULL,
    demande_id         BIGINT       NOT NULL,
    nom_fichier        VARCHAR(255) NOT NULL,
    chemin_fichier     VARCHAR(255) NOT NULL,
    type_mime          VARCHAR(255) NOT NULL,
    taille_fichier     BIGINT NULL,
    `description`      TEXT NULL,
    CONSTRAINT pk_pieces_jointes PRIMARY KEY (id)
);

CREATE TABLE roles
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    name          VARCHAR(255) NOT NULL,
    `description` VARCHAR(255) NULL,
    CONSTRAINT pk_roles PRIMARY KEY (id)
);

CREATE TABLE screen_roles
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    screen_id  BIGINT NOT NULL,
    role_id    BIGINT NOT NULL,
    can_view   BIT(1) NULL,
    can_create BIT(1) NULL,
    can_edit   BIT(1) NULL,
    can_delete BIT(1) NULL,
    can_export BIT(1) NULL,
    created_at datetime NULL,
    updated_at datetime NULL,
    CONSTRAINT pk_screen_roles PRIMARY KEY (id)
);

CREATE TABLE screens
(
    id                 BIGINT AUTO_INCREMENT NOT NULL,
    created_date       datetime     NOT NULL,
    last_modified_date datetime NULL,
    created_by         VARCHAR(255) NULL,
    last_modified_by   VARCHAR(255) NULL,
    version            BIGINT NULL,
    code               VARCHAR(255) NOT NULL,
    libelle            VARCHAR(255) NOT NULL,
    `description`      VARCHAR(500) NULL,
    route              VARCHAR(255) NOT NULL,
    icon               VARCHAR(255) NULL,
    ordre              INT NULL,
    parent_id          BIGINT NULL,
    actif              BIT(1)       NOT NULL,
    CONSTRAINT pk_screens PRIMARY KEY (id)
);

CREATE TABLE taux
(
    id                 BIGINT AUTO_INCREMENT NOT NULL,
    created_date       datetime     NOT NULL,
    last_modified_date datetime NULL,
    created_by         VARCHAR(255) NULL,
    last_modified_by   VARCHAR(255) NULL,
    version            BIGINT NULL,
    commercant_id      BIGINT       NOT NULL,
    ancien_taux_commission DOUBLE NULL,
    nouveau_taux_commission DOUBLE NOT NULL,
    ancien_taux_commission_inter DOUBLE NULL,
    nouveau_taux_commission_inter DOUBLE NOT NULL,
    statut             VARCHAR(255) NOT NULL,
    inputer_id         BIGINT       NOT NULL,
    authorizer_id      BIGINT NULL,
    date_saisie        datetime     NOT NULL,
    date_validation    datetime NULL,
    motif_rejet        TEXT NULL,
    commentaire        TEXT NULL,
    date_application   datetime NULL,
    actif              BIT(1) NULL,
    CONSTRAINT pk_taux PRIMARY KEY (id)
);

CREATE TABLE tpe_import_records
(
    id                    BIGINT AUTO_INCREMENT NOT NULL,
    created_date          datetime     NOT NULL,
    last_modified_date    datetime NULL,
    created_by            VARCHAR(255) NULL,
    last_modified_by      VARCHAR(255) NULL,
    version               BIGINT NULL,
    n_affiliation         VARCHAR(255) NOT NULL,
    source_row_number     INT          NOT NULL,
    source_file_name      VARCHAR(255) NULL,
    type_tpe              VARCHAR(255) NULL,
    numero_serie          VARCHAR(255) NULL,
    numero_terminal       VARCHAR(255) NULL,
    raison_sociale        VARCHAR(255) NULL,
    activite              VARCHAR(255) NULL,
    mcc                   VARCHAR(255) NULL,
    numero_compte         VARCHAR(255) NULL,
    code_agence           VARCHAR(255) NULL,
    adresse               VARCHAR(255) NULL,
    code_postal           VARCHAR(255) NULL,
    telephone             VARCHAR(255) NULL,
    email                 VARCHAR(255) NULL,
    privilege_secteur     VARCHAR(255) NULL,
    taux_commission       VARCHAR(255) NULL,
    taux_commission_inter VARCHAR(255) NULL,
    loyer                 VARCHAR(255) NULL,
    n_compte_intern       VARCHAR(255) NULL,
    groupe                VARCHAR(255) NULL,
    num_seq               VARCHAR(255) NULL,
    active                BIT(1) NULL,
    value_date            date NULL,
    date_affiliation      date NULL,
    raw_data_json         TEXT NULL,
    CONSTRAINT pk_tpe_import_records PRIMARY KEY (id)
);

CREATE TABLE tpe_posting_comp
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    branch        VARCHAR(255) NULL,
    profit_centre VARCHAR(255) NULL,
    client_id     VARCHAR(255) NULL,
    account_no    VARCHAR(255) NULL,
    account_name  VARCHAR(255) NULL,
    account_type  VARCHAR(255) NULL,
    ccy           VARCHAR(255) NULL,
    seq_no        VARCHAR(255) NULL,
    reference_no  VARCHAR(255) NULL,
    rb_tran_type  VARCHAR(255) NULL,
    value_date    VARCHAR(255) NULL,
    amount        DECIMAL(15, 3) NULL,
    dc            VARCHAR(255) NULL,
    narrative     VARCHAR(255) NULL,
    tran_type     VARCHAR(255) NULL,
    rb_gl         VARCHAR(255) NULL,
    session_date  VARCHAR(255) NULL,
    session_user  VARCHAR(255) NULL,
    created_at    datetime NULL,
    CONSTRAINT pk_tpe_posting_comp PRIMARY KEY (id)
);

CREATE TABLE tpes
(
    id                   BIGINT AUTO_INCREMENT NOT NULL,
    created_date         datetime     NOT NULL,
    last_modified_date   datetime NULL,
    created_by           VARCHAR(255) NULL,
    last_modified_by     VARCHAR(255) NULL,
    version              BIGINT NULL,
    typetpe              VARCHAR(255) NOT NULL,
    numero_serie         VARCHAR(255) NOT NULL,
    numero_terminal      VARCHAR(255) NULL,
    statut               VARCHAR(255) NOT NULL,
    marque               VARCHAR(255) NULL,
    modele               VARCHAR(255) NULL,
    date_acquisition     date NULL,
    date_mise_en_service date NULL,
    mcc                  VARCHAR(255) NULL,
    numero_affiliation   VARCHAR(255) NULL,
    cle_api              VARCHAR(255) NULL,
    commercant_id        BIGINT NULL,
    commentaire          TEXT NULL,
    CONSTRAINT pk_tpes PRIMARY KEY (id)
);

CREATE TABLE user_roles
(
    role_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (role_id, user_id)
);

CREATE TABLE users
(
    id                    BIGINT AUTO_INCREMENT NOT NULL,
    created_date          datetime     NOT NULL,
    last_modified_date    datetime NULL,
    created_by            VARCHAR(255) NULL,
    last_modified_by      VARCHAR(255) NULL,
    version               BIGINT NULL,
    username              VARCHAR(255) NOT NULL,
    password              VARCHAR(255) NOT NULL,
    nom                   VARCHAR(255) NOT NULL,
    prenom                VARCHAR(255) NOT NULL,
    email                 VARCHAR(255) NOT NULL,
    telephone             VARCHAR(255) NULL,
    code_agence           VARCHAR(255) NULL,
    actif                 BIT(1)       NOT NULL,
    last_login            datetime NULL,
    failed_login_attempts INT NULL,
    account_locked        BIT(1) NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE affectations
    ADD CONSTRAINT uc_affectations_demande UNIQUE (demande_id);

ALTER TABLE commercants
    ADD CONSTRAINT uc_commercants_email UNIQUE (email);

ALTER TABLE demandes
    ADD CONSTRAINT uc_demandes_reference UNIQUE (`reference`);

ALTER TABLE pannes
    ADD CONSTRAINT uc_pannes_reference UNIQUE (`reference`);

ALTER TABLE roles
    ADD CONSTRAINT uc_roles_name UNIQUE (name);

ALTER TABLE screens
    ADD CONSTRAINT uc_screens_code UNIQUE (code);

ALTER TABLE screens
    ADD CONSTRAINT uc_screens_route UNIQUE (route);

ALTER TABLE tpes
    ADD CONSTRAINT uc_tpes_numeroserie UNIQUE (numero_serie);

ALTER TABLE tpes
    ADD CONSTRAINT uc_tpes_numeroterminal UNIQUE (numero_terminal);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE users
    ADD CONSTRAINT uc_users_username UNIQUE (username);

ALTER TABLE affectations
    ADD CONSTRAINT FK_AFFECTATIONS_ON_AFFECTE_PAR FOREIGN KEY (affecte_par_id) REFERENCES users (id);

ALTER TABLE affectations
    ADD CONSTRAINT FK_AFFECTATIONS_ON_COMMERCANT FOREIGN KEY (commercant_id) REFERENCES commercants (id);

ALTER TABLE affectations
    ADD CONSTRAINT FK_AFFECTATIONS_ON_DEMANDE FOREIGN KEY (demande_id) REFERENCES demandes (id);

ALTER TABLE affectations
    ADD CONSTRAINT FK_AFFECTATIONS_ON_TPE FOREIGN KEY (tpe_id) REFERENCES tpes (id);

ALTER TABLE commentaires
    ADD CONSTRAINT FK_COMMENTAIRES_ON_AUTEUR FOREIGN KEY (auteur_id) REFERENCES users (id);

ALTER TABLE commentaires
    ADD CONSTRAINT FK_COMMENTAIRES_ON_DEMANDE FOREIGN KEY (demande_id) REFERENCES demandes (id);

ALTER TABLE demandes
    ADD CONSTRAINT FK_DEMANDES_ON_COMMERCANT FOREIGN KEY (commercant_id) REFERENCES commercants (id);

ALTER TABLE demandes
    ADD CONSTRAINT FK_DEMANDES_ON_DEMANDEUR FOREIGN KEY (demandeur_id) REFERENCES users (id);

ALTER TABLE demandes
    ADD CONSTRAINT FK_DEMANDES_ON_INPUTER FOREIGN KEY (inputer_id) REFERENCES users (id);

ALTER TABLE demandes
    ADD CONSTRAINT FK_DEMANDES_ON_VALIDEUR FOREIGN KEY (valideur_id) REFERENCES users (id);

ALTER TABLE historique_statuts
    ADD CONSTRAINT FK_HISTORIQUE_STATUTS_ON_TPE FOREIGN KEY (tpe_id) REFERENCES tpes (id);

ALTER TABLE pannes
    ADD CONSTRAINT FK_PANNES_ON_DECLARANT FOREIGN KEY (declarant_id) REFERENCES users (id);

ALTER TABLE pannes
    ADD CONSTRAINT FK_PANNES_ON_TECHNICIEN FOREIGN KEY (technicien_id) REFERENCES users (id);

ALTER TABLE pannes
    ADD CONSTRAINT FK_PANNES_ON_TPE FOREIGN KEY (tpe_id) REFERENCES tpes (id);

ALTER TABLE pannes
    ADD CONSTRAINT FK_PANNES_ON_TPE_REMPLACEMENT FOREIGN KEY (tpe_remplacement_id) REFERENCES tpes (id);

ALTER TABLE pieces_detachees
    ADD CONSTRAINT FK_PIECES_DETACHEES_ON_PANNE FOREIGN KEY (panne_id) REFERENCES pannes (id);

ALTER TABLE pieces_jointes
    ADD CONSTRAINT FK_PIECES_JOINTES_ON_DEMANDE FOREIGN KEY (demande_id) REFERENCES demandes (id);

ALTER TABLE screen_roles
    ADD CONSTRAINT FK_SCREEN_ROLES_ON_ROLE FOREIGN KEY (role_id) REFERENCES roles (id);

ALTER TABLE screen_roles
    ADD CONSTRAINT FK_SCREEN_ROLES_ON_SCREEN FOREIGN KEY (screen_id) REFERENCES screens (id);

ALTER TABLE taux
    ADD CONSTRAINT FK_TAUX_ON_AUTHORIZER FOREIGN KEY (authorizer_id) REFERENCES users (id);

ALTER TABLE taux
    ADD CONSTRAINT FK_TAUX_ON_COMMERCANT FOREIGN KEY (commercant_id) REFERENCES commercants (id);

ALTER TABLE taux
    ADD CONSTRAINT FK_TAUX_ON_INPUTER FOREIGN KEY (inputer_id) REFERENCES users (id);

ALTER TABLE tpes
    ADD CONSTRAINT FK_TPES_ON_COMMERCANT FOREIGN KEY (commercant_id) REFERENCES commercants (id);

ALTER TABLE user_roles
    ADD CONSTRAINT fk_userol_on_role FOREIGN KEY (role_id) REFERENCES roles (id);

ALTER TABLE user_roles
    ADD CONSTRAINT fk_userol_on_user FOREIGN KEY (user_id) REFERENCES users (id);
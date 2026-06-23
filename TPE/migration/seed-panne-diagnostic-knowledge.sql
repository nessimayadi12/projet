;
-- Remplissage manuel de la base RAG des diagnostics de pannes TPE.
-- Base cible: MySQL / TPE_Managements
-- Execution possible dans phpMyAdmin, MySQL Workbench ou mysql CLI.

CREATE TABLE IF NOT EXISTS panne_diagnostic_knowledge (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) DEFAULT 'manual',
    last_modified_by VARCHAR(255) DEFAULT 'manual',
    version BIGINT DEFAULT 0,
    titre VARCHAR(160) NOT NULL,
    type_panne VARCHAR(80),
    mots_cles TEXT,
    symptomes TEXT,
    diagnostic TEXT NOT NULL,
    action_corrective TEXT NOT NULL,
    urgence VARCHAR(20) NOT NULL,
    recommandations TEXT,
    remplacement_recommande BOOLEAN DEFAULT FALSE,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    priorite INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

INSERT INTO panne_diagnostic_knowledge (
    created_date, last_modified_date, created_by, last_modified_by, version,
    titre, type_panne, mots_cles, symptomes, diagnostic, action_corrective,
    urgence, recommandations, remplacement_recommande, actif, priorite
)
SELECT
    NOW(), NOW(), 'manual', 'manual', 0,
    'TPE ne s allume plus / batterie',
    'PROBLEME_BATTERIE_CHARGE',
    'ne s allume
ecran noir
batterie
charge
chargeur
decharge
alimentation
port de charge',
    'Le terminal ne demarre pas, affiche un ecran noir, ne tient pas la charge ou ne reagit pas au chargeur.',
    'Batterie dechargee, chargeur defectueux, port de charge endommage ou circuit de charge a controler.',
    'Tester une autre prise et un autre chargeur, laisser le TPE en charge, controler la batterie puis remplacer la batterie si la charge ne tient pas.',
    'HAUTE',
    'Controler la tension batterie
Tester avec un chargeur fonctionnel
Verifier le port de charge
Verifier si le voyant de charge s allume',
    FALSE, TRUE, 95
WHERE NOT EXISTS (
    SELECT 1 FROM panne_diagnostic_knowledge WHERE titre = 'TPE ne s allume plus / batterie'
);

INSERT INTO panne_diagnostic_knowledge (
    created_date, last_modified_date, created_by, last_modified_by, version,
    titre, type_panne, mots_cles, symptomes, diagnostic, action_corrective,
    urgence, recommandations, remplacement_recommande, actif, priorite
)
SELECT
    NOW(), NOW(), 'manual', 'manual', 0,
    'Imprimante bloquee ou ticket non imprime',
    'IMPRIMANTE_BLOQUEE',
    'imprimante
papier
ticket
recu
rouleau
bourrage
n imprime pas
impression',
    'Le TPE ne sort pas de ticket, le papier est bloque ou le rouleau est mal place.',
    'Blocage imprimante, rouleau mal installe, papier incompatible ou tete thermique a nettoyer.',
    'Verifier le sens du papier, remplacer le rouleau, nettoyer le compartiment imprimante puis lancer une impression de test.',
    'MOYENNE',
    'Verifier le sens du rouleau
Tester un nouveau papier
Nettoyer la tete thermique
Relancer une impression de test',
    FALSE, TRUE, 80
WHERE NOT EXISTS (
    SELECT 1 FROM panne_diagnostic_knowledge WHERE titre = 'Imprimante bloquee ou ticket non imprime'
);

INSERT INTO panne_diagnostic_knowledge (
    created_date, last_modified_date, created_by, last_modified_by, version,
    titre, type_panne, mots_cles, symptomes, diagnostic, action_corrective,
    urgence, recommandations, remplacement_recommande, actif, priorite
)
SELECT
    NOW(), NOW(), 'manual', 'manual', 0,
    'Incident 0060 centre bancaire non atteint',
    'INCIDENT_0060_CENTRE_BANCAIRE_NON_ATTEINT',
    '0060
centre bancaire
non atteint
reseau
connexion
gprs
sim
signal
communication
timeout',
    'Le TPE affiche 0060, centre bancaire non atteint, erreur reseau ou impossible de communiquer.',
    'Incident de communication avec le centre bancaire, souvent lie au reseau, a la SIM ou au parametrage de communication.',
    'Verifier la couverture reseau, tester la SIM, controler les parametres GPRS/IP puis relancer un test de communication.',
    'HAUTE',
    'Verifier le signal reseau
Tester la carte SIM
Verifier APN ou parametres IP
Relancer test communication
Controler statut serveur bancaire',
    FALSE, TRUE, 90
WHERE NOT EXISTS (
    SELECT 1 FROM panne_diagnostic_knowledge WHERE titre = 'Incident 0060 centre bancaire non atteint'
);

INSERT INTO panne_diagnostic_knowledge (
    created_date, last_modified_date, created_by, last_modified_by, version,
    titre, type_panne, mots_cles, symptomes, diagnostic, action_corrective,
    urgence, recommandations, remplacement_recommande, actif, priorite
)
SELECT
    NOW(), NOW(), 'manual', 'manual', 0,
    'Erreur carte ou lecteur carte',
    'INCIDENT_0060_ERREUR_CARTE',
    'erreur carte
carte refusee
carte non lue
lecteur carte
puce
piste
insertion carte
0060 carte',
    'La carte n est pas lue, le client obtient une erreur carte ou le lecteur ne detecte pas la carte.',
    'Erreur de lecture carte, lecteur sale ou usage d une carte defectueuse.',
    'Nettoyer le lecteur, tester plusieurs cartes, verifier la lecture puce/piste et controler la version applicative.',
    'MOYENNE',
    'Tester plusieurs cartes
Nettoyer le lecteur
Verifier lecture puce et piste
Controler la version applicative',
    FALSE, TRUE, 75
WHERE NOT EXISTS (
    SELECT 1 FROM panne_diagnostic_knowledge WHERE titre = 'Erreur carte ou lecteur carte'
);

INSERT INTO panne_diagnostic_knowledge (
    created_date, last_modified_date, created_by, last_modified_by, version,
    titre, type_panne, mots_cles, symptomes, diagnostic, action_corrective,
    urgence, recommandations, remplacement_recommande, actif, priorite
)
SELECT
    NOW(), NOW(), 'manual', 'manual', 0,
    'Erreur saisie PIN ou clavier',
    'ERREUR_SAISIE_PIN',
    'pin
code secret
clavier
touche
pinpad
saisie pin
code pin
touche bloquee',
    'Le client ne peut pas saisir le code PIN, une touche ne repond pas ou le pinpad est bloque.',
    'Probleme de clavier, pinpad bloque ou erreur de saisie PIN.',
    'Tester toutes les touches, redemarrer le TPE, verifier le pinpad et orienter en maintenance si le clavier reste bloque.',
    'MOYENNE',
    'Tester toutes les touches
Redemarrer le terminal
Verifier le pinpad
Controler si une touche reste enfoncee',
    FALSE, TRUE, 70
WHERE NOT EXISTS (
    SELECT 1 FROM panne_diagnostic_knowledge WHERE titre = 'Erreur saisie PIN ou clavier'
);

INSERT INTO panne_diagnostic_knowledge (
    created_date, last_modified_date, created_by, last_modified_by, version,
    titre, type_panne, mots_cles, symptomes, diagnostic, action_corrective,
    urgence, recommandations, remplacement_recommande, actif, priorite
)
SELECT
    NOW(), NOW(), 'manual', 'manual', 0,
    'Alerte irruption ou tamper',
    'ALERTE_IRRUPTION',
    'alerte irruption
irruption
intrusion
tamper
security alert
alerte securite
boitier ouvert',
    'Le TPE affiche une alerte de securite, tamper, intrusion ou alerte irruption.',
    'Alerte de securite detectee par le terminal. Le TPE ne doit pas etre remis en service sans expertise.',
    'Isoler le TPE, tracer l incident, ne pas l utiliser, puis planifier une expertise constructeur ou un remplacement.',
    'CRITIQUE',
    'Retirer le TPE du service
Tracer l incident
Verifier garantie constructeur
Prevoir remplacement si alerte confirmee',
    TRUE, TRUE, 100
WHERE NOT EXISTS (
    SELECT 1 FROM panne_diagnostic_knowledge WHERE titre = 'Alerte irruption ou tamper'
);

INSERT INTO panne_diagnostic_knowledge (
    created_date, last_modified_date, created_by, last_modified_by, version,
    titre, type_panne, mots_cles, symptomes, diagnostic, action_corrective,
    urgence, recommandations, remplacement_recommande, actif, priorite
)
SELECT
    NOW(), NOW(), 'manual', 'manual', 0,
    'Incident logiciel ou parametrage',
    'SOFTWARE',
    'logiciel
software
application
bloque
fige
mise a jour
parametrage
redemarre seul
telechargement parametres',
    'Application bloquee, terminal fige, redemarrages repetes, parametrage incomplet ou version incorrecte.',
    'Anomalie logicielle ou parametrage incomplet.',
    'Redemarrer le TPE, verifier la version applicative, recharger les parametres et analyser les journaux.',
    'MOYENNE',
    'Verifier version applicative
Recharger les parametres
Relancer telecollecte si necessaire
Analyser les journaux',
    FALSE, TRUE, 75
WHERE NOT EXISTS (
    SELECT 1 FROM panne_diagnostic_knowledge WHERE titre = 'Incident logiciel ou parametrage'
);

INSERT INTO panne_diagnostic_knowledge (
    created_date, last_modified_date, created_by, last_modified_by, version,
    titre, type_panne, mots_cles, symptomes, diagnostic, action_corrective,
    urgence, recommandations, remplacement_recommande, actif, priorite
)
SELECT
    NOW(), NOW(), 'manual', 'manual', 0,
    'Defaut materiel',
    'HARDWARE',
    'hardware
materiel
ecran casse
lecteur casse
clavier casse
touche cassee
boitier casse
choc
tombe
endommage',
    'Le TPE presente un choc, un ecran casse, un clavier defectueux, un lecteur carte casse ou un boitier endommage.',
    'Defaut materiel probable necessitant un controle physique ou une reparation atelier.',
    'Inspecter le boitier, tester ecran/clavier/lecteur, verifier traces de choc puis envoyer en atelier si le defaut est confirme.',
    'HAUTE',
    'Inspecter le boitier
Tester ecran clavier lecteur
Verifier traces de choc
Envoyer en atelier si besoin',
    FALSE, TRUE, 80
WHERE NOT EXISTS (
    SELECT 1 FROM panne_diagnostic_knowledge WHERE titre = 'Defaut materiel'
);

INSERT INTO panne_diagnostic_knowledge (
    created_date, last_modified_date, created_by, last_modified_by, version,
    titre, type_panne, mots_cles, symptomes, diagnostic, action_corrective,
    urgence, recommandations, remplacement_recommande, actif, priorite
)
SELECT
    NOW(), NOW(), 'manual', 'manual', 0,
    'Coupure secteur ou alimentation externe',
    'COUPURE_SECTEUR',
    'coupure secteur
prise
courant
secteur
adaptateur
alimentation externe
cable alimentation
ne charge pas sur secteur',
    'Le terminal ne recoit pas l alimentation secteur, le chargeur ne fonctionne pas ou la prise est defectueuse.',
    'Probleme d alimentation secteur, adaptateur ou cable defectueux.',
    'Tester une autre prise, verifier le chargeur, essayer un autre adaptateur et controler le cable alimentation.',
    'MOYENNE',
    'Tester une autre prise
Tester un autre adaptateur
Verifier cable alimentation
Controler voyant charge',
    FALSE, TRUE, 70
WHERE NOT EXISTS (
    SELECT 1 FROM panne_diagnostic_knowledge WHERE titre = 'Coupure secteur ou alimentation externe'
);

INSERT INTO panne_diagnostic_knowledge (
    created_date, last_modified_date, created_by, last_modified_by, version,
    titre, type_panne, mots_cles, symptomes, diagnostic, action_corrective,
    urgence, recommandations, remplacement_recommande, actif, priorite
)
SELECT
    NOW(), NOW(), 'manual', 'manual', 0,
    'Court circuit ou surchauffe',
    'COURT_CIRCUIT',
    'court circuit
brule
odeur brule
fumee
surchauffe
chauffe
etincelle
composant brule',
    'Le TPE chauffe anormalement, sent le brule, produit de la fumee ou presente un risque electrique.',
    'Risque de court-circuit ou composant endommage.',
    'Debrancher immediatement, ne plus utiliser le TPE, isoler le terminal et envoyer en expertise technique.',
    'CRITIQUE',
    'Debrancher immediatement
Ne pas remettre en service
Verifier traces de chauffe
Envoyer en expertise technique',
    TRUE, TRUE, 100
WHERE NOT EXISTS (
    SELECT 1 FROM panne_diagnostic_knowledge WHERE titre = 'Court circuit ou surchauffe'
);

INSERT INTO panne_diagnostic_knowledge (
    created_date, last_modified_date, created_by, last_modified_by, version,
    titre, type_panne, mots_cles, symptomes, diagnostic, action_corrective,
    urgence, recommandations, remplacement_recommande, actif, priorite
)
SELECT
    NOW(), NOW(), 'manual', 'manual', 0,
    'Incident 0044 ou 0088',
    'INCIDENT_0044_0088',
    '0044
0088
incident 0044
incident 0088
telecollecte
parametres commercant
transaction refusee',
    'Le TPE affiche un incident 0044 ou 0088 pendant transaction, telecollecte ou synchronisation.',
    'Incident applicatif lie au parametrage ou a la synchronisation du terminal.',
    'Verifier le parametrage commercant, relancer la telecollecte, recharger les parametres puis tester une transaction.',
    'MOYENNE',
    'Verifier parametres commercant
Relancer telecollecte
Recharger parametres
Tester transaction',
    FALSE, TRUE, 65
WHERE NOT EXISTS (
    SELECT 1 FROM panne_diagnostic_knowledge WHERE titre = 'Incident 0044 ou 0088'
);

INSERT INTO panne_diagnostic_knowledge (
    created_date, last_modified_date, created_by, last_modified_by, version,
    titre, type_panne, mots_cles, symptomes, diagnostic, action_corrective,
    urgence, recommandations, remplacement_recommande, actif, priorite
)
SELECT
    NOW(), NOW(), 'manual', 'manual', 0,
    'Incident 020E ou 0067',
    'INCIDENT_020E_0067',
    '020e
0067
incident 020e
incident 0067
version logicielle
parametrage
application',
    'Le terminal affiche un incident 020E ou 0067, souvent apres parametrage ou mise a jour.',
    'Incident applicatif pouvant etre lie a la version logicielle ou au parametrage.',
    'Verifier la version logicielle, recharger les parametres et tester apres redemarrage.',
    'MOYENNE',
    'Verifier version applicative
Recharger parametres
Redemarrer terminal
Tester transaction',
    FALSE, TRUE, 65
WHERE NOT EXISTS (
    SELECT 1 FROM panne_diagnostic_knowledge WHERE titre = 'Incident 020E ou 0067'
);

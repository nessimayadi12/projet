# Guide d'Installation - Système de Demande d'Affectation TPE

## Prérequis

Avant de commencer, assurez-vous d'avoir :

✅ **Backend**
- Java 17 ou supérieur
- Maven 3.8+
- SQL Server 2019+ (ou SQL Server Express)
- Base de données `tpe_management` créée

✅ **Frontend**
- Node.js 14+ et npm 6+
- Angular CLI 12+

---

## Installation

### Étape 1: Configuration de la Base de Données

1. **Créer la base de données** (si pas déjà fait)
```sql
CREATE DATABASE tpe_management;
```

2. **Exécuter la migration**
```sql
-- Localisation: TPE/src/main/resources/db/migration/V2__add_demande_affectation_fields.sql
-- Exécuter le script complet dans SQL Server Management Studio
```

3. **Vérifier les colonnes ajoutées**
```sql
USE tpe_management;
GO

-- Vérifier les nouvelles colonnes
SELECT COLUMN_NAME, DATA_TYPE 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'demandes'
ORDER BY ORDINAL_POSITION;
```

**Colonnes attendues** :
- raison_sociale
- activite
- numero_compte
- adresse
- code_postal
- code_agence
- telephone
- rne_file_path
- email_notification
- mcc
- taux_commission
- taux_commission_inter
- loyer
- serie_tpe
- numero_terminal
- value_date
- localite
- rib
- webmaster
- contact_technique
- url_site_marchand

---

### Étape 2: Configuration Backend

1. **Naviguer vers le dossier backend**
```bash
cd TPE
```

2. **Vérifier la configuration de la BD**

Éditer `src/main/resources/application.properties` :
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=tpe_management;encrypt=true;trustServerCertificate=true
spring.datasource.username=votre_utilisateur
spring.datasource.password=votre_mot_de_passe

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

3. **Compiler le projet**
```bash
mvn clean install
```

**Résultat attendu** :
```
[INFO] BUILD SUCCESS
[INFO] Total time: 2:30 min
```

4. **Démarrer le serveur**
```bash
mvn spring-boot:run
```

**ou**

```bash
java -jar target/tpe-management-1.0.0.jar
```

5. **Vérifier le démarrage**

Le serveur devrait démarrer sur `http://localhost:8080`

Tester l'API :
```bash
curl http://localhost:8080/api/demandes
```

---

### Étape 3: Configuration Frontend

1. **Naviguer vers le dossier frontend**
```bash
cd "../front end"
```

2. **Installer les dépendances**
```bash
npm install
```

**Si erreurs de dépendances** :
```bash
npm install --legacy-peer-deps
```

3. **Vérifier la configuration API**

Éditer `src/environments/environment.ts` :
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

4. **Démarrer le serveur de développement**
```bash
ng serve
```

**ou**

```bash
npm start
```

5. **Accéder à l'application**

Ouvrir le navigateur : `http://localhost:4200`

---

## Vérification de l'Installation

### 1. Test Backend

**Test 1: Créer une demande TPE Physique**
```bash
curl -X POST http://localhost:8080/api/demandes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "typeDemande": "TPE_PHYSIQUE",
    "commercantId": 1,
    "raisonSociale": "Test Commerce",
    "activite": "Vente détail",
    "numeroCompte": "1234567890",
    "adresse": "123 Rue Test",
    "codePostal": "20000",
    "codeAgence": "AG001",
    "telephone": "+212612345678",
    "emailNotification": "test@test.ma",
    "urgence": false
  }'
```

**Réponse attendue** : Code 201 avec les données de la demande créée

**Test 2: Lister les demandes**
```bash
curl http://localhost:8080/api/demandes \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Réponse attendue** : Liste des demandes en JSON

### 2. Test Frontend

1. **Se connecter**
   - URL: `http://localhost:4200/login`
   - Utilisateur par défaut: `admin / Admin@123`

2. **Créer une demande**
   - Menu: Demandes > Nouvelle demande
   - Type: TPE Physique
   - Remplir tous les champs obligatoires
   - Soumettre

3. **Valider une demande (utilisateur Monétique)**
   - Menu: Demandes > Liste
   - Cliquer sur "Valider" pour une demande
   - Remplir les champs de validation
   - Générer le TID
   - Valider

---

## Problèmes Courants et Solutions

### Problème 1: Migration SQL échoue

**Erreur** : `Column 'raison_sociale' already exists`

**Solution** :
```sql
-- Vérifier si les colonnes existent déjà
SELECT COLUMN_NAME 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'demandes' 
  AND COLUMN_NAME = 'raison_sociale';

-- Si elles existent, supprimer et recréer si nécessaire
```

### Problème 2: Backend ne démarre pas

**Erreur** : `Cannot create connection to database`

**Solution** :
1. Vérifier que SQL Server est démarré
2. Vérifier les credentials dans `application.properties`
3. Tester la connexion manuellement

### Problème 3: Frontend - Erreur CORS

**Erreur** : `Access to XMLHttpRequest has been blocked by CORS policy`

**Solution** :

Vérifier la configuration CORS dans le backend (`WebConfig.java`) :
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

### Problème 4: Composant de validation non trouvé

**Erreur** : `Can't resolve 'DemandeValidationComponent'`

**Solution** :
```bash
# Vérifier que le fichier existe
ls "src/app/demandes/demande-validation/"

# Recompiler
ng build
```

### Problème 5: TID non généré

**Erreur** : Le bouton de génération ne fonctionne pas

**Solution** :
1. Vérifier que le service TPE est accessible
2. Vérifier les logs backend pour les erreurs
3. S'assurer que RIB et Code Agence sont renseignés

---

## Configuration Avancée

### Activer les Logs Détaillés

**Backend** (`application.properties`) :
```properties
logging.level.com.banque.abc.tpe=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

**Frontend** (console du navigateur) :
```typescript
// Dans environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  debug: true
};
```

### Configuration Email (Notifications)

**Backend** (`application.properties`) :
```properties
# Configuration SMTP
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=votre_email@gmail.com
spring.mail.password=votre_mot_de_passe
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

## Production

### Build Frontend
```bash
cd "front end"
ng build --prod
```

Les fichiers compilés seront dans `dist/`

### Build Backend
```bash
cd TPE
mvn clean package -DskipTests
```

Le JAR exécutable sera dans `target/tpe-management-1.0.0.jar`

### Déploiement
```bash
# Backend
java -jar tpe-management-1.0.0.jar --spring.profiles.active=prod

# Frontend (avec un serveur web comme Nginx)
# Copier le contenu de dist/ vers /var/www/html/
```

---

## Rollback

Si vous devez annuler les modifications :

### Base de Données
```sql
-- Supprimer les colonnes ajoutées
ALTER TABLE demandes DROP COLUMN raison_sociale;
ALTER TABLE demandes DROP COLUMN activite;
-- ... (répéter pour toutes les colonnes)

-- Supprimer les index
DROP INDEX idx_demandes_code_agence ON demandes;
DROP INDEX idx_demandes_numero_terminal ON demandes;
```

### Code
```bash
# Revenir à la version précédente avec Git
git checkout HEAD~1
```

---

## Support et Documentation

- **Documentation complète** : [DEMANDE-AFFECTATION-TPE.md](DEMANDE-AFFECTATION-TPE.md)
- **Récapitulatif** : [RECAPITULATIF-MODIFICATIONS.md](RECAPITULATIF-MODIFICATIONS.md)
- **API Documentation** : `http://localhost:8080/swagger-ui.html`

---

## Checklist Finale

Avant de considérer l'installation terminée :

- [ ] La migration SQL a été exécutée avec succès
- [ ] Le backend démarre sans erreur
- [ ] Le frontend démarre sans erreur
- [ ] Connexion réussie avec l'utilisateur admin
- [ ] Création d'une demande TPE Physique fonctionne
- [ ] Création d'une demande E-commerce fonctionne
- [ ] Validation Monétique fonctionne
- [ ] Génération du TID fonctionne
- [ ] Les notifications email sont configurées (optionnel)
- [ ] Les logs sont accessibles et lisibles

---

**Installation terminée avec succès!** 🎉

Pour toute question, consultez la documentation ou les logs d'application.

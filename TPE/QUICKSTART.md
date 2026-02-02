# Guide de démarrage rapide

## Prérequis
1. Java 17 installé
2. SQL Server en cours d'exécution
3. Maven installé

## Étapes

### 1. Créer la base de données
```sql
CREATE DATABASE tpe_management;
```

### 2. Configurer application.properties
Modifier les paramètres de connexion SQL Server dans :
`src/main/resources/application.properties`

### 3. Compiler et lancer
```bash
mvn clean install
mvn spring-boot:run
```

### 4. Tester l'API
Accéder à : http://localhost:8080/swagger-ui.html

### 5. Se connecter
Utilisateur par défaut :
- Username: `admin`
- Password: `Admin@123`

## Endpoints principaux

- POST `/api/auth/login` - Authentification
- GET `/api/tpes` - Liste des TPE
- GET `/api/commercants` - Liste des commerçants
- GET `/api/demandes` - Liste des demandes

## Documentation complète
Voir README.md pour plus de détails.

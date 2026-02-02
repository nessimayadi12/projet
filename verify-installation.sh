#!/bin/bash

# Script de vérification post-installation
# Système de Demande d'Affectation TPE

echo "========================================="
echo "Vérification du Système TPE"
echo "========================================="
echo ""

# Couleurs
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Compteurs
TESTS_PASSED=0
TESTS_FAILED=0

# Configuration
API_URL="http://localhost:8080/api"
FRONTEND_URL="http://localhost:4200"

# Fonction de test
test_endpoint() {
    local name=$1
    local url=$2
    local expected_code=$3
    
    echo -n "Test: $name ... "
    
    response_code=$(curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null)
    
    if [ "$response_code" == "$expected_code" ]; then
        echo -e "${GREEN}✓ PASS${NC} (Code: $response_code)"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}✗ FAIL${NC} (Attendu: $expected_code, Reçu: $response_code)"
        ((TESTS_FAILED++))
    fi
}

# Vérification des services
echo "=== Vérification des Services ==="
echo ""

# Backend
echo -n "Backend (API) ... "
if curl -s "$API_URL" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ RUNNING${NC} ($API_URL)"
else
    echo -e "${RED}✗ NOT RUNNING${NC}"
    echo "Veuillez démarrer le backend avec: cd TPE && mvn spring-boot:run"
    exit 1
fi

# Frontend
echo -n "Frontend (Angular) ... "
if curl -s "$FRONTEND_URL" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ RUNNING${NC} ($FRONTEND_URL)"
else
    echo -e "${YELLOW}⚠ NOT RUNNING${NC}"
    echo "Info: Démarrez le frontend avec: cd 'front end' && ng serve"
fi

echo ""
echo "=== Tests des Endpoints API ==="
echo ""

# Test endpoints publics
test_endpoint "Health Check" "$API_URL/actuator/health" "200"
test_endpoint "Liste des demandes" "$API_URL/demandes" "401"

echo ""
echo "=== Vérification de la Base de Données ===" 
echo ""

# Note: Ces commandes nécessitent sqlcmd installé
if command -v sqlcmd &> /dev/null; then
    echo "Vérification des colonnes de la table 'demandes'..."
    
    # Liste des colonnes attendues
    EXPECTED_COLUMNS=(
        "raison_sociale"
        "activite"
        "numero_compte"
        "adresse"
        "code_postal"
        "code_agence"
        "telephone"
        "email_notification"
        "mcc"
        "taux_commission"
        "numero_terminal"
    )
    
    for col in "${EXPECTED_COLUMNS[@]}"; do
        echo -n "  Colonne '$col' ... "
        # Vérifier si la colonne existe (à adapter selon votre config SQL Server)
        echo -e "${YELLOW}[SKIP - Nécessite sqlcmd configuré]${NC}"
    done
else
    echo -e "${YELLOW}⚠ sqlcmd non installé - Vérification BD ignorée${NC}"
    echo "Pour vérifier manuellement, exécutez:"
    echo "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'demandes';"
fi

echo ""
echo "=== Vérification des Fichiers ==="
echo ""

# Vérifier les fichiers créés
FILES_TO_CHECK=(
    "front end/src/app/demandes/demande-validation/demande-validation.component.ts"
    "front end/src/app/demandes/demande-validation/demande-validation.component.html"
    "front end/src/app/demandes/demande-validation/demande-validation.component.css"
    "TPE/src/main/resources/db/migration/V2__add_demande_affectation_fields.sql"
    "DEMANDE-AFFECTATION-TPE.md"
    "RECAPITULATIF-MODIFICATIONS.md"
    "GUIDE-INSTALLATION.md"
)

for file in "${FILES_TO_CHECK[@]}"; do
    echo -n "  $file ... "
    if [ -f "$file" ]; then
        echo -e "${GREEN}✓ EXISTS${NC}"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}✗ MISSING${NC}"
        ((TESTS_FAILED++))
    fi
done

echo ""
echo "=== Résumé des Tests ==="
echo ""

TOTAL_TESTS=$((TESTS_PASSED + TESTS_FAILED))
echo "Tests réussis: $TESTS_PASSED"
echo "Tests échoués: $TESTS_FAILED"
echo "Total: $TOTAL_TESTS"
echo ""

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "${GREEN}✓ Tous les tests sont passés!${NC}"
    echo ""
    echo "Prochaines étapes:"
    echo "1. Exécuter la migration SQL: TPE/src/main/resources/db/migration/V2__add_demande_affectation_fields.sql"
    echo "2. Se connecter à l'application: $FRONTEND_URL"
    echo "3. Créer une demande de test"
    echo "4. Valider la demande avec un utilisateur Monétique"
    exit 0
else
    echo -e "${RED}✗ Certains tests ont échoué${NC}"
    echo ""
    echo "Veuillez consulter:"
    echo "- GUIDE-INSTALLATION.md pour les instructions détaillées"
    echo "- RECAPITULATIF-MODIFICATIONS.md pour la liste des changements"
    exit 1
fi

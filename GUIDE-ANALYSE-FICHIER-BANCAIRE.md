# Guide d'Analyse de Fichier Bancaire

## Vue d'ensemble

Cette fonctionnalité permet d'analyser des fichiers bancaires au format fixe et de générer des rapports détaillés en PDF ou TXT. L'analyse est effectuée localement dans le navigateur, sans transmission au serveur.

## Format de Fichier Supporté

### Structure du fichier

Le fichier doit être un fichier texte (.txt ou .dat) avec un format à positions fixes.

#### Ligne d'en-tête (Type 01)
- **Position 1-2**: Type d'enregistrement = "01"
- **Position 3-10**: Code banque
- **Position 11-18**: Date du fichier (JJMMAAAA)
- **Position 19-28**: Numéro de séquence

#### Lignes de transaction (Type 10)
- **Position 1-2**: Type d'enregistrement = "10"
- **Position 3-10**: Numéro de séquence
- **Position 11-18**: Date (JJMMAAAA)
- **Position 19-28**: Numéro terminal
- **Position 29-48**: Numéro commerçant
- **Position 49-73**: Nom commerçant
- **Position 74-110**: Localisation
- **Position 111-125**: Numéro VISA
- **Position 126-133**: Date transaction (JJMMAAAA)
- **Position 134-144**: Nombre de transactions
- **Position 145-159**: Montant brut (en millimes)
- **Position 160-174**: Commission (en millimes)
- **Position 175-189**: TVA (en millimes)
- **Position 190-204**: Montant net (en millimes)
- **Position 205-219**: Montant total (en millimes)
- **Position 230-239**: Code banque

### Exemple de fichier

```
01000001  18022622222200128                                                                                                                                                                                                                                                                                                                                                                                                                            X
1000000201180226280000016428000000501100000181    SERGENT MAJOR            MILLENIUM                            0000000000241180226000002     00000044538000000012548078830000000220820000000041960000004191030000004437800000000016000000000000000000003199006       28049  2800000164                                                                                       1100128                                                                  X
```

## Utilisation

### 1. Accéder à la page d'analyse

- Connectez-vous à l'application
- Allez dans le menu "Upload Transactions" (icône cloud_upload)
- Ou accédez directement à `/file-upload`

### 2. Sélectionner le fichier

- Cliquez sur la zone de dépôt ou glissez-déposez votre fichier
- Le fichier doit être au format .txt ou .dat
- La taille du fichier et son nom s'afficheront

### 3. Analyser le fichier

- Cliquez sur le bouton "Analyser le fichier"
- L'analyse démarre immédiatement dans le navigateur
- Un indicateur de progression s'affiche

### 4. Consulter les résultats

Après l'analyse, vous verrez:

#### Statistiques globales
- **Total lignes**: Nombre total de lignes dans le fichier
- **Traitées**: Nombre de lignes analysées avec succès
- **Erreurs**: Nombre de lignes avec des erreurs
- **Taux de réussite**: Pourcentage de lignes traitées

#### Tableau des transactions
Les transactions sont affichées avec les colonnes suivantes:
- Ligne
- Terminal
- Commerçant
- Localisation
- Date
- Nb Trans
- Montant Brut
- Commission
- Montant Net

### 5. Générer un rapport

Deux options sont disponibles:

#### Rapport PDF
- Cliquez sur "Rapport PDF"
- Un fichier PDF au format paysage sera téléchargé
- Contient: résumé, tableau des transactions, erreurs
- Format professionnel avec en-têtes et pieds de page

#### Rapport TXT
- Cliquez sur "Rapport TXT"
- Un fichier texte formaté sera téléchargé
- Contient: résumé, tableau aligné, totaux, erreurs
- Format adapté pour l'impression ou l'archivage

## Rapports générés

### Contenu du rapport PDF

1. **En-tête**
   - Titre: "Rapport d'Analyse du Fichier Bancaire"
   - Informations de la banque
   - Date et nom du fichier

2. **Informations du fichier**
   - Code banque
   - Date du fichier

3. **Résumé du traitement**
   - Tableau récapitulatif des statistiques

4. **Détail des transactions**
   - Tableau avec toutes les transactions
   - Colonnes formatées et alignées
   - Montants alignés à droite

5. **Erreurs** (si présentes)
   - Liste des lignes en erreur
   - Contenu et description de l'erreur

6. **Pied de page**
   - Numérotation des pages
   - Mention de confidentialité

### Contenu du rapport TXT

1. **En-tête ASCII**
   - Titre encadré
   - Informations de la banque
   - Date et nom du fichier

2. **Informations du fichier**
   - Code banque, date, séquence

3. **Résumé du traitement**
   - Statistiques avec séparateurs

4. **Détail des transactions**
   - Tableau formaté avec séparateurs
   - Colonnes alignées
   - Largeur fixe pour impression

5. **Totaux calculés**
   - Total Montant Brut
   - Total Commission
   - Total TVA
   - Total Montant Net

6. **Erreurs** (si présentes)
   - Liste détaillée avec numéros de ligne

7. **Pied de page**
   - Mention de confidentialité

## Traitement des montants

Les montants dans le fichier sont exprimés en millimes (1/1000 de dinar).

**Exemple:**
- Valeur dans le fichier: `000000012548`
- Valeur affichée: `12.548` TND

La conversion est automatique:
```
Montant affiché = Valeur fichier / 1000
```

## Traitement des dates

Les dates au format JJMMAAAA sont converties en DD/MM/YYYY.

**Exemple:**
- Valeur dans le fichier: `18022628`
- Valeur affichée: `18/02/2628`

## Gestion des erreurs

Les erreurs peuvent survenir pour:
- Format de ligne incorrect
- Positions de champs invalides
- Type d'enregistrement inconnu
- Données manquantes

Chaque erreur est enregistrée avec:
- Numéro de ligne
- Contenu de la ligne
- Description de l'erreur

Les lignes en erreur n'empêchent pas le traitement des autres lignes.

## Performances

- **Traitement local**: Aucune latence réseau
- **Capacité**: Peut traiter des fichiers de plusieurs milliers de lignes
- **Instantané**: Analyse en quelques secondes
- **Sécurité**: Le fichier ne quitte jamais votre navigateur

## Formats de sortie

### Format PDF
- **Orientation**: Paysage (pour plus de colonnes)
- **Police**: Helvetica
- **Tailles**: Titres 14-18pt, données 6-10pt
- **Couleurs**: En-têtes bleus, erreurs rouges
- **Tableaux**: Avec bordures et lignes alternées

### Format TXT
- **Encodage**: UTF-8
- **Largeur**: 180 caractères
- **Séparateurs**: ASCII (═, ─, |)
- **Alignement**: Texte à gauche, montants à droite
- **Espacement**: Fixe pour impression monospace

## Accès et Permissions

Cette fonctionnalité est accessible aux utilisateurs avec les rôles:
- **ADMIN**: Accès complet
- **MONETIQUE**: Accès complet

Visible dans le menu de navigation sous "Upload Transactions".

## Notes importantes

1. **Analyse locale**: Le fichier n'est pas envoyé au serveur
2. **Pas de sauvegarde**: Les données ne sont pas stockées en base
3. **Format strict**: Le fichier doit respecter les positions fixes
4. **Encodage**: UTF-8 recommandé
5. **Taille**: Aucune limite stricte, mais performances optimales < 10 MB

## Dépannage

### Le fichier ne se charge pas
- Vérifiez l'extension (.txt ou .dat)
- Vérifiez l'encodage (UTF-8)
- Vérifiez que le fichier n'est pas vide

### Erreurs de parsing
- Vérifiez que les lignes commencent par "01" ou "10"
- Vérifiez la longueur des lignes
- Vérifiez les positions des champs

### PDF ou TXT ne se télécharge pas
- Vérifiez les paramètres de téléchargement du navigateur
- Autorisez les téléchargements depuis l'application
- Vérifiez l'espace disque disponible

## Support

Pour toute question ou problème:
1. Vérifiez ce guide
2. Consultez les logs du navigateur (F12)
3. Contactez le support IT avec:
   - Nom du fichier
   - Taille du fichier
   - Message d'erreur éventuel
   - Capture d'écran

---

**Version**: 1.0  
**Date**: Février 2026  
**Auteur**: Équipe Développement TPE Management

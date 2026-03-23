# Guide de Génération PDF - Frontend Angular & Backend Java

## 📋 Vue d'ensemble

Ce guide explique comment utiliser la fonctionnalité de génération de rapports PDF des fichiers bancaires TPE avec l'intégration complète entre le frontend Angular et le backend Java Spring Boot.

## 🏗️ Architecture

### Backend (Java/Spring Boot)
- **Service**: `RapportFichierBancaireService.java`
- **Contrôleur**: `FichierBancaireController.java`
- **Bibliothèque PDF**: iText 5.5.13.3
- **Endpoint**: `GET /api/fichier-bancaire/rapport/pdf/{sessionDate}`

### Frontend (Angular)
- **Service**: `tpe-posting.service.ts`
- **Composant**: `upload-fichier-bancaire.component.ts`
- **Template**: `upload-fichier-bancaire.component.html`

## 📦 Dépendances

### Backend - pom.xml
```xml
<!-- iText pour génération PDF -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itextpdf</artifactId>
    <version>5.5.13.3</version>
</dependency>
```

### Frontend - package.json
Aucune dépendance supplémentaire requise. La gestion des blobs est native au navigateur.

## 🚀 Utilisation

### 1. Démarrer le Backend
```powershell
cd TPE
mvn clean install
mvn spring-boot:run
```

Le backend démarre sur `http://localhost:8080`

### 2. Démarrer le Frontend
```powershell
cd "front end"
npm install
npm start
```

Le frontend démarre sur `http://localhost:4200`

### 3. Générer un rapport PDF

#### Via l'interface Web:
1. Accédez à l'application: `http://localhost:4200`
2. Naviguez vers "Upload Fichier Bancaire"
3. Uploadez un fichier bancaire `.txt`
4. Après le traitement, cliquez sur **"Export PDF"**
5. Le PDF se télécharge automatiquement

#### Via l'API directement:
```powershell
# Tester l'endpoint
$sessionDate = "20260224"
Invoke-WebRequest -Uri "http://localhost:8080/api/fichier-bancaire/rapport/pdf/$sessionDate" `
    -Method GET `
    -OutFile "rapport_$sessionDate.pdf"
```

## 📄 Contenu du Rapport PDF

Le PDF généré contient:

### 1. **En-tête**
- Titre: "RAPPORT DE TRAITEMENT FICHIER BANCAIRE TPE"
- Date de session (format: dd/MM/yyyy)
- Date de génération

### 2. **Statistiques**
- Nombre total d'écritures
- Nombre de débits (DR)
- Nombre de crédits (CR)

### 3. **Tableau détaillé des écritures**
Colonnes:
- ID
- BRANCH (Agence)
- CLIENT
- ACCOUNT (Compte)
- AMOUNT (Montant)
- CR/DR (Crédit/Débit)
- REF (Référence)
- NARRATIVE (Libellé)

### 4. **Totaux**
- Total des débits (fond rouge)
- Total des crédits (fond vert)
- Solde (débits - crédits)

### 5. **Pied de page**
- Mention "Document généré automatiquement"

## 🎨 Mise en forme

- **Format**: A4 paysage (landscape)
- **Police**: Helvetica
- **Couleurs**:
  - En-tête tableau: Bleu (#337AB7)
  - Débits: Rouge
  - Crédits: Vert
  - Texte: Gris foncé

## 🔧 Code Backend Important

### Service - genererRapportPDF()
```java
public byte[] genererRapportPDF(String sessionDate) throws DocumentException {
    Document document = new Document(PageSize.A4.rotate());
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    
    try {
        PdfWriter.getInstance(document, baos);
        document.open();
        
        // En-tête
        ajouterEntetePDF(document, sessionDate);
        
        // Récupérer les écritures
        List<TPEPostingComp> ecritures = 
            tpePostingCompRepository.findBySessionDate(sessionDate);
        
        if (!ecritures.isEmpty()) {
            ajouterStatistiquesPDF(document, ecritures);
            ajouterTableauEcrituresPDF(document, ecritures);
            ajouterTotauxPDF(document, ecritures);
        }
        
        ajouterPiedDePagePDF(document);
    } finally {
        document.close();
    }
    
    return baos.toByteArray();
}
```

### Contrôleur - Endpoint PDF
```java
@GetMapping("/rapport/pdf/{sessionDate}")
public ResponseEntity<byte[]> genererRapportPDF(@PathVariable String sessionDate) {
    try {
        byte[] pdfBytes = rapportService.genererRapportPDF(sessionDate);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", 
            "rapport_fichier_bancaire_" + sessionDate + ".pdf");
        headers.setContentLength(pdfBytes.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    } catch (Exception e) {
        log.error("Erreur lors de la génération du rapport PDF", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
```

## 💻 Code Frontend Important

### Service Angular - telechargerRapportPDF()
```typescript
telechargerRapportPDF(sessionDate: string): Observable<Blob> {
  return this.http.get(
    `${this.fichierBancaireUrl}/rapport/pdf/${sessionDate}`,
    { responseType: 'blob' }
  );
}
```

### Composant - Téléchargement
```typescript
telechargerPDF(): void {
  if (!this.sessionDate || this.sessionDate.length !== 8) {
    this.errorMessage = 'Date de session invalide';
    return;
  }

  this.tpePostingService.telechargerRapportPDF(this.sessionDate)
    .subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `rapport_fichier_bancaire_${this.sessionDate}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
        console.log('✅ PDF téléchargé');
      },
      error: (error) => {
        console.error('❌ Erreur téléchargement PDF:', error);
        this.errorMessage = 'Erreur lors du téléchargement du rapport PDF';
      }
    });
}
```

## 🧪 Tests

### Test Script PowerShell
```powershell
# test-pdf-generation.ps1

$sessionDate = "20260224"
$backendUrl = "http://localhost:8080"
$endpoint = "$backendUrl/api/fichier-bancaire/rapport/pdf/$sessionDate"

Write-Host "🔍 Test de génération PDF..." -ForegroundColor Cyan

try {
    # Test de l'endpoint
    $response = Invoke-WebRequest -Uri $endpoint -Method GET -UseBasicParsing
    
    if ($response.StatusCode -eq 200) {
        $pdfPath = "test_rapport_$sessionDate.pdf"
        [System.IO.File]::WriteAllBytes($pdfPath, $response.Content)
        Write-Host "✅ PDF généré avec succès: $pdfPath" -ForegroundColor Green
        Write-Host "📊 Taille: $($response.Content.Length) bytes" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Erreur: $_" -ForegroundColor Red
}
```

### Test depuis l'interface
1. Uploader le fichier test: `test_cpabc049_sample.txt`
2. Attendre la confirmation de traitement
3. Cliquer sur "Export PDF"
4. Vérifier le téléchargement dans le dossier Téléchargements

## ⚠️ Dépannage

### Erreur: "package com.itextpdf.text.pdf does not exist"

**Solution 1**: Recharger les dépendances Maven
```powershell
cd TPE
mvn clean install -U
```

**Solution 2**: Dans VS Code/IntelliJ
- Clic droit sur le projet → Maven → Reload Project
- Ou: Ctrl+Shift+P → "Java: Clean Java Language Server Workspace"

### Erreur 404 - Endpoint non trouvé

**Vérifications**:
1. Backend démarré? → `http://localhost:8080/api/fichier-bancaire/test`
2. CORS configuré? → `@CrossOrigin(origins = "*")` dans le contrôleur
3. Mapping correct? → `@GetMapping("/rapport/pdf/{sessionDate}")`

### Erreur: PDF vide ou corrompu

**Causes possibles**:
1. SessionDate invalide (doit être format yyyyMMdd)
2. Aucune donnée pour cette session
3. Erreur de génération côté backend (vérifier les logs)

**Logs Backend à vérifier**:
```
ERROR c.b.a.t.s.RapportFichierBancaireService : Erreur lors de la génération du PDF
```

### CORS bloqué par le navigateur

**Vérifier** `FichierBancaireController.java`:
```java
@CrossOrigin(origins = "*")  // ou origins = "http://localhost:4200"
```

## 📊 Formats de rapport disponibles

1. **PDF** (iText 5)
   - Endpoint: `/api/fichier-bancaire/rapport/pdf/{sessionDate}`
   - Format: A4 paysage avec mise en forme colorée
   - Usage: Rapports officiels, archivage

2. **TXT** (Format texte)
   - Endpoint: `/api/fichier-bancaire/rapport/text/{sessionDate}`
   - Format: Texte brut avec tableaux ASCII
   - Usage: Import dans d'autres systèmes, logs

## 🔐 Sécurité

- Actuellement: CORS ouvert (`origins = "*"`)
- Production: Limiter aux domaines autorisés
- Ajouter authentification JWT si nécessaire
- Valider le format de sessionDate pour éviter injections

## 📝 Notes

- Le PDF est généré à la volée (pas de stockage sur disque)
- Format de date: `yyyyMMdd` (ex: 20260224)
- Encodage: UTF-8
- Compatible avec tous les navigateurs modernes
- Taille typique: ~50-200 KB selon le nombre d'écritures

## 🔗 Ressources

- Documentation iText 5: https://itextpdf.com/en/resources/books/itext-5-legacy
- Angular HTTP Client: https://angular.io/guide/http
- Spring ResponseEntity: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/ResponseEntity.html

## ✅ Checklist de mise en production

- [ ] Tester avec différentes sessions
- [ ] Vérifier la gestion des erreurs
- [ ] Valider l'encodage UTF-8 (caractères spéciaux)
- [ ] Configurer CORS pour production
- [ ] Ajouter logs appropriés
- [ ] Tester performance avec gros volumes
- [ ] Documenter pour utilisateurs finaux
- [ ] Backup de la base avant traitement

---

**Version**: 1.0.0  
**Date**: 25/02/2026  
**Dernière mise à jour**: Guide complet intégration PDF

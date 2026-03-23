// ⚠️ IMPORTANT : Ceci est un exemple de configuration
// NE PAS commiter ce fichier avec de vraies valeurs

export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  
  // Configuration Power BI
  powerBI: {
    // Mettre à true pour activer Power BI
    enabled: false,
    
    // Report ID - Récupérer après publication sur Power BI Service
    // Exemple : '5b218778-e7a5-4d73-8187-f10824047715'
    reportId: 'VOTRE_REPORT_ID',
    
    // Embed URL complet
    // Format : https://app.powerbi.com/reportEmbed?reportId={reportId}&groupId={workspaceId}
    // Exemple : 'https://app.powerbi.com/reportEmbed?reportId=5b218778-e7a5-4d73-8187-f10824047715&groupId=be8908da-25a4-4f96-b4c6-08455c1e3441'
    embedUrl: 'VOTRE_EMBED_URL'
  }
};

// Comment obtenir ces informations ?
// 1. Créer votre rapport dans Power BI Desktop
// 2. Publier sur Power BI Service (nécessite Power BI Pro)
// 3. Ouvrir le rapport sur app.powerbi.com
// 4. L'URL ressemble à : https://app.powerbi.com/groups/{workspaceId}/reports/{reportId}
// 5. Copier workspaceId et reportId
// 6. Former l'embedUrl comme indiqué ci-dessus

// Pour activer :
// 1. Remplacer VOTRE_REPORT_ID par votre vrai Report ID
// 2. Remplacer VOTRE_EMBED_URL par votre vraie Embed URL
// 3. Mettre enabled: true
// 4. Configurer aussi le backend (application.properties)

// The file contents for the current environment will overwrite these during build.
// The build system defaults to the dev environment which uses `environment.ts`, but if you do
// `ng build --env=prod` then `environment.prod.ts` will be used instead.
// The list of which env maps to which file can be found in `.angular-cli.json`.

export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  powerBI: {
    // Désactivé : nécessite droits admin Azure ou admin Power BI
    enabled: false,
    reportId: '0b6cd765-f966-41aa-ae2d-68f1a6ce302d',
    embedUrl: 'https://app.powerbi.com/reportEmbed?reportId=0b6cd765-f966-41aa-ae2d-68f1a6ce302d',
    publicUrl: ''
  }
};

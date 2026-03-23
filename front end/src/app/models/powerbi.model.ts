export interface PowerBIConfig {
  type: 'report' | 'dashboard' | 'tile';
  embedUrl: string;
  accessToken: string;
  id: string;
  tokenType?: 'Aad' | 'Embed';
  permissions?: 'Read' | 'ReadWrite' | 'Copy' | 'Create' | 'All';
  viewMode?: 'View' | 'Edit';
  settings?: PowerBISettings;
}

export interface PowerBISettings {
  filterPaneEnabled?: boolean;
  navContentPaneEnabled?: boolean;
  layoutType?: 'Master' | 'MobilePortrait' | 'MobileLandscape' | 'Custom';
  background?: 'Default' | 'Transparent';
}

export interface PowerBIEmbedToken {
  token: string;
  tokenId: string;
  expiration: string;
}

export interface PowerBIReport {
  id: string;
  name: string;
  embedUrl: string;
  webUrl: string;
  datasetId?: string;
}

export interface PowerBIDashboard {
  id: string;
  displayName: string;
  embedUrl: string;
  isReadOnly: boolean;
}

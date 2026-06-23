export interface AssistantRequestDTO {
  question: string;
}

export interface AssistantResponseDTO {
  question: string;
  reponseIA?: string;
  sqlGenere?: string;
  explication?: string;
  donnees: AssistantMetierRow[];
  nombreResultats: number;
  erreur: boolean;
  messageErreur?: string;
}

export interface AssistantMetierRow {
  [key: string]: any;
}

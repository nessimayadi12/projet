declare module 'jspdf-autotable' {
  import { jsPDF } from 'jspdf';
  
  interface AutoTableOptions {
    startY?: number;
    head?: any[][];
    body?: any[][];
    theme?: 'striped' | 'grid' | 'plain';
    headStyles?: any;
    bodyStyles?: any;
    columnStyles?: any;
    margin?: any;
    styles?: any;
  }

  export default function autoTable(doc: jsPDF, options: AutoTableOptions): void;
}

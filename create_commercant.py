#!/usr/bin/env python3
"""
Créer un commerçant de test
"""
import mysql.connector
from datetime import datetime

try:
    conn = mysql.connector.connect(host='localhost', user='root', password='', database='TPE_Managements', autocommit=True)
    cursor = conn.cursor()
    
    print("=" * 80)
    print("📋 CRÉATION COMMERÇANT DE TEST")
    print("=" * 80)
    
    # Vérifier si le commerçant existe
    cursor.execute('SELECT id FROM commercants WHERE raison_sociale = "Carrefour Manouba"')
    existing = cursor.fetchone()
    
    if existing:
        print("✅ Commerçant existe déjà (ID: {})".format(existing[0]))
        commercant_id = existing[0]
    else:
        # Créer le commerçant
        sql = """INSERT INTO commercants 
                 (created_date, raison_sociale, activite, numero_compte, code_agence, 
                  adresse, localite, code_postal, email, statut)
                 VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"""
        
        cursor.execute(sql, (
            datetime.now(),
            'Carrefour Manouba',
            'Distribution & Retail',
            'ACC123456789',
            'AGENCE001',
            '123 Avenue Principal',
            'Manouba',
            '2033',
            'carrefour.manouba@bank-abc.com',
            'ACTIF'
        ))
        
        # Récupérer l'ID du commerçant inséré
        cursor.execute('SELECT LAST_INSERT_ID()')
        commercant_id = cursor.fetchone()[0]
        print(f"✅ Commerçant créé: Carrefour Manouba (ID: {commercant_id})")
    
    # Vérifier
    cursor.execute('SELECT id, raison_sociale, email, statut FROM commercants WHERE id = %s', (commercant_id,))
    row = cursor.fetchone()
    if row:
        print(f"\n📋 COMMERÇANT TEST:")
        print(f"   ID: {row[0]}")
        print(f"   Nom: {row[1]}")
        print(f"   Email: {row[2]}")
        print(f"   Statut: {row[3]}")
    
    print("\n" + "=" * 80)
    print("✅ Commerçant prêt pour les tests")
    print("=" * 80)
    
    cursor.close()
    conn.close()
    
except Exception as e:
    print(f"❌ Error: {e}")
    import traceback
    traceback.print_exc()

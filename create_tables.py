#!/usr/bin/env python3
"""
Créer les tables manquantes
"""
import mysql.connector
import sys

try:
    conn = mysql.connector.connect(host='localhost', user='root', password='', database='TPE_Managements', autocommit=True)
    cursor = conn.cursor()
    
    # Lire le script SQL
    with open(r'c:\Users\Nessim\OneDrive\Desktop\projet\TPE\src\main\resources\create_missing_tables.sql', 'r') as f:
        sql_script = f.read()
    
    print("=" * 80)
    print("📋 CRÉATION DES TABLES MANQUANTES")
    print("=" * 80)
    
    # Exécuter chaque requête
    for statement in sql_script.split(';'):
        statement = statement.strip()
        if statement and not statement.startswith('--'):
            try:
                cursor.execute(statement)
                if 'CREATE TABLE' in statement:
                    table_name = statement.split('CREATE TABLE')[1].split('(')[0].strip().replace('IF NOT EXISTS', '').strip()
                    print(f"✅ Table créée/vérifiée: {table_name}")
            except mysql.connector.Error as e:
                if 'already exists' in str(e):
                    print(f"ℹ️  {e.msg[:50]}...")
                else:
                    print(f"⚠️  {e.msg[:100]}")
    
    # Vérifier les tables
    print("\n📊 TABLES EXISTANTES:")
    cursor.execute("SHOW TABLES")
    for row in cursor.fetchall():
        print(f"   ✅ {row[0]}")
    
    print("\n" + "=" * 80)
    print("✅ Toutes les tables sont prêtes")
    print("=" * 80)
    
    cursor.close()
    conn.close()
    
except Exception as e:
    print(f"❌ Error: {e}")
    sys.exit(1)

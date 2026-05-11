#!/usr/bin/env python3
"""
Diagnostic complet: BD + utilisateurs + rôles
"""
import mysql.connector
import sys

try:
    conn = mysql.connector.connect(host='localhost', user='root', password='', database='TPE_Managements', autocommit=True)
    cursor = conn.cursor()
    
    print("=" * 80)
    print("🔍 DIAGNOSTIC COMPLET - BASE DE DONNÉES")
    print("=" * 80)
    
    # 1. Vérifier les rôles
    print("\n1️⃣  RÔLES DISPONIBLES:")
    cursor.execute('SELECT id, name FROM roles LIMIT 10')
    for row in cursor.fetchall():
        print(f"   {row[0]:2} → {row[1]}")
    
    # 2. Vérifier inputer_user
    print("\n2️⃣  UTILISATEUR: inputer_user")
    cursor.execute('SELECT id, username, email, actif, account_locked FROM users WHERE username = "inputer_user"')
    user = cursor.fetchone()
    if user:
        print(f"   ✅ ID: {user[0]}")
        print(f"   ✅ Username: {user[1]}")
        print(f"   ✅ Email: {user[2]}")
        print(f"   ✅ Actif: {user[3]}")
        print(f"   ✅ Account Locked: {user[4]}")
        
        # Vérifier les rôles
        cursor.execute('''
            SELECT r.name FROM roles r
            JOIN user_roles ur ON r.id = ur.role_id
            WHERE ur.user_id = %s
        ''', (user[0],))
        roles = [r[0] for r in cursor.fetchall()]
        print(f"   ✅ Rôles: {roles if roles else 'AUCUN'}")
    else:
        print("   ❌ Utilisateur introuvable!")
    
    # 3. Vérifier authorizer_user
    print("\n3️⃣  UTILISATEUR: authorizer_user")
    cursor.execute('SELECT id, username, email, actif, account_locked FROM users WHERE username = "authorizer_user"')
    user = cursor.fetchone()
    if user:
        print(f"   ✅ ID: {user[0]}")
        print(f"   ✅ Username: {user[1]}")
        print(f"   ✅ Email: {user[2]}")
        print(f"   ✅ Actif: {user[3]}")
        print(f"   ✅ Account Locked: {user[4]}")
        
        # Vérifier les rôles
        cursor.execute('''
            SELECT r.name FROM roles r
            JOIN user_roles ur ON r.id = ur.role_id
            WHERE ur.user_id = %s
        ''', (user[0],))
        roles = [r[0] for r in cursor.fetchall()]
        print(f"   ✅ Rôles: {roles if roles else 'AUCUN'}")
    else:
        print("   ❌ Utilisateur introuvable!")
    
    # 4. Vérifier les tables critiques
    print("\n4️⃣  TABLES CRITIQUES:")
    tables = ['users', 'roles', 'user_roles', 'taux', 'commercant']
    cursor.execute("SELECT table_name FROM information_schema.tables WHERE table_schema='TPE_Managements'")
    existing = [r[0] for r in cursor.fetchall()]
    for t in tables:
        status = "✅" if t in existing else "❌"
        print(f"   {status} {t}")
    
    # 5. Compter les records
    print("\n5️⃣  NOMBRE DE RECORDS:")
    for table in ['users', 'roles', 'taux', 'commercant']:
        cursor.execute(f"SELECT COUNT(*) FROM {table}")
        count = cursor.fetchone()[0]
        print(f"   {table:15} : {count:5} records")
    
    print("\n" + "=" * 80)
    print("✅ BD diagnostiquée avec succès")
    print("=" * 80)
    
    cursor.close()
    conn.close()
    
except mysql.connector.Error as err:
    print(f"❌ MySQL Error: {err}")
    sys.exit(1)
except Exception as e:
    print(f"❌ Error: {e}")
    sys.exit(1)

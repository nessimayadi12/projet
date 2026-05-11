#!/usr/bin/env python3
import mysql.connector
import sys

# Le bon hash BCrypt pour "Password123!"
correct_hash = '$2b$10$xu20eblhRmwpZk.6uqw4iOlSA4rTEsPgibEjaWkJ2uoODev.77nNa'

try:
    conn = mysql.connector.connect(host='localhost', user='root', password='', database='TPE_Managements')
    cursor = conn.cursor()
    
    # Mettre à jour les deux utilisateurs
    cursor.execute(
        "UPDATE users SET password = %s WHERE username IN ('inputer_user', 'authorizer_user')",
        (correct_hash,)
    )
    conn.commit()
    
    affected = cursor.rowcount
    print(f"✅ Updated {affected} user password(s)")
    
    # Vérifier
    cursor.execute('SELECT username, password FROM users WHERE username IN ("inputer_user", "authorizer_user")')
    for row in cursor.fetchall():
        print(f"   {row[0]:20} password: {row[1][:30]}...")
    
    cursor.close()
    conn.close()
    
except Exception as e:
    print(f"❌ Error: {e}")
    sys.exit(1)

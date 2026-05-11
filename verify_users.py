#!/usr/bin/env python3
import mysql.connector

conn = mysql.connector.connect(host='localhost', user='root', password='', database='TPE_Managements')
cursor = conn.cursor()

# Vérifier les utilisateurs
cursor.execute('SELECT id, username, email, actif FROM users WHERE username IN ("inputer_user", "authorizer_user")')
print("📋 Users in Database:")
for row in cursor.fetchall():
    print(f"  ID:{row[0]:3} | User:{row[1]:20} | Email:{row[2]} | Actif:{row[3]}")

# Vérifier les rôles
cursor.execute('''
    SELECT u.username, r.name
    FROM users u
    LEFT JOIN user_roles ur ON u.id = ur.user_id
    LEFT JOIN roles r ON ur.role_id = r.id
    WHERE u.username IN ("inputer_user", "authorizer_user")
''')
print("\n📋 User Roles:")
for row in cursor.fetchall():
    print(f"  {row[0]:20} → {row[1]}")

cursor.close()
conn.close()

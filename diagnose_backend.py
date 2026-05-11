#!/usr/bin/env python3
"""
Diagnostic script pour vérifier le backend et l'authentification
"""
import mysql.connector
import requests
import json

print("=" * 80)
print("🔍 DIAGNOSTIC BACKEND & AUTHENTIFICATION")
print("=" * 80)

# 1. Vérifier le backend
print("\n1️⃣  VÉRIFYING BACKEND CONNECTIVITY")
try:
    response = requests.get('http://localhost:8080/api/health', timeout=2)
    print(f"   ✅ Backend accessible: {response.status_code}")
except Exception as e:
    print(f"   ❌ Backend NOT RESPONDING: {e}")
    print("   → Lancez le backend Spring Boot dans IntelliJ!")

# 2. Essayer de login
print("\n2️⃣  TESTING LOGIN ENDPOINT")
login_payload = {
    "username": "inputer_user",
    "password": "Password123!"
}

try:
    response = requests.post(
        'http://localhost:8080/api/auth/login',
        json=login_payload,
        timeout=5
    )
    print(f"   Status: {response.status_code}")
    if response.status_code == 200:
        data = response.json()
        print(f"   ✅ LOGIN SUCCESS!")
        print(f"      Token: {data.get('accessToken', 'N/A')[:50]}...")
    else:
        print(f"   ❌ LOGIN FAILED: {response.text[:200]}")
except Exception as e:
    print(f"   ❌ Error: {e}")

# 3. Vérifier l'utilisateur en BD
print("\n3️⃣  CHECKING DATABASE")
conn = mysql.connector.connect(host='localhost', user='root', password='', database='TPE_Managements')
cursor = conn.cursor()

cursor.execute('SELECT id, username, password, actif FROM users WHERE username = %s', ('inputer_user',))
row = cursor.fetchone()
if row:
    print(f"   ✅ User found:")
    print(f"      ID: {row[0]}")
    print(f"      Username: {row[1]}")
    print(f"      Password hash: {row[2][:30]}...")
    print(f"      Active: {row[3]}")
else:
    print(f"   ❌ User NOT FOUND")

cursor.execute('SELECT r.name FROM users u LEFT JOIN user_roles ur ON u.id = ur.user_id LEFT JOIN roles r ON ur.role_id = r.id WHERE u.username = %s', ('inputer_user',))
roles = [r[0] for r in cursor.fetchall() if r[0]]
print(f"      Roles: {roles if roles else 'NONE'}")

cursor.close()
conn.close()

print("\n" + "=" * 80)
print("📋 TROUBLESHOOTING STEPS:")
print("=" * 80)
print("1. Vérifiez que le backend Spring Boot tourne dans IntelliJ")
print("2. Vérifiez les logs IntelliJ pour les erreurs d'authentification")
print("3. Assurez-vous que le port 8080 n'est pas bloqué")
print("4. Vérifiez la configuration JwtAuthenticationFilter au backend")

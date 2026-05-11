#!/usr/bin/env python3
"""
Test login après redémarrage du backend
"""
import requests
import time

print("=" * 80)
print("🧪 TEST LOGIN - PROCESSUS 4 YEUX")
print("=" * 80)

login_data = {
    'username': 'inputer',
    'password': 'Inputer@123'
}

print("\n⏳ Attendre que le backend soit prêt...")
time.sleep(2)

print("\n🔐 Tentative de connexion...")
try:
    response = requests.post(
        'http://localhost:8080/api/auth/login',
        json=login_data,
        timeout=10
    )
    
    if response.status_code == 200:
        data = response.json()
        print("\n✅ LOGIN RÉUSSI!")
        print(f"   Token Type: {data.get('tokenType')}")
        print(f"   Expires In: {data.get('expiresIn')} ms (24h)")
        print(f"   Access Token: {data.get('accessToken')[:80]}...")
        print("\n✅ Vous pouvez maintenant accéder à l'interface!")
        print("   http://localhost:4200/#/login")
        
    elif response.status_code == 401:
        print(f"\n❌ Bad credentials (401)")
        print(f"   Message: {response.text[:300]}")
        
    else:
        print(f"\n❌ Status {response.status_code}")
        print(f"   Response: {response.text[:300]}")
        
except requests.exceptions.ConnectionError:
    print("\n❌ BACKEND NOT ACCESSIBLE!")
    print("   ❌ Vérifiez que le backend est en cours d'exécution sur le port 8080")
    print("   ❌ Relancez le backend dans IntelliJ")
    
except Exception as e:
    print(f"\n❌ Error: {e}")

print("\n" + "=" * 80)

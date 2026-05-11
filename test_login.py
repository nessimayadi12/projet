#!/usr/bin/env python3
import requests

login_data = {
    'username': 'inputer_user',
    'password': 'Password123!'
}

try:
    response = requests.post('http://localhost:8080/api/auth/login', json=login_data, timeout=5)
    if response.status_code == 200:
        data = response.json()
        print('✅ LOGIN SUCCESS!')
        print(f"Token: {data.get('accessToken', 'N/A')[:80]}...")
        print(f"Token type: {data.get('tokenType')}")
        print(f"Expires in: {data.get('expiresIn')} ms")
    else:
        print(f'❌ Status {response.status_code}')
        print(f'Response: {response.text[:200]}')
except Exception as e:
    print(f'❌ Error: {e}')

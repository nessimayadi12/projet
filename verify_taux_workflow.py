#!/usr/bin/env python3
import requests
import sys

BASE = 'http://localhost:8080'


def login(username, password):
    r = requests.post(f'{BASE}/api/auth/login', json={'username': username, 'password': password}, timeout=10)
    r.raise_for_status()
    data = r.json()
    token = data.get('token') or data.get('accessToken')
    if not token:
        raise RuntimeError(f'No token in response: {data}')
    return token


def headers(token):
    return {'Authorization': f'Bearer {token}', 'Content-Type': 'application/json'}


def main():
    print('1) Login inputer')
    inputer_token = login('inputer', 'Inputer@123')
    print('   OK')

    print('2) Login authorizer')
    authorizer_token = login('authorizer', 'Authorizer@123')
    print('   OK')

    print('3) Create taux')
    create_payload = {
        'commercantId': 610,
        'nouveauTauxCommission': 2.5,
        'nouveauTauxCommissionInter': 1.75,
        'commentaire': 'Test 4 yeux'
    }
    r = requests.post(f'{BASE}/api/taux', json=create_payload, headers=headers(inputer_token), timeout=10)
    print('   status', r.status_code)
    r.raise_for_status()
    taux = r.json()
    taux_id = taux['id']
    print('   taux_id', taux_id, 'statut', taux.get('statut'))

    print('4) Submit taux')
    r = requests.post(f'{BASE}/api/taux/{taux_id}/soumettre', headers=headers(inputer_token), timeout=10)
    print('   status', r.status_code)
    r.raise_for_status()
    taux = r.json()
    print('   statut', taux.get('statut'))

    print('5) Get en attente')
    r = requests.get(f'{BASE}/api/taux/en-attente', headers=headers(authorizer_token), timeout=10)
    print('   status', r.status_code)
    r.raise_for_status()
    waiting = r.json()
    print('   waiting_count', len(waiting))

    print('6) Authorizer validates taux')
    r = requests.post(f'{BASE}/api/taux/{taux_id}/valider', json={'approuver': True, 'motifRejet': None}, headers=headers(authorizer_token), timeout=10)
    print('   status', r.status_code)
    r.raise_for_status()
    taux = r.json()
    print('   statut', taux.get('statut'), 'actif', taux.get('actif'))

    print('7) Inputer tries to validate own taux (should fail)')
    r = requests.post(f'{BASE}/api/taux/{taux_id}/valider', json={'approuver': True, 'motifRejet': None}, headers=headers(inputer_token), timeout=10)
    print('   status', r.status_code)
    print('   body', r.text[:300])

    print('SUCCESS: Taux workflow integration is working')


if __name__ == '__main__':
    try:
        main()
    except Exception as exc:
        print('FAILED:', exc)
        sys.exit(1)

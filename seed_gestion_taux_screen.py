#!/usr/bin/env python3
import mysql.connector

conn = mysql.connector.connect(host='localhost', user='root', password='', database='TPE_Managements', autocommit=True)
cur = conn.cursor(dictionary=True)

roles = {}
cur.execute("SELECT id, name FROM roles WHERE name IN ('ROLE_ADMIN','ROLE_MONETIQUE','ROLE_INPUTER','ROLE_AUTHORIZER')")
for row in cur.fetchall():
    roles[row['name']] = row['id']

screen = {
    'code': 'GESTION_TAUX',
    'libelle': 'Gestion Taux',
    'description': 'Gestion des taux TPE et processus 4 yeux',
    'route': '/taux',
    'icon': 'percent',
    'ordre': 14,
    'actif': 1,
}

cur.execute("SELECT id FROM screens WHERE code = %s", (screen['code'],))
row = cur.fetchone()
if row:
    screen_id = row['id']
    cur.execute(
        """
        UPDATE screens
        SET libelle=%s, description=%s, route=%s, icon=%s, ordre=%s, actif=%s
        WHERE id=%s
        """,
        (screen['libelle'], screen['description'], screen['route'], screen['icon'], screen['ordre'], screen['actif'], screen_id)
    )
    print(f'Updated screen {screen["code"]} (id={screen_id})')
else:
    cur.execute(
        """
        INSERT INTO screens (code, libelle, description, route, icon, ordre, actif, created_date)
        VALUES (%s,%s,%s,%s,%s,%s,%s,NOW())
        """,
        (screen['code'], screen['libelle'], screen['description'], screen['route'], screen['icon'], screen['ordre'], screen['actif'])
    )
    screen_id = cur.lastrowid
    print(f'Inserted screen {screen["code"]} (id={screen_id})')

# Reset permissions for this screen to avoid duplicates/inconsistencies
cur.execute("DELETE FROM screen_roles WHERE screen_id = %s", (screen_id,))

permissions = [
    ('ROLE_ADMIN', True, True, True, True, True),
    ('ROLE_MONETIQUE', True, True, True, True, True),
    ('ROLE_INPUTER', True, False, False, False, False),
    ('ROLE_AUTHORIZER', True, False, False, False, False),
]

for role_name, can_view, can_create, can_edit, can_delete, can_export in permissions:
    role_id = roles.get(role_name)
    if not role_id:
        print(f'Skip missing role {role_name}')
        continue
    cur.execute(
        """
        INSERT INTO screen_roles (screen_id, role_id, can_view, can_create, can_edit, can_delete, can_export, created_at, updated_at)
        VALUES (%s,%s,%s,%s,%s,%s,%s,NOW(),NOW())
        """,
        (screen_id, role_id, int(can_view), int(can_create), int(can_edit), int(can_delete), int(can_export))
    )
    print(f'Granted {role_name} on {screen["code"]}')

cur.close()
conn.close()
print('DONE')

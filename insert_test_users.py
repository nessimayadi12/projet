#!/usr/bin/env python3
"""
Insert test users for 4 Eyes Taux process testing
Users: inputer_user (INPUTER role) + authorizer_user (AUTHORIZER role)
Password: Password123! (BCrypt hash)
"""

import mysql.connector
import sys

# MySQL connection config
config = {
    'host': 'localhost',
    'user': 'root',
    'password': '',
    'database': 'TPE_Managements'
}

# Test user data
users_data = [
    {
        'username': 'inputer_user',
        'password': '$2a$10$dXJ3SVlmUEg.RA.9z3OQUeEWNzHA5aM7DQcsbqgQXJ3GQhzXm',
        'nom': 'Ayadi',
        'prenom': 'Nessim',
        'email': 'nessim.inputer@bank-abc.com',
        'telephone': '+216 95 123 456',
        'code_agence': 'AGENCE001',
        'role': 'ROLE_INPUTER'
    },
    {
        'username': 'authorizer_user',
        'password': '$2a$10$dXJ3SVlmUEg.RA.9z3OQUeEWNzHA5aM7DQcsbqgQXJ3GQhzXm',
        'nom': 'Raoudha',
        'prenom': 'Aymen',
        'email': 'aymen.raoudha@bank-abc.com',
        'telephone': '+216 95 789 012',
        'code_agence': 'AGENCE001',
        'role': 'ROLE_AUTHORIZER'
    }
]

try:
    # Connect to MySQL
    conn = mysql.connector.connect(**config)
    cursor = conn.cursor()
    
    print("✅ Connected to MySQL database: TPE_Managements")
    
    # Ensure roles exist
    roles = ['ROLE_INPUTER', 'ROLE_AUTHORIZER']
    for role in roles:
        cursor.execute(
            "INSERT IGNORE INTO roles (name, description) VALUES (%s, %s)",
            (role, f'Test user for {role}')
        )
    conn.commit()
    print("✅ Roles ensured in database")
    
    # Delete existing test users
    cursor.execute("DELETE FROM user_roles WHERE user_id IN (SELECT id FROM users WHERE username IN ('inputer_user', 'authorizer_user'))")
    cursor.execute("DELETE FROM users WHERE username IN ('inputer_user', 'authorizer_user')")
    conn.commit()
    print("✅ Deleted old test users")
    
    # Insert new users and assign roles
    for user in users_data:
        # Insert user (created_date is auto-set by JPA)
        cursor.execute(
            """INSERT INTO users 
               (username, password, nom, prenom, email, telephone, code_agence, actif, 
                failed_login_attempts, account_locked, created_date)
               VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, NOW())""",
            (user['username'], user['password'], user['nom'], user['prenom'], 
             user['email'], user['telephone'], user['code_agence'], 1, 0, 0)
        )
        conn.commit()
        
        # Get user ID
        cursor.execute("SELECT id FROM users WHERE username = %s", (user['username'],))
        user_id = cursor.fetchone()[0]
        
        # Get role ID
        cursor.execute("SELECT id FROM roles WHERE name = %s", (user['role'],))
        role_id = cursor.fetchone()[0]
        
        # Assign role
        cursor.execute(
            "INSERT INTO user_roles (user_id, role_id) VALUES (%s, %s)",
            (user_id, role_id)
        )
        conn.commit()
        print(f"✅ Created user: {user['username']} ({user['role']})")
    
    # Verify
    cursor.execute("""
        SELECT u.username, u.nom, u.prenom, u.email, r.name as role
        FROM users u
        LEFT JOIN user_roles ur ON u.id = ur.user_id
        LEFT JOIN roles r ON ur.role_id = r.id
        WHERE u.username IN ('inputer_user', 'authorizer_user')
        ORDER BY u.username
    """)
    
    print("\n📋 Test Users Created:")
    print("-" * 80)
    for row in cursor.fetchall():
        print(f"  • {row[0]:20} | {row[1]} {row[2]:15} | {row[3]:30} | {row[4]}")
    
    cursor.close()
    conn.close()
    
    print("\n✅ SUCCESS: Test users ready for 4 Eyes workflow testing!")
    print("   Username: inputer_user | Password: Password123! | Role: INPUTER")
    print("   Username: authorizer_user | Password: Password123! | Role: AUTHORIZER")
    
except mysql.connector.Error as err:
    print(f"❌ Database Error: {err}")
    sys.exit(1)
except Exception as e:
    print(f"❌ Error: {e}")
    sys.exit(1)

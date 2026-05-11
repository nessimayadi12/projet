-- ============================================
-- INSERT TEST USERS FOR 4 EYES PROCESS
-- ============================================

-- Step 1: Ensure roles exist
INSERT INTO roles (name, description) VALUES ('ROLE_INPUTER', 'Utilisateur qui saisit les taux')
ON DUPLICATE KEY UPDATE description = description;

INSERT INTO roles (name, description) VALUES ('ROLE_AUTHORIZER', 'Utilisateur qui valide/rejette les taux')
ON DUPLICATE KEY UPDATE description = description;

-- Step 2: Get role IDs
SET @inputer_role_id = (SELECT id FROM roles WHERE name = 'ROLE_INPUTER' LIMIT 1);
SET @authorizer_role_id = (SELECT id FROM roles WHERE name = 'ROLE_AUTHORIZER' LIMIT 1);

-- Step 3: Delete existing test users if they exist
DELETE FROM user_roles WHERE user_id IN (SELECT id FROM users WHERE username IN ('inputer_user', 'authorizer_user'));
DELETE FROM users WHERE username IN ('inputer_user', 'authorizer_user');

-- Step 4: Insert INPUTER test user
-- Password: "Password123!" (hash: $2a$10$dXJ3SVlmUEg.RA.9z3OQUeEWNzHA5aM7DQcsbqgQXJ3GQhzXm)
INSERT INTO users (username, password, nom, prenom, email, telephone, code_agence, actif, failed_login_attempts, account_locked, created_at, updated_at)
VALUES (
  'inputer_user',
  '$2a$10$dXJ3SVlmUEg.RA.9z3OQUeEWNzHA5aM7DQcsbqgQXJ3GQhzXm',
  'Ayadi',
  'Nessim',
  'nessim.inputer@bank-abc.com',
  '+216 95 123 456',
  'AGENCE001',
  true,
  0,
  false,
  NOW(),
  NOW()
);

-- Step 5: Insert AUTHORIZER test user
-- Password: "Password123!" (same hash)
INSERT INTO users (username, password, nom, prenom, email, telephone, code_agence, actif, failed_login_attempts, account_locked, created_at, updated_at)
VALUES (
  'authorizer_user',
  '$2a$10$dXJ3SVlmUEg.RA.9z3OQUeEWNzHA5aM7DQcsbqgQXJ3GQhzXm',
  'Raoudha',
  'Aymen',
  'aymen.raoudha@bank-abc.com',
  '+216 95 789 012',
  'AGENCE001',
  true,
  0,
  false,
  NOW(),
  NOW()
);

-- Step 6: Assign roles
SET @inputer_user_id = (SELECT id FROM users WHERE username = 'inputer_user' LIMIT 1);
SET @authorizer_user_id = (SELECT id FROM users WHERE username = 'authorizer_user' LIMIT 1);

INSERT INTO user_roles (user_id, role_id)
VALUES (@inputer_user_id, @inputer_role_id);

INSERT INTO user_roles (user_id, role_id)
VALUES (@authorizer_user_id, @authorizer_role_id);

-- Verify
SELECT 'TEST USERS CREATED' as status;
SELECT u.username, u.nom, u.prenom, u.email, r.name as role
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
WHERE u.username IN ('inputer_user', 'authorizer_user')
ORDER BY u.username;

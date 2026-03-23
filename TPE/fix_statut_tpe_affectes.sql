-- Script pour mettre à jour le statut des TPE qui ont des affectations actives
-- mais dont le statut n'est pas AFFECTE

USE TPE_Managements;
GO

-- 1. Vérifier l'état actuel
PRINT '=== AVANT CORRECTION ==='
SELECT 
    statut,
    COUNT(*) as nombre
FROM tpes
GROUP BY statut
ORDER BY statut;

PRINT ''
PRINT '=== TPE avec affectations actives mais statut différent de AFFECTE ==='
SELECT 
    t.id,
    t.numero_terminal,
    t.numero_serie,
    t.statut,
    c.raison_sociale as commercant,
    a.date_affectation
FROM tpes t
INNER JOIN affectations a ON t.id = a.tpe_id
INNER JOIN commercants c ON a.commercant_id = c.id
WHERE a.actif = 1 
  AND t.statut != 'AFFECTE';

-- 2. Mettre à jour les TPE qui ont des affectations actives
PRINT ''
PRINT '=== MISE À JOUR DES STATUTS ==='
UPDATE t
SET t.statut = 'AFFECTE'
FROM tpes t
INNER JOIN affectations a ON t.id = a.tpe_id
WHERE a.actif = 1 
  AND t.statut != 'AFFECTE';

PRINT CAST(@@ROWCOUNT AS VARCHAR) + ' TPE mis à jour'

-- 3. Vérifier après correction
PRINT ''
PRINT '=== APRÈS CORRECTION ==='
SELECT 
    statut,
    COUNT(*) as nombre
FROM tpes
GROUP BY statut
ORDER BY statut;

-- 4. Vérifier les affectations actives
PRINT ''
PRINT '=== AFFECTATIONS ACTIVES ==='
SELECT 
    t.numero_terminal,
    t.statut as statut_tpe,
    c.raison_sociale as commercant,
    a.date_affectation,
    d.reference as demande_ref,
    d.statut as statut_demande
FROM affectations a
INNER JOIN tpes t ON a.tpe_id = t.id
INNER JOIN commercants c ON a.commercant_id = c.id
INNER JOIN demandes d ON a.demande_id = d.id
WHERE a.actif = 1
ORDER BY a.date_affectation DESC;

GO

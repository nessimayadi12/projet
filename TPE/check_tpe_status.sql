-- Vérifier l'état actuel de tous les TPE
USE TPE_Managements;
GO

SELECT 
    statut,
    COUNT(*) as nombre
FROM tpes
GROUP BY statut
ORDER BY statut;

PRINT ''
PRINT 'Détail des TPE :'
SELECT 
    id,
    numero_terminal,
    numero_serie,
    statut,
    marque,
    modele
FROM tpes
ORDER BY id;

PRINT ''
PRINT 'TPE avec affectations actives :'
SELECT 
    t.id,
    t.numero_terminal,
    t.statut as statut_tpe,
    c.raison_sociale as commercant,
    a.date_affectation,
    d.reference as demande_ref
FROM tpes t
LEFT JOIN affectations a ON t.id = a.tpe_id AND a.actif = 1
LEFT JOIN commercants c ON a.commercant_id = c.id
LEFT JOIN demandes d ON a.demande_id = d.id
ORDER BY t.id;

GO

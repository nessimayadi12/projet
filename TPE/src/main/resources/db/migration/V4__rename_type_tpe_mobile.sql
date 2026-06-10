-- Migration: Renommage des types TPE
-- PHYSIQUE devient TPE, ECOMMERCE devient MOBILE.

UPDATE demandes
SET type_demande = 'TPE'
WHERE type_demande = 'PHYSIQUE';

UPDATE demandes
SET type_demande = 'MOBILE'
WHERE type_demande = 'ECOMMERCE';

UPDATE tpes
SET typetpe = 'TPE'
WHERE typetpe = 'PHYSIQUE';

UPDATE tpes
SET typetpe = 'MOBILE'
WHERE typetpe = 'ECOMMERCE';

UPDATE commercants
SET type_commerce = 'TPE'
WHERE type_commerce = 'PHYSIQUE';

UPDATE commercants
SET type_commerce = 'MOBILE'
WHERE type_commerce = 'ECOMMERCE';

-- Migration : ajout des champs de période d'inscription sur l'année scolaire
-- À exécuter une seule fois sur la base de données

ALTER TABLE lbs.lbs_annee_scolaire
  ADD COLUMN IF NOT EXISTS lbs_ansc_en_cours BOOLEAN DEFAULT FALSE,
  ADD COLUMN IF NOT EXISTS lbs_ansc_date_ouverture_inscription DATE,
  ADD COLUMN IF NOT EXISTS lbs_ansc_date_cloture_inscription DATE;

-- Exemple : marquer l'année 2024-2025 comme année en cours avec une période d'inscription ouverte
-- UPDATE lbs.lbs_annee_scolaire
--   SET lbs_ansc_en_cours = TRUE,
--       lbs_ansc_date_ouverture_inscription = '2025-06-01',
--       lbs_ansc_date_cloture_inscription   = '2025-09-30'
-- WHERE lbs_ansc_code = '2024-2025';

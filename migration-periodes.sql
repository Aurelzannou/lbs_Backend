-- Migration : création de la table des périodes d'inscription
-- À exécuter une seule fois sur la base de données

CREATE TABLE IF NOT EXISTS lbs.lbs_periode_inscription (
    id                         BIGSERIAL PRIMARY KEY,
    lbs_pins_uuid              VARCHAR(50)  NOT NULL UNIQUE,
    lbs_pins_libelle           VARCHAR(150),
    lbs_pins_annee_scolaire_id BIGINT NOT NULL REFERENCES lbs.lbs_annee_scolaire(id),
    lbs_pins_date_ouverture    DATE NOT NULL,
    lbs_pins_date_cloture      DATE NOT NULL,
    lbs_pins_actif             BOOLEAN DEFAULT TRUE,
    lbs_pins_modifier_le       TIMESTAMP,
    lbs_pins_modifier_par      VARCHAR(100)
);

-- Fix pre-existant : cast de la colonne souffrant en boolean
ALTER TABLE lbs.lbs_eleve
  ALTER COLUMN lbs_elev_souffrant TYPE boolean
  USING lbs_elev_souffrant::boolean;

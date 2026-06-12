-- Migration : création des tables de périodes d'inscription et de rentrée
-- À exécuter une seule fois sur la base de données

CREATE TABLE IF NOT EXISTS lbs.lbs_periode_inscription (
    id                    BIGSERIAL PRIMARY KEY,
    lbs_pins_uuid         VARCHAR(50)  NOT NULL UNIQUE,
    lbs_pins_libelle      VARCHAR(150),
    lbs_pins_annee_scolaire_id BIGINT NOT NULL REFERENCES lbs.lbs_annee_scolaire(id),
    lbs_pins_date_ouverture    DATE NOT NULL,
    lbs_pins_date_cloture      DATE NOT NULL,
    lbs_pins_actif             BOOLEAN DEFAULT TRUE,
    lbs_pins_modifier_le       TIMESTAMP,
    lbs_pins_modifier_par      VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS lbs.lbs_periode_rentree (
    id                    BIGSERIAL PRIMARY KEY,
    lbs_pren_uuid         VARCHAR(50)  NOT NULL UNIQUE,
    lbs_pren_libelle      VARCHAR(150),
    lbs_pren_annee_scolaire_id BIGINT NOT NULL REFERENCES lbs.lbs_annee_scolaire(id),
    lbs_pren_date_ouverture    DATE NOT NULL,
    lbs_pren_date_cloture      DATE NOT NULL,
    lbs_pren_actif             BOOLEAN DEFAULT TRUE,
    lbs_pren_modifier_le       TIMESTAMP,
    lbs_pren_modifier_par      VARCHAR(100)
);

#!/usr/bin/env bash
# ============================================================
#  LBS — sauvegarde des données de production
#  À placer dans /opt/lbs/backup.sh sur le serveur, puis :
#    chmod +x /opt/lbs/backup.sh
#    crontab -e   (utilisateur `deploy`)
#    0 3 * * *  /opt/lbs/backup.sh >> /opt/lbs/backups/backup.log 2>&1
#  (tous les jours à 3h du matin, heure serveur)
# ============================================================
set -euo pipefail

STACK_DIR="/opt/lbs"
BACKUP_DIR="$STACK_DIR/backups"
RETENTION_JOURS=14
DATE=$(date +%Y%m%d-%H%M%S)

cd "$STACK_DIR"
# On n'utilise PAS `source .env` : certaines valeurs (ex. MAIL_FROM="Nom <adresse>") contiennent
# des caractères spéciaux pour bash (<, espaces) — inoffensifs pour Docker Compose (qui lit le
# fichier comme de simples paires KEY=VALEUR), mais invalides en shell. On extrait donc
# uniquement les deux variables dont ce script a besoin.
APP_DB_USER=$(grep -E '^APP_DB_USER=' .env | head -1 | cut -d'=' -f2-)
APP_DB_NAME=$(grep -E '^APP_DB_NAME=' .env | head -1 | cut -d'=' -f2-)
KC_DB_USER=$(grep -E '^KC_DB_USER=' .env | head -1 | cut -d'=' -f2-)

mkdir -p "$BACKUP_DIR"

echo "[$DATE] Début de la sauvegarde"

# --- 1. Base applicative (élèves, notes, paiements...) ---
docker compose exec -T postgres pg_dump -U "$APP_DB_USER" "$APP_DB_NAME" \
  | gzip > "$BACKUP_DIR/app-db-$DATE.sql.gz"
echo "  ✓ app-db-$DATE.sql.gz"

# --- 2. Base Keycloak (comptes, rôles) ---
docker compose exec -T keycloak-postgres pg_dump -U "$KC_DB_USER" keycloak \
  | gzip > "$BACKUP_DIR/keycloak-db-$DATE.sql.gz"
echo "  ✓ keycloak-db-$DATE.sql.gz"

# --- 3. Fichiers MinIO (photos, pièces jointes, PDF générés stockés) ---
docker run --rm \
  -v lbs_minio_data:/data:ro \
  -v "$BACKUP_DIR":/backup \
  alpine tar czf "/backup/minio-$DATE.tar.gz" -C /data .
echo "  ✓ minio-$DATE.tar.gz"

# --- 4. Purge des sauvegardes de plus de $RETENTION_JOURS jours ---
find "$BACKUP_DIR" -name "*.gz" -mtime +"$RETENTION_JOURS" -delete
echo "  ✓ purge des sauvegardes > $RETENTION_JOURS jours"

echo "[$DATE] Sauvegarde terminée avec succès"

# Restauration (au besoin) :
#   gunzip -c app-db-XXXX.sql.gz | docker compose exec -T postgres psql -U $APP_DB_USER -d $APP_DB_NAME
#   gunzip -c keycloak-db-XXXX.sql.gz | docker compose exec -T keycloak-postgres psql -U $KC_DB_USER -d keycloak
#   tar xzf minio-XXXX.tar.gz -C /chemin/temporaire   (puis recopier dans le volume minio_data)

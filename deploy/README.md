# Déploiement LBS — guide

Stack : **Angular (nginx) + Spring Boot + Keycloak + PostgreSQL ×2 + MinIO + RabbitMQ**, derrière **Traefik** (HTTPS automatique Let's Encrypt).

CI/CD : **GitHub Actions**, déclenché **manuellement**. Chaque repo construit son image Docker, la pousse sur **GHCR**, puis se connecte en SSH au serveur pour `docker compose pull && up -d`.

---

## 1. Prérequis serveur (une seule fois)

VPS Ubuntu 22.04/24.04, 8 Go RAM conseillé. En root :

```bash
# Docker + compose
curl -fsSL https://get.docker.com | sh

# Utilisateur de déploiement
adduser --disabled-password --gecos "" deploy
usermod -aG docker deploy

# Dossier de la stack
mkdir -p /opt/lbs/keycloak/import
chown -R deploy:deploy /opt/lbs
```

Clé SSH pour GitHub Actions (sur ta machine) :

```bash
ssh-keygen -t ed25519 -f lbs_deploy -C "github-actions"
ssh-copy-id -i lbs_deploy.pub deploy@IP_DU_SERVEUR
# -> contenu de lbs_deploy  = secret DEPLOY_SSH_KEY
```

## 2. DNS

Deux enregistrements **A** vers l'IP du serveur :

| Nom | Type | Valeur |
|-----|------|--------|
| `ecole.mondomaine.com` | A | IP serveur |
| `auth.mondomaine.com`  | A | IP serveur |

## 3. Fichiers sur le serveur (`/opt/lbs/`)

```bash
scp deploy/docker-compose.yml deploy@IP:/opt/lbs/docker-compose.yml
scp deploy/.env.example       deploy@IP:/opt/lbs/.env
ssh deploy@IP 'nano /opt/lbs/.env'   # renseigner TOUTES les valeurs CHANGE_ME
```

(Optionnel) importer le realm Keycloak existant :

```bash
# Depuis le Keycloak actuel (dev) :
docker exec lbs-keycloak /opt/keycloak/bin/kc.sh export \
  --dir /tmp/export --realm lbs-realm --users realm_file
docker cp lbs-keycloak:/tmp/export/lbs-realm-realm.json ./lbs-realm.json
# Éditer lbs-realm.json : sslRequired="external", ajouter les URLs de prod aux
# redirectUris / webOrigins du client lbs-client (https://ecole.mondomaine.com/*).
scp lbs-realm.json deploy@IP:/opt/lbs/keycloak/import/
```

Sans import : Keycloak démarre vide, on configure le realm à la main dans la console
`https://auth.mondomaine.com` (compte `KEYCLOAK_ADMIN_USER`).

## 4. Secrets GitHub (chaque repo → Settings ▸ Secrets ▸ Actions)

| Secret | Valeur |
|--------|--------|
| `DEPLOY_HOST` | IP ou domaine du serveur |
| `DEPLOY_USER` | `deploy` |
| `DEPLOY_SSH_KEY` | contenu de la clé privée `lbs_deploy` |
| `DEPLOY_PATH` | `/opt/lbs` |

`GITHUB_TOKEN` est fourni automatiquement (login GHCR côté serveur).
Rendre les 2 packages GHCR **privés** (par défaut) — le token du workflow suffit à `pull`.

## 5. Premier démarrage

```bash
ssh deploy@IP
cd /opt/lbs
docker login ghcr.io -u TON_USER_GITHUB        # une fois, pour le 1er pull manuel
docker compose up -d
docker compose logs -f backend
```

Vérifs : `https://ecole.mondomaine.com` (login super-admin), `https://auth.mondomaine.com`.

Après le 1er boot réussi : passer `DDL_AUTO=validate` dans `.env` puis `docker compose up -d backend`.

## 6. Déploiements suivants

GitHub ▸ onglet **Actions** ▸ *Deploy (frontend)* ou *Deploy (backend)* ▸ **Run workflow**.

---

## État / reste à faire

- [x] Dockerfiles front + back
- [x] `application-prod.properties`, CORS configurable, sonde `/actuator/health`
- [x] Workflows `deploy.yml` (build → GHCR → SSH)
- [x] `docker-compose.yml` prod + Traefik + `.env.example`
- [ ] Export du realm Keycloak de prod (à faire depuis l'instance en cours)
- [ ] Sauvegardes automatiques (dump Postgres + MinIO) — cron à ajouter
- [ ] Tester la chaîne complète une fois le VPS acheté

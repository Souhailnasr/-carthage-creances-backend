-- ============================================
-- Migration Base de Données - Interface Superadmin (CORRIGÉE)
-- ============================================
-- Date: 2024-12-03
-- Description: Migration sécurisée avec nettoyage des données existantes
-- ============================================

-- ============================================
-- ÉTAPE 1 : Nettoyage des données existantes dans audit_logs
-- ============================================

-- 1.1. Corriger les valeurs datetime invalides (0000-00-00 00:00:00)
UPDATE audit_logs 
SET timestamp = CURRENT_TIMESTAMP 
WHERE timestamp IS NULL OR timestamp = '0000-00-00 00:00:00' OR timestamp < '1970-01-01 00:00:00';

-- 1.2. Supprimer les enregistrements avec utilisateur_id invalide (qui n'existe pas dans utilisateur)
DELETE FROM audit_logs 
WHERE user_id IS NOT NULL 
  AND user_id NOT IN (SELECT id FROM utilisateur);

-- 1.3. Mettre à NULL les utilisateur_id invalides au lieu de supprimer (si vous préférez garder les logs)
-- UPDATE audit_logs 
-- SET user_id = NULL 
-- WHERE user_id IS NOT NULL 
--   AND user_id NOT IN (SELECT id FROM utilisateur);

-- ============================================
-- ÉTAPE 2 : Ajout des nouvelles colonnes (NULLABLE d'abord)
-- ============================================

-- Colonne date_heure (NULLABLE pour permettre la migration progressive)
ALTER TABLE audit_logs 
ADD COLUMN IF NOT EXISTS date_heure DATETIME NULL;

-- Colonne utilisateur_id (NULLABLE pour compatibilité)
ALTER TABLE audit_logs 
ADD COLUMN IF NOT EXISTS utilisateur_id BIGINT NULL;

-- Colonne type_action
ALTER TABLE audit_logs 
ADD COLUMN IF NOT EXISTS type_action VARCHAR(50) NULL;

-- Colonne entite
ALTER TABLE audit_logs 
ADD COLUMN IF NOT EXISTS entite VARCHAR(50) NULL;

-- Colonne entite_id
ALTER TABLE audit_logs 
ADD COLUMN IF NOT EXISTS entite_id BIGINT NULL;

-- Colonne details
ALTER TABLE audit_logs 
ADD COLUMN IF NOT EXISTS details TEXT NULL;

-- Colonne avant (JSON)
ALTER TABLE audit_logs 
ADD COLUMN IF NOT EXISTS avant JSON NULL;

-- Colonne apres (JSON)
ALTER TABLE audit_logs 
ADD COLUMN IF NOT EXISTS apres JSON NULL;

-- Colonne ip
ALTER TABLE audit_logs 
ADD COLUMN IF NOT EXISTS ip VARCHAR(45) NULL;

-- ============================================
-- ÉTAPE 3 : Migration des données existantes vers les nouvelles colonnes
-- ============================================

-- Migrer timestamp vers date_heure
UPDATE audit_logs 
SET date_heure = FROM_UNIXTIME(UNIX_TIMESTAMP(timestamp))
WHERE date_heure IS NULL AND timestamp IS NOT NULL AND timestamp != '0000-00-00 00:00:00';

-- Si date_heure est toujours NULL, utiliser la date actuelle
UPDATE audit_logs 
SET date_heure = CURRENT_TIMESTAMP
WHERE date_heure IS NULL;

-- Migrer user_id vers utilisateur_id
UPDATE audit_logs 
SET utilisateur_id = user_id
WHERE utilisateur_id IS NULL AND user_id IS NOT NULL;

-- Migrer dossier_id vers entite_id et définir entite
UPDATE audit_logs 
SET entite_id = dossier_id,
    entite = 'DOSSIER'
WHERE entite_id IS NULL AND dossier_id IS NOT NULL;

-- Migrer change_type vers type_action
UPDATE audit_logs 
SET type_action = change_type
WHERE type_action IS NULL AND change_type IS NOT NULL;

-- Migrer before_value vers avant
UPDATE audit_logs 
SET avant = CAST(before_value AS JSON)
WHERE avant IS NULL AND before_value IS NOT NULL AND before_value != '';

-- Migrer after_value vers apres
UPDATE audit_logs 
SET apres = CAST(after_value AS JSON)
WHERE apres IS NULL AND after_value IS NOT NULL AND after_value != '';

-- Migrer description vers details
UPDATE audit_logs 
SET details = description
WHERE details IS NULL AND description IS NOT NULL;

-- ============================================
-- ÉTAPE 4 : Rendre les colonnes NOT NULL (après migration)
-- ============================================

-- Rendre date_heure NOT NULL (maintenant que toutes les valeurs sont remplies)
ALTER TABLE audit_logs 
MODIFY COLUMN date_heure DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Rendre utilisateur_id NOT NULL (seulement si vous êtes sûr qu'il n'y a plus de NULL)
-- ALTER TABLE audit_logs 
-- MODIFY COLUMN utilisateur_id BIGINT NOT NULL;

-- Rendre type_action NOT NULL (seulement si vous êtes sûr qu'il n'y a plus de NULL)
-- ALTER TABLE audit_logs 
-- MODIFY COLUMN type_action VARCHAR(50) NOT NULL;

-- Rendre entite NOT NULL (seulement si vous êtes sûr qu'il n'y a plus de NULL)
-- ALTER TABLE audit_logs 
-- MODIFY COLUMN entite VARCHAR(50) NOT NULL;

-- ============================================
-- ÉTAPE 5 : Création des index
-- ============================================

CREATE INDEX IF NOT EXISTS idx_audit_date ON audit_logs(date_heure);
CREATE INDEX IF NOT EXISTS idx_audit_utilisateur ON audit_logs(utilisateur_id);
CREATE INDEX IF NOT EXISTS idx_audit_entite ON audit_logs(entite, entite_id);

-- ============================================
-- ÉTAPE 6 : Contrainte de clé étrangère (optionnelle, après nettoyage)
-- ============================================

-- Ajouter la contrainte de clé étrangère seulement si toutes les données sont valides
-- ALTER TABLE audit_logs 
-- ADD CONSTRAINT FK_audit_logs_utilisateur 
-- FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id);

-- ============================================
-- ÉTAPE 7 : Création des nouvelles tables
-- ============================================

-- Table commentaires_internes
CREATE TABLE IF NOT EXISTS commentaires_internes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  dossier_id BIGINT NOT NULL,
  auteur_id BIGINT NOT NULL,
  commentaire TEXT NOT NULL,
  visible_par_chef BOOLEAN DEFAULT true,
  date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (dossier_id) REFERENCES dossier(id) ON DELETE CASCADE,
  FOREIGN KEY (auteur_id) REFERENCES utilisateur(id),
  INDEX idx_commentaire_dossier (dossier_id),
  INDEX idx_commentaire_auteur (auteur_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table parametres_systeme
CREATE TABLE IF NOT EXISTS parametres_systeme (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  categorie VARCHAR(50) NOT NULL,
  cle VARCHAR(100) NOT NULL,
  valeur TEXT,
  type VARCHAR(20) NOT NULL DEFAULT 'STRING',
  description TEXT,
  date_modification DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  modifie_par BIGINT,
  UNIQUE KEY uk_categorie_cle (categorie, cle),
  FOREIGN KEY (modifie_par) REFERENCES utilisateur(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- ÉTAPE 8 : Index recommandés sur autres tables
-- ============================================

CREATE INDEX IF NOT EXISTS idx_dossier_statut ON dossier(dossier_status);
CREATE INDEX IF NOT EXISTS idx_dossier_departement ON dossier(type_recouvrement);
CREATE INDEX IF NOT EXISTS idx_dossier_date_creation ON dossier(date_creation);
CREATE INDEX IF NOT EXISTS idx_dossier_date_cloture ON dossier(date_cloture);
CREATE INDEX IF NOT EXISTS idx_dossier_agent ON dossier(agent_responsable_id);

CREATE INDEX IF NOT EXISTS idx_utilisateur_role ON utilisateur(role_Utilisateur);
CREATE INDEX IF NOT EXISTS idx_utilisateur_actif ON utilisateur(actif);
CREATE INDEX IF NOT EXISTS idx_utilisateur_derniere_connexion ON utilisateur(derniere_connexion);

-- ============================================
-- ÉTAPE 9 : Données initiales parametres_systeme (optionnel)
-- ============================================
INSERT IGNORE INTO parametres_systeme (categorie, cle, valeur, type, description) VALUES
('GENERAUX', 'nom_application', 'Carthage Créance', 'STRING', 'Nom de l''application'),
('GENERAUX', 'langue_par_defaut', 'fr', 'STRING', 'Langue par défaut'),
('ARCHIVAGE', 'delai_archivage_jours', '365', 'NUMBER', 'Délai avant archivage automatique (en jours)'),
('ALERTES', 'seuil_score_ia_faible', '30', 'NUMBER', 'Seuil de score IA pour alerte'),
('ALERTES', 'seuil_retard_jours', '90', 'NUMBER', 'Seuil de retard en jours pour alerte'),
('SECURITE', 'duree_session_minutes', '30', 'NUMBER', 'Durée de session en minutes'),
('SECURITE', 'tentatives_connexion_max', '5', 'NUMBER', 'Nombre maximum de tentatives de connexion');

-- ============================================
-- FIN DE LA MIGRATION
-- ============================================

-- Vérification : Compter les enregistrements avec date_heure NULL (devrait être 0)
SELECT COUNT(*) as nb_logs_sans_date FROM audit_logs WHERE date_heure IS NULL;

-- Vérification : Compter les enregistrements avec utilisateur_id invalide (devrait être 0)
SELECT COUNT(*) as nb_logs_utilisateur_invalide 
FROM audit_logs 
WHERE utilisateur_id IS NOT NULL 
  AND utilisateur_id NOT IN (SELECT id FROM utilisateur);


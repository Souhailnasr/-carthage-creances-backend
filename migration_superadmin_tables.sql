-- ============================================
-- Migration Base de Données - Interface Superadmin
-- ============================================
-- Date: 2024-12-03
-- Description: Création des tables et améliorations pour l'interface Superadmin
-- ============================================

-- ============================================
-- 1. Table commentaires_internes
-- ============================================
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

-- ============================================
-- 2. Table parametres_systeme
-- ============================================
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
-- 3. Amélioration table audit_logs
-- ============================================
-- Ajouter les nouvelles colonnes si elles n'existent pas déjà

-- Colonne date_heure
SET @dbname = DATABASE();
SET @tablename = 'audit_logs';
SET @columnname = 'date_heure';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' DATETIME DEFAULT CURRENT_TIMESTAMP')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Colonne utilisateur_id
SET @columnname = 'utilisateur_id';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' BIGINT')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Colonne type_action
SET @columnname = 'type_action';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(50)')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Colonne entite
SET @columnname = 'entite';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(50)')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Colonne entite_id
SET @columnname = 'entite_id';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' BIGINT')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Colonne details
SET @columnname = 'details';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' TEXT')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Colonne avant
SET @columnname = 'avant';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' JSON')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Colonne apres
SET @columnname = 'apres';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' JSON')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Colonne ip
SET @columnname = 'ip';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(45)')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- ============================================
-- 4. Index sur audit_logs
-- ============================================
CREATE INDEX IF NOT EXISTS idx_audit_date ON audit_logs(date_heure);
CREATE INDEX IF NOT EXISTS idx_audit_utilisateur ON audit_logs(utilisateur_id);
CREATE INDEX IF NOT EXISTS idx_audit_entite ON audit_logs(entite, entite_id);

-- ============================================
-- 5. Index recommandés sur dossier
-- ============================================
CREATE INDEX IF NOT EXISTS idx_dossier_statut ON dossier(dossier_status);
CREATE INDEX IF NOT EXISTS idx_dossier_departement ON dossier(type_recouvrement);
CREATE INDEX IF NOT EXISTS idx_dossier_date_creation ON dossier(date_creation);
CREATE INDEX IF NOT EXISTS idx_dossier_date_cloture ON dossier(date_cloture);
CREATE INDEX IF NOT EXISTS idx_dossier_agent ON dossier(agent_responsable_id);

-- ============================================
-- 6. Index recommandés sur utilisateur
-- ============================================
CREATE INDEX IF NOT EXISTS idx_utilisateur_role ON utilisateur(role_Utilisateur);
CREATE INDEX IF NOT EXISTS idx_utilisateur_actif ON utilisateur(actif);
CREATE INDEX IF NOT EXISTS idx_utilisateur_derniere_connexion ON utilisateur(derniere_connexion);

-- ============================================
-- 7. Migration des données existantes audit_logs
-- ============================================
-- Migrer les données des anciens champs vers les nouveaux
UPDATE audit_logs 
SET 
  date_heure = COALESCE(date_heure, FROM_UNIXTIME(UNIX_TIMESTAMP(timestamp))),
  utilisateur_id = COALESCE(utilisateur_id, user_id),
  entite_id = COALESCE(entite_id, dossier_id),
  entite = COALESCE(entite, 'DOSSIER'),
  type_action = COALESCE(type_action, change_type),
  avant = COALESCE(avant, before_value),
  apres = COALESCE(apres, after_value),
  details = COALESCE(details, description)
WHERE 
  (date_heure IS NULL AND timestamp IS NOT NULL) OR
  (utilisateur_id IS NULL AND user_id IS NOT NULL) OR
  (entite_id IS NULL AND dossier_id IS NOT NULL);

-- ============================================
-- 8. Données initiales parametres_systeme (optionnel)
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


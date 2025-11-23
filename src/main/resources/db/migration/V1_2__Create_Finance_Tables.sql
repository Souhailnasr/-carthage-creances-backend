-- Migration pour créer les tables de gestion financière complète
-- Version: 1.2
-- Date: 2024

-- ============================================
-- 1. Table des tarifs catalogue
-- ============================================
CREATE TABLE IF NOT EXISTS tarifs_catalogue (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phase VARCHAR(50) NOT NULL,
    categorie VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    fournisseur VARCHAR(200),
    tarif_unitaire DECIMAL(15, 2) NOT NULL,
    devise VARCHAR(10) DEFAULT 'TND',
    date_debut DATE NOT NULL DEFAULT CURRENT_DATE,
    date_fin DATE,
    actif BOOLEAN DEFAULT TRUE,
    INDEX idx_phase (phase),
    INDEX idx_categorie (categorie),
    INDEX idx_actif (actif),
    INDEX idx_dates (date_debut, date_fin)
);

-- ============================================
-- 2. Table des flux de frais
-- ============================================
CREATE TABLE IF NOT EXISTS flux_frais (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phase VARCHAR(50) NOT NULL,
    categorie VARCHAR(100) NOT NULL,
    quantite INT DEFAULT 1,
    tarif_unitaire DECIMAL(15, 2),
    montant DECIMAL(15, 2),
    statut VARCHAR(50) DEFAULT 'EN_ATTENTE',
    date_action DATE DEFAULT CURRENT_DATE,
    justificatif_url VARCHAR(500),
    commentaire VARCHAR(1000),
    
    -- Relations
    dossier_id BIGINT NOT NULL,
    action_id BIGINT,
    enquete_id BIGINT,
    audience_id BIGINT,
    avocat_id BIGINT,
    huissier_id BIGINT,
    facture_id BIGINT,
    
    FOREIGN KEY (dossier_id) REFERENCES dossiers(id) ON DELETE CASCADE,
    FOREIGN KEY (action_id) REFERENCES actions(id) ON DELETE SET NULL,
    FOREIGN KEY (enquete_id) REFERENCES enquetes(id) ON DELETE SET NULL,
    FOREIGN KEY (audience_id) REFERENCES audiences(id) ON DELETE SET NULL,
    FOREIGN KEY (avocat_id) REFERENCES avocats(id) ON DELETE SET NULL,
    FOREIGN KEY (huissier_id) REFERENCES huissiers(id) ON DELETE SET NULL,
    FOREIGN KEY (facture_id) REFERENCES factures(id) ON DELETE SET NULL,
    
    INDEX idx_dossier (dossier_id),
    INDEX idx_statut (statut),
    INDEX idx_phase (phase),
    INDEX idx_date_action (date_action),
    INDEX idx_facture (facture_id)
);

-- ============================================
-- 3. Table des factures
-- ============================================
CREATE TABLE IF NOT EXISTS factures (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_facture VARCHAR(50) UNIQUE NOT NULL,
    dossier_id BIGINT NOT NULL,
    periode_debut DATE,
    periode_fin DATE,
    date_emission DATE DEFAULT CURRENT_DATE,
    date_echeance DATE,
    montant_ht DECIMAL(15, 2) DEFAULT 0.0,
    montant_ttc DECIMAL(15, 2) DEFAULT 0.0,
    tva DECIMAL(5, 2) DEFAULT 19.0,
    statut VARCHAR(50) DEFAULT 'BROUILLON',
    pdf_url VARCHAR(500),
    envoyee BOOLEAN DEFAULT FALSE,
    relance_envoyee BOOLEAN DEFAULT FALSE,
    
    FOREIGN KEY (dossier_id) REFERENCES dossiers(id) ON DELETE CASCADE,
    
    INDEX idx_dossier (dossier_id),
    INDEX idx_statut (statut),
    INDEX idx_numero (numero_facture),
    INDEX idx_date_echeance (date_echeance)
);

-- ============================================
-- 4. Table des paiements
-- ============================================
CREATE TABLE IF NOT EXISTS paiements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    facture_id BIGINT NOT NULL,
    date_paiement DATE DEFAULT CURRENT_DATE,
    montant DECIMAL(15, 2),
    mode_paiement VARCHAR(50),
    reference VARCHAR(200),
    statut VARCHAR(50) DEFAULT 'EN_ATTENTE',
    commentaire VARCHAR(1000),
    
    FOREIGN KEY (facture_id) REFERENCES factures(id) ON DELETE CASCADE,
    
    INDEX idx_facture (facture_id),
    INDEX idx_statut (statut),
    INDEX idx_date_paiement (date_paiement),
    INDEX idx_reference (reference)
);

-- ============================================
-- 5. Insertion des tarifs par défaut (optionnel)
-- ============================================
INSERT INTO tarifs_catalogue (phase, categorie, description, tarif_unitaire, devise, actif) VALUES
('CREATION', 'CREATION_DOSSIER', 'Frais de création de dossier', 50.00, 'TND', TRUE),
('CREATION', 'GESTION_MENSUELLE', 'Frais de gestion mensuelle', 10.00, 'TND', TRUE),
('AMIABLE', 'APPEL', 'Coût d''un appel téléphonique', 5.00, 'TND', TRUE),
('AMIABLE', 'EMAIL', 'Coût d''un email', 2.00, 'TND', TRUE),
('AMIABLE', 'VISITE', 'Coût d''une visite', 20.00, 'TND', TRUE),
('AMIABLE', 'LETTRE', 'Coût d''une lettre recommandée', 15.00, 'TND', TRUE),
('ENQUETE', 'ENQUETE', 'Frais d''enquête (par heure)', 50.00, 'TND', TRUE),
('JURIDIQUE', 'AUDIENCE', 'Frais d''audience', 100.00, 'TND', TRUE),
('JURIDIQUE', 'AVOCAT', 'Honoraires avocat (par heure)', 150.00, 'TND', TRUE),
('JURIDIQUE', 'HUISSIER', 'Frais huissier', 200.00, 'TND', TRUE)
ON DUPLICATE KEY UPDATE tarif_unitaire = VALUES(tarif_unitaire);


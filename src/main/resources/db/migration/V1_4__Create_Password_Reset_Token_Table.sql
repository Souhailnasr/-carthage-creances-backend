-- Migration pour créer la table password_reset_token
-- Date: 2025-01-05

CREATE TABLE IF NOT EXISTS password_reset_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    utilisateur_id BIGINT NOT NULL,
    date_creation DATETIME NOT NULL,
    date_expiration DATETIME NOT NULL,
    statut ENUM('ACTIF', 'UTILISE', 'EXPIRE') NOT NULL DEFAULT 'ACTIF',
    date_utilisation DATETIME NULL,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE,
    INDEX idx_token (token),
    INDEX idx_utilisateur (utilisateur_id),
    INDEX idx_expiration (date_expiration)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


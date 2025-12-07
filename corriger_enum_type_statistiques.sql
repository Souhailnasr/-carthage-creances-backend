-- Script SQL pour corriger l'ENUM de la colonne 'type' dans la table statistiques
-- Problème: L'ENUM dans la base ne contient pas toutes les valeurs de l'enum Java TypeStatistique
-- Solution: Modifier l'ENUM pour inclure toutes les valeurs nécessaires

-- Étape 1: Vérifier l'ENUM actuel
-- SHOW COLUMNS FROM statistiques WHERE Field = 'type';

-- Étape 2: Modifier l'ENUM pour inclure toutes les valeurs de TypeStatistique
ALTER TABLE statistiques 
MODIFY COLUMN type ENUM(
    'TOTAL_DOSSIERS',
    'DOSSIERS_EN_COURS',
    'DOSSIERS_VALIDES',
    'DOSSIERS_REJETES',
    'DOSSIERS_CLOTURES',
    'DOSSIERS_CREES_CE_MOIS',
    'DOSSIERS_PAR_PHASE_CREATION',
    'DOSSIERS_PAR_PHASE_ENQUETE',
    'DOSSIERS_PAR_PHASE_AMIABLE',
    'DOSSIERS_PAR_PHASE_JURIDIQUE',
    'PERFORMANCE_AGENTS',
    'PERFORMANCE_CHEFS',
    'TOTAL_ENQUETES',
    'ENQUETES_COMPLETEES',
    'ACTIONS_AMIABLES',
    'ACTIONS_AMIABLES_COMPLETEES',
    'DOCUMENTS_HUISSIER_CREES',
    'DOCUMENTS_HUISSIER_COMPLETES',
    'ACTIONS_HUISSIER_CREES',
    'ACTIONS_HUISSIER_COMPLETES',
    'AUDIENCES_PROCHAINES',
    'AUDIENCES_TOTALES',
    'TACHES_COMPLETEES',
    'TACHES_EN_COURS',
    'TACHES_EN_RETARD',
    'TAUX_REUSSITE_GLOBAL',
    'MONTANT_RECOUVRE',
    'MONTANT_EN_COURS'
) NOT NULL;

-- Étape 3: Vérifier la modification
-- SHOW COLUMNS FROM statistiques WHERE Field = 'type';

-- Note: Après cette modification, vous pouvez appeler l'API:
-- POST http://localhost:8089/carthage-creance/api/statistiques/recalculer
-- Headers: Authorization: Bearer {token}

-- Vérification: Voir les statistiques stockées
-- SELECT type, valeur, description, periode, date_calcul
-- FROM statistiques
-- WHERE periode = DATE_FORMAT(NOW(), '%Y-%m')
-- ORDER BY type, date_calcul DESC;


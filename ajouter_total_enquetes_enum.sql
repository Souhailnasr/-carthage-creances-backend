-- Script SQL pour ajouter TOTAL_ENQUETES à l'ENUM de la colonne 'type'
-- Problème: L'ENUM dans la base ne contient pas TOTAL_ENQUETES qui a été ajouté récemment à l'enum Java
-- Solution: Modifier l'ENUM pour inclure TOTAL_ENQUETES

-- Étape 1: Vérifier l'ENUM actuel
-- SHOW COLUMNS FROM statistiques WHERE Field = 'type';

-- Étape 2: Ajouter TOTAL_ENQUETES à l'ENUM
-- Note: MySQL ne permet pas d'ajouter une valeur à un ENUM existant directement
-- Il faut recréer l'ENUM avec toutes les valeurs + la nouvelle

ALTER TABLE statistiques 
MODIFY COLUMN type ENUM(
    'ACTIONS_AMIABLES',
    'ACTIONS_AMIABLES_COMPLETEES',
    'ACTIONS_HUISSIER_COMPLETES',
    'ACTIONS_HUISSIER_CREES',
    'AUDIENCES_PROCHAINES',
    'AUDIENCES_TOTALES',
    'DOCUMENTS_HUISSIER_COMPLETES',
    'DOCUMENTS_HUISSIER_CREES',
    'DOSSIERS_CLOTURES',
    'DOSSIERS_CREES_CE_MOIS',
    'DOSSIERS_EN_COURS',
    'DOSSIERS_PAR_PHASE_AMIABLE',
    'DOSSIERS_PAR_PHASE_CREATION',
    'DOSSIERS_PAR_PHASE_ENQUETE',
    'DOSSIERS_PAR_PHASE_JURIDIQUE',
    'DOSSIERS_REJETES',
    'DOSSIERS_VALIDES',
    'ENQUETES_COMPLETEES',
    'TOTAL_ENQUETES',  -- ✅ NOUVELLE VALEUR AJOUTÉE
    'MONTANT_EN_COURS',
    'MONTANT_RECOUVRE',
    'PERFORMANCE_AGENTS',
    'PERFORMANCE_CHEFS',
    'TACHES_COMPLETEES',
    'TACHES_EN_COURS',
    'TACHES_EN_RETARD',
    'TAUX_REUSSITE_GLOBAL',
    'TOTAL_DOSSIERS'
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


-- Script pour corriger les données orphelines dans audit_logs
-- et permettre la création de la contrainte de clé étrangère

-- Étape 1: Désactiver temporairement les vérifications de clés étrangères
SET FOREIGN_KEY_CHECKS = 0;

-- Étape 2: Identifier les enregistrements orphelins
-- (ceux avec utilisateur_id qui n'existe pas dans utilisateur)
SELECT 'Enregistrements orphelins dans audit_logs:' AS 'Info';
SELECT al.id, al.utilisateur_id, al.date_heure, al.type_action
FROM audit_logs al
LEFT JOIN utilisateur u ON al.utilisateur_id = u.id
WHERE al.utilisateur_id IS NOT NULL AND u.id IS NULL;

-- Étape 3: Option A - Supprimer les enregistrements orphelins
-- (Décommentez si vous voulez supprimer les données orphelines)
-- DELETE al FROM audit_logs al
-- LEFT JOIN utilisateur u ON al.utilisateur_id = u.id
-- WHERE al.utilisateur_id IS NOT NULL AND u.id IS NULL;

-- Étape 3: Option B - Mettre à jour les utilisateur_id orphelins avec un utilisateur valide
-- Trouver un utilisateur valide (par exemple, le premier Superadmin)
SET @valid_user_id = (SELECT id FROM utilisateur WHERE role_utilisateur LIKE '%SUPER_ADMIN%' LIMIT 1);

-- Si aucun Superadmin trouvé, utiliser le premier utilisateur
SET @valid_user_id = COALESCE(@valid_user_id, (SELECT id FROM utilisateur LIMIT 1));

-- Si toujours null, utiliser 1 par défaut (assurez-vous qu'un utilisateur avec ID 1 existe)
SET @valid_user_id = COALESCE(@valid_user_id, 1);

-- Mettre à jour les enregistrements orphelins
UPDATE audit_logs al
LEFT JOIN utilisateur u ON al.utilisateur_id = u.id
SET al.utilisateur_id = @valid_user_id
WHERE al.utilisateur_id IS NOT NULL AND u.id IS NULL;

-- Étape 4: Vérifier qu'il n'y a plus d'enregistrements orphelins
SELECT 'Vérification après correction:' AS 'Info';
SELECT COUNT(*) AS 'Enregistrements orphelins restants'
FROM audit_logs al
LEFT JOIN utilisateur u ON al.utilisateur_id = u.id
WHERE al.utilisateur_id IS NOT NULL AND u.id IS NULL;

-- Étape 5: Réactiver les vérifications de clés étrangères
SET FOREIGN_KEY_CHECKS = 1;

-- Étape 6: Supprimer la contrainte existante si elle existe (pour la recréer proprement)
-- ALTER TABLE audit_logs DROP FOREIGN KEY IF EXISTS FK3ys01furke47wvt1itscj3ns0;

-- Note: Hibernate recréera automatiquement la contrainte au prochain démarrage
-- si l'entité AuditLog existe toujours dans le code


package projet.carthagecreance_backend.Entity;

public enum TypeNotificationHuissier {
    DELAY_WARNING,              // Avertissement avant expiration du délai
    DELAY_EXPIRED,             // Délai légal expiré
    ACTION_PERFORMED,          // Action huissier effectuée
    AMIABLE_RESPONSE_POSITIVE, // Réponse positive du débiteur (amiable)
    AMIABLE_RESPONSE_NEGATIVE, // Réponse négative du débiteur (amiable)
    AMOUNT_UPDATED,            // Montant mis à jour
    DOCUMENT_CREATED,          // Document créé
    STATUS_CHANGED             // Statut du dossier changé
}


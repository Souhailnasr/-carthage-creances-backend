package projet.carthagecreance_backend.Entity;

public enum StatutDocumentHuissier {
    PENDING,    // En attente (délai légal non expiré)
    EXPIRED,    // Délai légal expiré
    COMPLETED   // Complété (action suivante effectuée)
}


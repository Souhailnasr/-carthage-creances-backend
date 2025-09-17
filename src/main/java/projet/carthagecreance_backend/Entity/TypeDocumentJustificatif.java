package projet.carthagecreance_backend.Entity;

public enum TypeDocumentJustificatif {
    // Documents contractuels
    CONTRAT,
    BON_DE_COMMANDE,
    BON_DE_LIVRAISON,
    DEVIS_ACCEPTE,
    // Documents financiers
    FACTURE,
    RELEVE_DE_COMPTE,
    ECHEANCIER,
    LETTRE_DE_CHANGE,
    BILLET_A_ORDRE,
    // Correspondances et preuves d’engagement
    COURRIER_RELANCE,
    RECONNAISSANCE_DE_DOTE,
    EMAIL_RECONNAISSANCE_DETTE,
    ACCUSE_RECEPTION_FACTURE,
    // Documents officiels
    JUGEMENT,
    ORDONNANCE_REFERÉ,
    PROCÈS_VERBAL_CONCILIATION,
    PROCÈS_VERBAL_HUISSIER
}
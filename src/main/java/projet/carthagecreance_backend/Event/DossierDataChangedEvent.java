package projet.carthagecreance_backend.Event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Événement déclenché lorsqu'une donnée d'un dossier change
 * et nécessite un recalcul de la prédiction IA
 */
@Getter
public class DossierDataChangedEvent extends ApplicationEvent {
    private final Long dossierId;
    private final String changeType; // "ACTION_AMIABLE", "ACTION_HUISSIER", "AUDIENCE", "DOCUMENT_HUISSIER"
    
    public DossierDataChangedEvent(Object source, Long dossierId, String changeType) {
        super(source);
        this.dossierId = dossierId;
        this.changeType = changeType;
    }
}


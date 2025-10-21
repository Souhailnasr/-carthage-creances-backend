package projet.carthagecreance_backend.Entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import projet.carthagecreance_backend.Entity.DossierStatus;

@Converter(autoApply = true)
public class DossierStatusConverter implements AttributeConverter<DossierStatus, String> {

    @Override
    public String convertToDatabaseColumn(DossierStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public DossierStatus convertToEntityAttribute(String dbData) {
        // Utilise notre méthode fromString pour gérer les valeurs null, vides ou invalides
        return DossierStatus.fromString(dbData);
    }
}

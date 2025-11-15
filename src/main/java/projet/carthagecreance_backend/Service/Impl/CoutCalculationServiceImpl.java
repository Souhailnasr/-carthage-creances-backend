package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projet.carthagecreance_backend.Entity.Action;
import projet.carthagecreance_backend.Entity.Dossier;
import projet.carthagecreance_backend.Entity.Finance;
import projet.carthagecreance_backend.Entity.TypeRecouvrement;
import projet.carthagecreance_backend.Repository.ActionRepository;
import projet.carthagecreance_backend.Repository.DossierRepository;
import projet.carthagecreance_backend.Repository.FinanceRepository;
import projet.carthagecreance_backend.Service.CoutCalculationService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CoutCalculationServiceImpl implements CoutCalculationService {

    @Autowired
    private ActionRepository actionRepository;
    
    @Autowired
    private DossierRepository dossierRepository;
    
    @Autowired
    private FinanceRepository financeRepository;

    @Override
    public Double calculerCoutActionsAmiable(Long dossierId) {
        List<Action> actions = actionRepository.findByDossierId(dossierId);
        
        return actions.stream()
                .filter(action -> {
                    Dossier dossier = action.getDossier();
                    return dossier != null && 
                           dossier.getTypeRecouvrement() == TypeRecouvrement.AMIABLE;
                })
                .mapToDouble(action -> {
                    if (action.getNbOccurrences() != null && action.getCoutUnitaire() != null) {
                        return action.getNbOccurrences() * action.getCoutUnitaire();
                    }
                    return 0.0;
                })
                .sum();
    }

    @Override
    public Double calculerCoutActionsJuridique(Long dossierId) {
        List<Action> actions = actionRepository.findByDossierId(dossierId);
        
        return actions.stream()
                .filter(action -> {
                    Dossier dossier = action.getDossier();
                    return dossier != null && 
                           dossier.getTypeRecouvrement() == TypeRecouvrement.JURIDIQUE;
                })
                .mapToDouble(action -> {
                    if (action.getNbOccurrences() != null && action.getCoutUnitaire() != null) {
                        return action.getNbOccurrences() * action.getCoutUnitaire();
                    }
                    return 0.0;
                })
                .sum();
    }

    @Override
    public Integer calculerDureeGestion(Long dossierId) {
        Optional<Dossier> dossierOpt = dossierRepository.findById(dossierId);
        if (dossierOpt.isEmpty()) {
            throw new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId);
        }
        
        Dossier dossier = dossierOpt.get();
        if (dossier.getDateCreation() == null) {
            return 0;
        }
        
        LocalDate dateCreation = dossier.getDateCreation().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        
        LocalDate dateFin;
        if (dossier.getDateCloture() != null) {
            dateFin = dossier.getDateCloture().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        } else {
            dateFin = LocalDate.now();
        }
        
        long mois = ChronoUnit.MONTHS.between(dateCreation, dateFin);
        // Arrondir au mois supérieur
        return (int) Math.max(1, mois + 1);
    }

    @Override
    public Double calculerCoutGestion(Long dossierId) {
        Optional<Finance> financeOpt = financeRepository.findByDossierId(dossierId);
        if (financeOpt.isEmpty()) {
            throw new RuntimeException("Finance non trouvée pour le dossier ID: " + dossierId);
        }
        
        Finance finance = financeOpt.get();
        Integer dureeMois = calculerDureeGestion(dossierId);
        Double fraisGestion = finance.getFraisGestionDossier();
        
        if (fraisGestion == null) {
            fraisGestion = 10.0; // Valeur par défaut
        }
        
        return fraisGestion * dureeMois;
    }

    @Override
    public Finance recalculerTousLesCouts(Long dossierId) {
        Optional<Finance> financeOpt = financeRepository.findByDossierId(dossierId);
        if (financeOpt.isEmpty()) {
            throw new RuntimeException("Finance non trouvée pour le dossier ID: " + dossierId);
        }
        
        Finance finance = financeOpt.get();
        
        // Recalculer les coûts des actions
        synchroniserActionsAvecFinance(dossierId);
        
        // Recalculer la durée de gestion
        Integer dureeMois = calculerDureeGestion(dossierId);
        finance.setDureeGestionMois(dureeMois);
        
        // Recharger depuis la base pour avoir les valeurs mises à jour
        finance = financeRepository.save(finance);
        finance = financeRepository.findByDossierId(dossierId).orElse(finance);
        
        return finance;
    }

    @Override
    public void synchroniserActionsAvecFinance(Long dossierId) {
        Optional<Finance> financeOpt = financeRepository.findByDossierId(dossierId);
        if (financeOpt.isEmpty()) {
            throw new RuntimeException("Finance non trouvée pour le dossier ID: " + dossierId);
        }
        
        Finance finance = financeOpt.get();
        List<Action> actions = actionRepository.findByDossierId(dossierId);
        
        // Réinitialiser les compteurs
        double coutAmiable = 0.0;
        double coutJuridique = 0.0;
        int nombreAmiable = 0;
        int nombreJuridique = 0;
        
        // Recalculer depuis toutes les actions
        for (Action action : actions) {
            Dossier dossier = action.getDossier();
            if (dossier == null) continue;
            
            TypeRecouvrement typeRecouvrement = dossier.getTypeRecouvrement();
            if (typeRecouvrement == null) continue;
            
            Double coutAction = action.getTotalCout();
            if (coutAction == null) continue;
            
            if (typeRecouvrement == TypeRecouvrement.AMIABLE) {
                coutAmiable += coutAction;
                nombreAmiable++;
            } else if (typeRecouvrement == TypeRecouvrement.JURIDIQUE) {
                coutJuridique += coutAction;
                nombreJuridique++;
            }
        }
        
        // Mettre à jour la Finance
        finance.setCoutActionsAmiable(coutAmiable);
        finance.setCoutActionsJuridique(coutJuridique);
        finance.setNombreActionsAmiable(nombreAmiable);
        finance.setNombreActionsJuridique(nombreJuridique);
        
        financeRepository.save(finance);
    }
}


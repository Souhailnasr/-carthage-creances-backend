package projet.carthagecreance_backend.Service.Impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projet.carthagecreance_backend.DTO.ActionRequestDTO;
import projet.carthagecreance_backend.Entity.Action;
import projet.carthagecreance_backend.Entity.TypeAction;
import projet.carthagecreance_backend.Entity.ReponseDebiteur;
import projet.carthagecreance_backend.Entity.Finance;
import projet.carthagecreance_backend.Entity.Dossier;
import projet.carthagecreance_backend.Entity.TypeRecouvrement;
import projet.carthagecreance_backend.Repository.ActionRepository;
import projet.carthagecreance_backend.Repository.FinanceRepository;
import projet.carthagecreance_backend.Repository.DossierRepository;
import projet.carthagecreance_backend.Service.ActionService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ActionServiceImpl implements ActionService {

    private static final Logger logger = LoggerFactory.getLogger(ActionServiceImpl.class);

    @Autowired
    private ActionRepository actionRepository;
    
    @Autowired
    private FinanceRepository financeRepository;
    
    @Autowired
    private DossierRepository dossierRepository;

    /**
     * Crée une action à partir d'un DTO
     * Le coutUnitaire est envoyé par le frontend, le calcul total se fait dans Finance
     */
    @Override
    public Action createActionFromDTO(ActionRequestDTO actionDTO) {
        // Validation des champs obligatoires
        if (actionDTO.getDossierId() == null) {
            throw new IllegalArgumentException("L'ID du dossier est obligatoire");
        }
        if (actionDTO.getType() == null) {
            throw new IllegalArgumentException("Le type d'action est obligatoire");
        }
        if (actionDTO.getDateAction() == null) {
            throw new IllegalArgumentException("La date de l'action est obligatoire");
        }
        if (actionDTO.getNbOccurrences() == null || actionDTO.getNbOccurrences() < 1) {
            throw new IllegalArgumentException("Le nombre d'occurrences doit être au moins 1");
        }
        if (actionDTO.getCoutUnitaire() == null || actionDTO.getCoutUnitaire() < 0) {
            throw new IllegalArgumentException("Le coût unitaire doit être positif ou nul");
        }
        
        // Récupérer le dossier
        Dossier dossier = dossierRepository.findById(actionDTO.getDossierId())
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + actionDTO.getDossierId()));
        
        // Récupérer ou créer la Finance pour ce dossier
        Finance finance = financeRepository.findByDossierId(dossier.getId())
                .orElseGet(() -> {
                    logger.info("Création automatique de Finance pour le dossier ID: {}", dossier.getId());
                    Finance newFinance = Finance.builder()
                            .dossier(dossier)
                            .devise("TND")
                            .dateOperation(LocalDate.now())
                            .description("Finance pour dossier " + dossier.getNumeroDossier())
                            .fraisCreationDossier(50.0)
                            .fraisGestionDossier(10.0)
                            .dureeGestionMois(0)
                            .coutActionsAmiable(0.0)
                            .coutActionsJuridique(0.0)
                            .nombreActionsAmiable(0)
                            .nombreActionsJuridique(0)
                            .factureFinalisee(false)
                            .build();
                    return financeRepository.save(newFinance);
                });
        
        // Créer l'action à partir du DTO
        Action action = Action.builder()
                .type(actionDTO.getType())
                .dateAction(actionDTO.getDateAction())
                .nbOccurrences(actionDTO.getNbOccurrences())
                .coutUnitaire(actionDTO.getCoutUnitaire()) // Reçu du frontend
                .reponseDebiteur(actionDTO.getReponseDebiteur()) // Peut être null
                .dossier(dossier)
                .finance(finance)
                .build();
        
        // Calculer le coût total de l'action (nbOccurrences * coutUnitaire)
        Double coutTotal = action.getTotalCout();
        logger.debug("Coût total de l'action: {} ({} occurrences × {})", 
                coutTotal, actionDTO.getNbOccurrences(), actionDTO.getCoutUnitaire());
        
        // Mettre à jour les coûts dans Finance selon le type de recouvrement
        TypeRecouvrement typeRecouvrement = dossier.getTypeRecouvrement();
        if (typeRecouvrement == null) {
            typeRecouvrement = TypeRecouvrement.NON_AFFECTE;
            logger.warn("Le dossier ID {} n'a pas de type de recouvrement défini, utilisation de NON_AFFECTE", dossier.getId());
        }
        
        if (typeRecouvrement == TypeRecouvrement.AMIABLE) {
            finance.setCoutActionsAmiable(
                (finance.getCoutActionsAmiable() != null ? finance.getCoutActionsAmiable() : 0.0) + coutTotal
            );
            finance.setNombreActionsAmiable(
                (finance.getNombreActionsAmiable() != null ? finance.getNombreActionsAmiable() : 0) + 1
            );
            logger.debug("Mise à jour Finance: coutActionsAmiable={}, nombreActionsAmiable={}", 
                    finance.getCoutActionsAmiable(), finance.getNombreActionsAmiable());
        } else if (typeRecouvrement == TypeRecouvrement.JURIDIQUE) {
            finance.setCoutActionsJuridique(
                (finance.getCoutActionsJuridique() != null ? finance.getCoutActionsJuridique() : 0.0) + coutTotal
            );
            finance.setNombreActionsJuridique(
                (finance.getNombreActionsJuridique() != null ? finance.getNombreActionsJuridique() : 0) + 1
            );
            logger.debug("Mise à jour Finance: coutActionsJuridique={}, nombreActionsJuridique={}", 
                    finance.getCoutActionsJuridique(), finance.getNombreActionsJuridique());
        } else {
            logger.warn("Le dossier ID {} a le type de recouvrement {}, les coûts ne seront pas mis à jour dans Finance", 
                    dossier.getId(), typeRecouvrement);
        }
        
        // Sauvegarder la Finance mise à jour
        financeRepository.save(finance);
        
        // Sauvegarder l'action
        Action savedAction = actionRepository.save(action);
        logger.info("Action créée avec succès, ID: {}, Dossier ID: {}", savedAction.getId(), dossier.getId());
        return savedAction;
    }

    /**
     * Met à jour une action à partir d'un DTO
     */
    @Override
    public Action updateActionFromDTO(Long id, ActionRequestDTO actionDTO) {
        Optional<Action> existingActionOpt = actionRepository.findById(id);
        if (existingActionOpt.isEmpty()) {
            throw new RuntimeException("Action non trouvée avec l'ID: " + id);
        }
        
        Action existingAction = existingActionOpt.get();
        Double ancienCout = existingAction.getTotalCout();
        Dossier dossier = existingAction.getDossier();
        
        // Validation
        if (actionDTO.getType() == null) {
            throw new IllegalArgumentException("Le type d'action est obligatoire");
        }
        if (actionDTO.getDateAction() == null) {
            throw new IllegalArgumentException("La date de l'action est obligatoire");
        }
        if (actionDTO.getNbOccurrences() == null || actionDTO.getNbOccurrences() < 1) {
            throw new IllegalArgumentException("Le nombre d'occurrences doit être au moins 1");
        }
        if (actionDTO.getCoutUnitaire() == null || actionDTO.getCoutUnitaire() < 0) {
            throw new IllegalArgumentException("Le coût unitaire doit être positif ou nul");
        }
        
        // Mettre à jour les champs de l'action
        existingAction.setType(actionDTO.getType());
        existingAction.setDateAction(actionDTO.getDateAction());
        existingAction.setNbOccurrences(actionDTO.getNbOccurrences());
        existingAction.setCoutUnitaire(actionDTO.getCoutUnitaire());
        existingAction.setReponseDebiteur(actionDTO.getReponseDebiteur());
        
        // Calculer le nouveau coût total
        Double nouveauCout = existingAction.getTotalCout();
        Double differenceCout = nouveauCout - ancienCout;
        
        // Mettre à jour les coûts dans Finance si nécessaire
        if (existingAction.getFinance() != null && dossier != null) {
            Finance finance = existingAction.getFinance();
            TypeRecouvrement typeRecouvrement = dossier.getTypeRecouvrement();
            
            if (typeRecouvrement == TypeRecouvrement.AMIABLE) {
                finance.setCoutActionsAmiable(
                    (finance.getCoutActionsAmiable() != null ? finance.getCoutActionsAmiable() : 0.0) + differenceCout
                );
            } else if (typeRecouvrement == TypeRecouvrement.JURIDIQUE) {
                finance.setCoutActionsJuridique(
                    (finance.getCoutActionsJuridique() != null ? finance.getCoutActionsJuridique() : 0.0) + differenceCout
                );
            }
            
            financeRepository.save(finance);
        }
        
        // Sauvegarder l'action mise à jour
        return actionRepository.save(existingAction);
    }

    @Override
    public Action createAction(Action action) {
        // Vérifier que le dossier existe
        if (action.getDossier() == null || action.getDossier().getId() == null) {
            throw new RuntimeException("L'action doit être associée à un dossier");
        }
        
        Dossier dossier = dossierRepository.findById(action.getDossier().getId())
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + action.getDossier().getId()));
        
        // Récupérer ou créer la Finance pour ce dossier
        Finance finance = financeRepository.findByDossierId(dossier.getId())
                .orElseGet(() -> {
                    // Créer une Finance si elle n'existe pas
                    Finance newFinance = Finance.builder()
                            .dossier(dossier)
                            .devise("TND")
                            .dateOperation(LocalDate.now())
                            .description("Finance pour dossier " + dossier.getNumeroDossier())
                            .fraisCreationDossier(50.0)
                            .fraisGestionDossier(10.0)
                            .dureeGestionMois(0)
                            .coutActionsAmiable(0.0)
                            .coutActionsJuridique(0.0)
                            .nombreActionsAmiable(0)
                            .nombreActionsJuridique(0)
                            .factureFinalisee(false)
                            .build();
                    return financeRepository.save(newFinance);
                });
        
        // Lier l'action à la Finance
        action.setFinance(finance);
        
        // Calculer le coût total de l'action
        Double coutTotal = action.getTotalCout();
        
        // Mettre à jour les coûts dans Finance selon le type de recouvrement
        TypeRecouvrement typeRecouvrement = dossier.getTypeRecouvrement();
        if (typeRecouvrement == null) {
            typeRecouvrement = TypeRecouvrement.NON_AFFECTE;
        }
        
        if (typeRecouvrement == TypeRecouvrement.AMIABLE) {
            finance.setCoutActionsAmiable(
                (finance.getCoutActionsAmiable() != null ? finance.getCoutActionsAmiable() : 0.0) + coutTotal
            );
            finance.setNombreActionsAmiable(
                (finance.getNombreActionsAmiable() != null ? finance.getNombreActionsAmiable() : 0) + 1
            );
        } else if (typeRecouvrement == TypeRecouvrement.JURIDIQUE) {
            finance.setCoutActionsJuridique(
                (finance.getCoutActionsJuridique() != null ? finance.getCoutActionsJuridique() : 0.0) + coutTotal
            );
            finance.setNombreActionsJuridique(
                (finance.getNombreActionsJuridique() != null ? finance.getNombreActionsJuridique() : 0) + 1
            );
        }
        
        // Sauvegarder la Finance mise à jour
        financeRepository.save(finance);
        
        // Sauvegarder l'action
        return actionRepository.save(action);
    }

    @Override
    public Optional<Action> getActionById(Long id) {
        return actionRepository.findById(id);
    }

    @Override
    public List<Action> getAllActions() {
        return actionRepository.findAll();
    }

    @Override
    public Action updateAction(Long id, Action action) {
        Optional<Action> existingActionOpt = actionRepository.findById(id);
        if (existingActionOpt.isEmpty()) {
            throw new RuntimeException("Action not found with id: " + id);
        }
        
        Action existingAction = existingActionOpt.get();
        Double ancienCout = existingAction.getTotalCout();
        
        // Mettre à jour l'action
        action.setId(id);
        Action updatedAction = actionRepository.save(action);
        
        // Recalculer les coûts dans Finance si nécessaire
        if (action.getFinance() != null && action.getDossier() != null) {
            Finance finance = action.getFinance();
            Dossier dossier = action.getDossier();
            TypeRecouvrement typeRecouvrement = dossier.getTypeRecouvrement();
            
            if (typeRecouvrement == TypeRecouvrement.AMIABLE) {
                Double nouveauCout = action.getTotalCout();
                finance.setCoutActionsAmiable(
                    (finance.getCoutActionsAmiable() != null ? finance.getCoutActionsAmiable() : 0.0) 
                    - ancienCout + nouveauCout
                );
            } else if (typeRecouvrement == TypeRecouvrement.JURIDIQUE) {
                Double nouveauCout = action.getTotalCout();
                finance.setCoutActionsJuridique(
                    (finance.getCoutActionsJuridique() != null ? finance.getCoutActionsJuridique() : 0.0) 
                    - ancienCout + nouveauCout
                );
            }
            
            financeRepository.save(finance);
        }
        
        return updatedAction;
    }

    @Override
    public void deleteAction(Long id) {
        Optional<Action> actionOpt = actionRepository.findById(id);
        if (actionOpt.isEmpty()) {
            throw new RuntimeException("Action not found with id: " + id);
        }
        
        Action action = actionOpt.get();
        Double coutTotal = action.getTotalCout();
        Finance finance = action.getFinance();
        Dossier dossier = action.getDossier();
        
        // Mettre à jour les coûts dans Finance avant suppression
        if (finance != null && dossier != null) {
            TypeRecouvrement typeRecouvrement = dossier.getTypeRecouvrement();
            
            if (typeRecouvrement == TypeRecouvrement.AMIABLE) {
                finance.setCoutActionsAmiable(
                    Math.max(0.0, (finance.getCoutActionsAmiable() != null ? finance.getCoutActionsAmiable() : 0.0) - coutTotal)
                );
                finance.setNombreActionsAmiable(
                    Math.max(0, (finance.getNombreActionsAmiable() != null ? finance.getNombreActionsAmiable() : 0) - 1)
                );
            } else if (typeRecouvrement == TypeRecouvrement.JURIDIQUE) {
                finance.setCoutActionsJuridique(
                    Math.max(0.0, (finance.getCoutActionsJuridique() != null ? finance.getCoutActionsJuridique() : 0.0) - coutTotal)
                );
                finance.setNombreActionsJuridique(
                    Math.max(0, (finance.getNombreActionsJuridique() != null ? finance.getNombreActionsJuridique() : 0) - 1)
                );
            }
            
            financeRepository.save(finance);
        }
        
        // Supprimer l'action
        actionRepository.deleteById(id);
    }

    @Override
    public List<Action> getActionsByType(TypeAction type) {
        return actionRepository.findByType(type);
    }

    @Override
    public List<Action> getActionsByDossier(Long dossierId) {
        return actionRepository.findByDossierId(dossierId);
    }

    @Override
    public List<Action> getActionsByDate(LocalDate date) {
        return actionRepository.findByDateAction(date);
    }

    @Override
    public List<Action> getActionsByDateRange(LocalDate startDate, LocalDate endDate) {
        return actionRepository.findByDateActionBetween(startDate, endDate);
    }

    @Override
    public List<Action> getActionsByTypeAndDossier(TypeAction type, Long dossierId) {
        return actionRepository.findByTypeAndDossierId(type, dossierId);
    }

    @Override
    public Double calculateTotalCostByDossier(Long dossierId) {
        return actionRepository.calculerCoutTotalParDossier(dossierId);
    }

    @Override
    public Double calculateTotalCostByType(TypeAction type) {
        return actionRepository.calculerCoutTotalParType(type);
    }

    @Override
    public List<Action> getActionsWithCostGreaterThan(Double amount) {
        return actionRepository.findByCoutSuperieurA(amount);
    }
    
    // ==================== IMPLÉMENTATION DES MÉTHODES POUR REPONSEDEBITEUR ====================
    
    @Override
    public List<Action> getActionsByReponseDebiteur(ReponseDebiteur reponseDebiteur) {
        return actionRepository.findByReponseDebiteur(reponseDebiteur);
    }
    
    @Override
    public List<Action> getActionsByTypeAndReponseDebiteur(TypeAction type, ReponseDebiteur reponseDebiteur) {
        return actionRepository.findByTypeAndReponseDebiteur(type, reponseDebiteur);
    }
    
    @Override
    public List<Action> getActionsByDossierAndReponseDebiteur(Long dossierId, ReponseDebiteur reponseDebiteur) {
        return actionRepository.findByDossierIdAndReponseDebiteur(dossierId, reponseDebiteur);
    }
    
    @Override
    public List<Action> getActionsByTypeAndDossierAndReponseDebiteur(TypeAction type, Long dossierId, ReponseDebiteur reponseDebiteur) {
        return actionRepository.findByTypeAndDossierIdAndReponseDebiteur(type, dossierId, reponseDebiteur);
    }
    
    @Override
    public List<Object[]> getActionCountByReponseDebiteur() {
        return actionRepository.compterActionsParReponseDebiteur();
    }
    
    @Override
    public List<Object[]> getActionCountByTypeAndReponseDebiteur() {
        return actionRepository.compterActionsParTypeEtReponseDebiteur();
    }
    
    @Override
    public Double calculateTotalCostByReponseDebiteur(ReponseDebiteur reponseDebiteur) {
        return actionRepository.calculerCoutTotalParReponseDebiteur(reponseDebiteur);
    }
    
    @Override
    public List<Action> getActionsWithPositiveResponse() {
        return actionRepository.findActionsAvecReponsePositive();
    }
    
    @Override
    public List<Action> getActionsWithNegativeResponse() {
        return actionRepository.findActionsAvecReponseNegative();
    }
    
    @Override
    public List<Action> getActionsWithoutResponse() {
        return actionRepository.findActionsSansReponseDebiteur();
    }
}

package projet.carthagecreance_backend.Service.Impl;

import org.springframework.stereotype.Service;
import projet.carthagecreance_backend.Entity.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Service pour construire les features à partir des données réelles
 * pour la prédiction IA
 */
@Service
public class IaFeatureBuilderService {
    
    /**
     * Construit les features à partir des données réelles du dossier
     * 
     * @param dossier Le dossier
     * @param enquete L'enquête associée (peut être null)
     * @param actions Les actions associées (peut être null ou vide)
     * @param audiences Les audiences associées (peut être null ou vide)
     * @param actionsHuissier Les actions huissier associées (peut être null ou vide)
     * @return Map contenant les features pour la prédiction
     */
    public Map<String, Object> buildFeaturesFromRealData(
            Dossier dossier,
            Enquette enquete,
            List<Action> actions,
            List<Audience> audiences,
            List<ActionHuissier> actionsHuissier) {
        
        Map<String, Object> features = new HashMap<>();
        
        // ========== Features du Dossier ==========
        features.put("montantCreance", dossier.getMontantCreance() != null ? dossier.getMontantCreance() : 0.0);
        features.put("montantRecouvre", dossier.getMontantRecouvre() != null ? dossier.getMontantRecouvre() : 0.0);
        features.put("montantRestant", dossier.getMontantRestant() != null ? dossier.getMontantRestant() : 0.0);
        
        // Calcul du pourcentage de recouvrement
        double pourcentageRecouvre = 0.0;
        if (dossier.getMontantCreance() != null && dossier.getMontantCreance() > 0) {
            double montantRecouvre = dossier.getMontantRecouvre() != null ? dossier.getMontantRecouvre() : 0.0;
            pourcentageRecouvre = (montantRecouvre / dossier.getMontantCreance()) * 100.0;
        }
        features.put("pourcentageRecouvre", pourcentageRecouvre);
        
        // ========== Features du Montant Recouvré par Phase ==========
        // Calculer le montant recouvré en phase amiable
        double montantRecouvreAmiable = 0.0;
        if (actions != null && !actions.isEmpty()) {
            // Le montant recouvré en phase amiable est dans le dossier
            // Mais on peut aussi le calculer depuis les actions avec réponse positive
            // Pour l'instant, on utilise le montant global du dossier si typeRecouvrement = AMIABLE
            if (dossier.getTypeRecouvrement() == TypeRecouvrement.AMIABLE) {
                montantRecouvreAmiable = dossier.getMontantRecouvre() != null ? dossier.getMontantRecouvre() : 0.0;
            }
        }
        features.put("montantRecouvreAmiable", montantRecouvreAmiable);
        
        // Calculer le montant recouvré en phase juridique (depuis les actions huissier)
        double montantRecouvreJuridique = 0.0;
        if (actionsHuissier != null && !actionsHuissier.isEmpty()) {
            for (ActionHuissier actionHuissier : actionsHuissier) {
                if (actionHuissier.getMontantRecouvre() != null) {
                    montantRecouvreJuridique += actionHuissier.getMontantRecouvre().doubleValue();
                }
            }
        }
        features.put("montantRecouvreJuridique", montantRecouvreJuridique);
        
        // Montant recouvré total (somme des deux phases)
        double montantRecouvreTotal = montantRecouvreAmiable + montantRecouvreJuridique;
        features.put("montantRecouvreTotal", montantRecouvreTotal);
        
        // Pourcentage de recouvrement par phase
        double pourcentageRecouvreAmiable = 0.0;
        if (dossier.getMontantCreance() != null && dossier.getMontantCreance() > 0) {
            pourcentageRecouvreAmiable = (montantRecouvreAmiable / dossier.getMontantCreance()) * 100.0;
        }
        features.put("pourcentageRecouvreAmiable", pourcentageRecouvreAmiable);
        
        double pourcentageRecouvreJuridique = 0.0;
        if (dossier.getMontantCreance() != null && dossier.getMontantCreance() > 0) {
            pourcentageRecouvreJuridique = (montantRecouvreJuridique / dossier.getMontantCreance()) * 100.0;
        }
        features.put("pourcentageRecouvreJuridique", pourcentageRecouvreJuridique);
        
        // Durée de gestion en jours
        long dureeGestionJours = 0;
        if (dossier.getDateCreation() != null) {
            LocalDate dateCreation = new java.sql.Date(dossier.getDateCreation().getTime()).toLocalDate();
            LocalDate dateFin = dossier.getDateCloture() != null 
                ? new java.sql.Date(dossier.getDateCloture().getTime()).toLocalDate()
                : LocalDate.now();
            dureeGestionJours = ChronoUnit.DAYS.between(dateCreation, dateFin);
        }
        features.put("dureeGestionJours", (double) dureeGestionJours);
        
        // Urgence (encodage)
        features.put("urgence_Faible", dossier.getUrgence() == Urgence.FAIBLE ? 1.0 : 0.0);
        features.put("urgence_Moyenne", dossier.getUrgence() == Urgence.MOYENNE ? 1.0 : 0.0);
        // Note: Urgence n'a que FAIBLE et MOYENNE dans l'enum actuel
        
        // Type de recouvrement
        features.put("typeRecouvrement_AMIABLE", 
            dossier.getTypeRecouvrement() == TypeRecouvrement.AMIABLE ? 1.0 : 0.0);
        features.put("typeRecouvrement_JURIDIQUE", 
            dossier.getTypeRecouvrement() == TypeRecouvrement.JURIDIQUE ? 1.0 : 0.0);
        
        // ========== Features de l'Enquête ==========
        if (enquete != null) {
            features.put("enquete_chiffreAffaire", enquete.getChiffreAffaire() != null ? enquete.getChiffreAffaire() : 0.0);
            features.put("enquete_resultatNet", enquete.getResultatNet() != null ? enquete.getResultatNet() : 0.0);
            features.put("enquete_capital", enquete.getCapital() != null ? enquete.getCapital() : 0.0);
            features.put("enquete_effectif", enquete.getEffectif() != null ? enquete.getEffectif() : 0);
            
            // Encodage des champs textuels (simplifié - peut être amélioré)
            features.put("enquete_hasAppreciationBancaire", 
                enquete.getAppreciationBancaire() != null && !enquete.getAppreciationBancaire().isEmpty() ? 1.0 : 0.0);
            features.put("enquete_hasBienImmobilier", 
                enquete.getBienImmobilier() != null && !enquete.getBienImmobilier().isEmpty() ? 1.0 : 0.0);
            features.put("enquete_hasBienMobilier", 
                enquete.getBienMobilier() != null && !enquete.getBienMobilier().isEmpty() ? 1.0 : 0.0);
        } else {
            // Valeurs par défaut si pas d'enquête
            features.put("enquete_chiffreAffaire", 0.0);
            features.put("enquete_resultatNet", 0.0);
            features.put("enquete_capital", 0.0);
            features.put("enquete_effectif", 0);
            features.put("enquete_hasAppreciationBancaire", 0.0);
            features.put("enquete_hasBienImmobilier", 0.0);
            features.put("enquete_hasBienMobilier", 0.0);
        }
        
        // ========== Features des Actions ==========
        if (actions != null && !actions.isEmpty()) {
            int nbActionsTotal = actions.size();
            int nbActionsPositives = 0;
            int nbActionsNegatives = 0;
            double coutTotalActions = 0.0;
            
            // Compter par type d'action
            Map<TypeAction, Integer> actionsParType = new HashMap<>();
            
            for (Action action : actions) {
                // Compter les réponses
                if (action.getReponseDebiteur() == ReponseDebiteur.POSITIVE) {
                    nbActionsPositives++;
                } else if (action.getReponseDebiteur() == ReponseDebiteur.NEGATIVE) {
                    nbActionsNegatives++;
                }
                
                // Calculer le coût total
                if (action.getTotalCout() != null) {
                    coutTotalActions += action.getTotalCout();
                }
                
                // Compter par type
                if (action.getType() != null) {
                    actionsParType.put(action.getType(), 
                        actionsParType.getOrDefault(action.getType(), 0) + 1);
                }
            }
            
            features.put("nbActionsTotal", (double) nbActionsTotal);
            features.put("nbActionsPositives", (double) nbActionsPositives);
            features.put("nbActionsNegatives", (double) nbActionsNegatives);
            features.put("tauxReponsePositive", nbActionsTotal > 0 ? (double) nbActionsPositives / nbActionsTotal : 0.0);
            features.put("coutTotalActions", coutTotalActions);
            
            // Features par type d'action
            features.put("nbActions_APPEL", (double) actionsParType.getOrDefault(TypeAction.APPEL, 0));
            features.put("nbActions_EMAIL", (double) actionsParType.getOrDefault(TypeAction.EMAIL, 0));
            features.put("nbActions_VISITE", (double) actionsParType.getOrDefault(TypeAction.VISITE, 0));
            features.put("nbActions_LETTRE", (double) actionsParType.getOrDefault(TypeAction.LETTRE, 0));
            features.put("nbActions_AUTRE", (double) actionsParType.getOrDefault(TypeAction.AUTRE, 0));
        } else {
            // Valeurs par défaut si pas d'actions
            features.put("nbActionsTotal", 0.0);
            features.put("nbActionsPositives", 0.0);
            features.put("nbActionsNegatives", 0.0);
            features.put("tauxReponsePositive", 0.0);
            features.put("coutTotalActions", 0.0);
            features.put("nbActions_APPEL", 0.0);
            features.put("nbActions_EMAIL", 0.0);
            features.put("nbActions_VISITE", 0.0);
            features.put("nbActions_LETTRE", 0.0);
            features.put("nbActions_AUTRE", 0.0);
        }
        
        // ========== Features des Audiences ==========
        if (audiences != null && !audiences.isEmpty()) {
            int nbAudiences = audiences.size();
            int nbAudiencesFavorables = 0;
            int nbAudiencesDefavorables = 0;
            
            for (Audience audience : audiences) {
                if (audience.getResultat() == DecisionResult.POSITIVE) {
                    nbAudiencesFavorables++;
                } else if (audience.getResultat() == DecisionResult.NEGATIVE) {
                    nbAudiencesDefavorables++;
                }
                // Note: DecisionResult.Rapporter n'est pas compté comme favorable ou défavorable
            }
            
            features.put("nbAudiences", (double) nbAudiences);
            features.put("nbAudiencesFavorables", (double) nbAudiencesFavorables);
            features.put("nbAudiencesDefavorables", (double) nbAudiencesDefavorables);
            features.put("tauxAudiencesFavorables", nbAudiences > 0 ? (double) nbAudiencesFavorables / nbAudiences : 0.0);
        } else {
            // Valeurs par défaut si pas d'audiences
            features.put("nbAudiences", 0.0);
            features.put("nbAudiencesFavorables", 0.0);
            features.put("nbAudiencesDefavorables", 0.0);
            features.put("tauxAudiencesFavorables", 0.0);
        }
        
        // ========== Features des Actions Huissier ==========
        if (actionsHuissier != null && !actionsHuissier.isEmpty()) {
            int nbActionsHuissierTotal = actionsHuissier.size();
            double montantRecouvreActionsHuissier = 0.0;
            double montantRestantActionsHuissier = 0.0;
            
            // Compter par type d'action huissier
            Map<TypeActionHuissier, Integer> actionsHuissierParType = new HashMap<>();
            
            // Compter les états
            int nbActionsHuissierRecoveredTotal = 0;
            int nbActionsHuissierRecoveredPartial = 0;
            int nbActionsHuissierNotRecovered = 0;
            
            for (ActionHuissier actionHuissier : actionsHuissier) {
                // Montant recouvré
                if (actionHuissier.getMontantRecouvre() != null) {
                    montantRecouvreActionsHuissier += actionHuissier.getMontantRecouvre().doubleValue();
                }
                
                // Montant restant
                if (actionHuissier.getMontantRestant() != null) {
                    montantRestantActionsHuissier += actionHuissier.getMontantRestant().doubleValue();
                }
                
                // Compter par type
                if (actionHuissier.getTypeAction() != null) {
                    actionsHuissierParType.put(
                        actionHuissier.getTypeAction(),
                        actionsHuissierParType.getOrDefault(actionHuissier.getTypeAction(), 0) + 1
                    );
                }
                
                // Compter par état
                if (actionHuissier.getEtatDossier() != null) {
                    if (actionHuissier.getEtatDossier() == EtatDossier.RECOVERED_TOTAL) {
                        nbActionsHuissierRecoveredTotal++;
                    } else if (actionHuissier.getEtatDossier() == EtatDossier.RECOVERED_PARTIAL) {
                        nbActionsHuissierRecoveredPartial++;
                    } else if (actionHuissier.getEtatDossier() == EtatDossier.NOT_RECOVERED) {
                        nbActionsHuissierNotRecovered++;
                    }
                }
            }
            
            features.put("nbActionsHuissierTotal", (double) nbActionsHuissierTotal);
            features.put("montantRecouvreActionsHuissier", montantRecouvreActionsHuissier);
            features.put("montantRestantActionsHuissier", montantRestantActionsHuissier);
            features.put("nbActionsHuissierRecoveredTotal", (double) nbActionsHuissierRecoveredTotal);
            features.put("nbActionsHuissierRecoveredPartial", (double) nbActionsHuissierRecoveredPartial);
            features.put("nbActionsHuissierNotRecovered", (double) nbActionsHuissierNotRecovered);
            
            // Features par type d'action huissier
            features.put("nbActionsHuissier_ACLA_TA7AFOUDHIA", 
                (double) actionsHuissierParType.getOrDefault(TypeActionHuissier.ACLA_TA7AFOUDHIA, 0));
            features.put("nbActionsHuissier_ACLA_TANFITHIA", 
                (double) actionsHuissierParType.getOrDefault(TypeActionHuissier.ACLA_TANFITHIA, 0));
            features.put("nbActionsHuissier_ACLA_TAW9IFIYA", 
                (double) actionsHuissierParType.getOrDefault(TypeActionHuissier.ACLA_TAW9IFIYA, 0));
            features.put("nbActionsHuissier_ACLA_A9ARYA", 
                (double) actionsHuissierParType.getOrDefault(TypeActionHuissier.ACLA_A9ARYA, 0));
            
            // Taux de réussite des actions huissier
            double tauxReussiteActionsHuissier = 0.0;
            if (nbActionsHuissierTotal > 0) {
                int nbReussies = nbActionsHuissierRecoveredTotal + nbActionsHuissierRecoveredPartial;
                tauxReussiteActionsHuissier = (double) nbReussies / nbActionsHuissierTotal;
            }
            features.put("tauxReussiteActionsHuissier", tauxReussiteActionsHuissier);
            
        } else {
            // Valeurs par défaut si pas d'actions huissier
            features.put("nbActionsHuissierTotal", 0.0);
            features.put("montantRecouvreActionsHuissier", 0.0);
            features.put("montantRestantActionsHuissier", 0.0);
            features.put("nbActionsHuissierRecoveredTotal", 0.0);
            features.put("nbActionsHuissierRecoveredPartial", 0.0);
            features.put("nbActionsHuissierNotRecovered", 0.0);
            features.put("nbActionsHuissier_ACLA_TA7AFOUDHIA", 0.0);
            features.put("nbActionsHuissier_ACLA_TANFITHIA", 0.0);
            features.put("nbActionsHuissier_ACLA_TAW9IFIYA", 0.0);
            features.put("nbActionsHuissier_ACLA_A9ARYA", 0.0);
            features.put("tauxReussiteActionsHuissier", 0.0);
        }
        
        // ========== Features de Finance ==========
        if (dossier.getFinance() != null) {
            Finance finance = dossier.getFinance();
            features.put("finance_fraisCreationDossier", 
                finance.getFraisCreationDossier() != null ? finance.getFraisCreationDossier() : 0.0);
            features.put("finance_fraisGestionDossier", 
                finance.getFraisGestionDossier() != null ? finance.getFraisGestionDossier() : 0.0);
            features.put("finance_dureeGestionMois", 
                finance.getDureeGestionMois() != null ? finance.getDureeGestionMois() : 0);
            features.put("finance_coutActionsAmiable", 
                finance.getCoutActionsAmiable() != null ? finance.getCoutActionsAmiable() : 0.0);
            features.put("finance_coutActionsJuridique", 
                finance.getCoutActionsJuridique() != null ? finance.getCoutActionsJuridique() : 0.0);
            features.put("finance_fraisAvocat", 
                finance.getFraisAvocat() != null ? finance.getFraisAvocat() : 0.0);
            features.put("finance_fraisHuissier", 
                finance.getFraisHuissier() != null ? finance.getFraisHuissier() : 0.0);
        } else {
            // Valeurs par défaut si pas de finance
            features.put("finance_fraisCreationDossier", 0.0);
            features.put("finance_fraisGestionDossier", 0.0);
            features.put("finance_dureeGestionMois", 0);
            features.put("finance_coutActionsAmiable", 0.0);
            features.put("finance_coutActionsJuridique", 0.0);
            features.put("finance_fraisAvocat", 0.0);
            features.put("finance_fraisHuissier", 0.0);
        }
        
        return features;
    }
}


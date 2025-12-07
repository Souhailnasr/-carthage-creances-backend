package projet.carthagecreance_backend.Service.Impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projet.carthagecreance_backend.DTO.FactureDTO;
import projet.carthagecreance_backend.Entity.*;
import projet.carthagecreance_backend.DTO.SoldeFactureDTO;
import projet.carthagecreance_backend.Repository.DossierRepository;
import projet.carthagecreance_backend.Repository.FactureRepository;
import projet.carthagecreance_backend.Repository.PaiementRepository;
import projet.carthagecreance_backend.Repository.TarifDossierRepository;
import projet.carthagecreance_backend.Service.FactureService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FactureServiceImpl implements FactureService {

    private static final Logger logger = LoggerFactory.getLogger(FactureServiceImpl.class);
    private static final Double TAUX_TVA_DEFAULT = 19.0; // 19% TVA par défaut

    @Autowired
    private FactureRepository factureRepository;

    @Autowired
    private DossierRepository dossierRepository;

    @Autowired
    private TarifDossierRepository tarifDossierRepository;
    
    @Autowired
    private projet.carthagecreance_backend.Service.StatistiqueService statistiqueService;
    
    @Autowired
    private PaiementRepository paiementRepository;

    @Override
    public Facture createFacture(FactureDTO dto) {
        logger.info("Création d'une facture pour le dossier ID: {}", dto.getDossierId());

        Dossier dossier = dossierRepository.findById(dto.getDossierId())
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dto.getDossierId()));

        Facture facture = Facture.builder()
                .numeroFacture(dto.getNumeroFacture() != null ? dto.getNumeroFacture() : genererNumeroFacture())
                .dossier(dossier)
                .periodeDebut(dto.getPeriodeDebut())
                .periodeFin(dto.getPeriodeFin())
                .dateEmission(dto.getDateEmission() != null ? dto.getDateEmission() : LocalDate.now())
                .dateEcheance(dto.getDateEcheance() != null ? dto.getDateEcheance() : LocalDate.now().plusDays(30))
                .montantHT(dto.getMontantHT() != null ? dto.getMontantHT() : 0.0)
                .tva(dto.getTva() != null ? dto.getTva() : TAUX_TVA_DEFAULT)
                .statut(FactureStatut.BROUILLON)
                .envoyee(false)
                .relanceEnvoyee(false)
                .build();

        // Calculer le montant TTC
        facture.setMontantTTC(calculerMontantTTC(facture.getMontantHT(), facture.getTva()));

        return factureRepository.save(facture);
    }

    @Override
    public Optional<Facture> getFactureById(Long id) {
        return factureRepository.findById(id);
    }

    @Override
    public Optional<Facture> getFactureByNumero(String numero) {
        return factureRepository.findByNumeroFacture(numero);
    }

    @Override
    public List<Facture> getAllFactures() {
        return factureRepository.findAll();
    }

    @Override
    public List<Facture> getFacturesByDossier(Long dossierId) {
        return factureRepository.findByDossierId(dossierId);
    }

    @Override
    public List<Facture> getFacturesByStatut(FactureStatut statut) {
        return factureRepository.findByStatut(statut);
    }

    @Override
    public List<Facture> getFacturesEnRetard() {
        return factureRepository.findFacturesEnRetard(LocalDate.now());
    }

    @Override
    public Facture genererFactureAutomatique(Long dossierId, LocalDate periodeDebut, LocalDate periodeFin) {
        logger.info("Génération automatique de facture pour dossier ID: {}, période: {} - {}", 
                dossierId, periodeDebut, periodeFin);

        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));

        // ✅ NOUVEAU : Récupérer les tarifs validés (TarifDossier) au lieu de FluxFrais
        List<TarifDossier> tarifsValides = tarifDossierRepository.findByDossierIdAndStatut(
                dossierId, StatutTarif.VALIDE);

        logger.info("📊 [GENERER-FACTURE] Nombre de tarifs validés trouvés: {}", tarifsValides.size());

        if (tarifsValides == null || tarifsValides.isEmpty()) {
            logger.error("❌ [GENERER-FACTURE] Aucun tarif validé trouvé pour dossier {}", dossierId);
            
            // Diagnostic : Vérifier tous les tarifs du dossier
            List<TarifDossier> tousTarifs = tarifDossierRepository.findByDossierId(dossierId);
            logger.error("📊 [GENERER-FACTURE] Tous les tarifs du dossier {}: {}", dossierId, tousTarifs.size());
            tousTarifs.forEach(t -> logger.error("  - Tarif ID {}: phase={}, statut={}, montant={}", 
                t.getId(), t.getPhase(), t.getStatut(), t.getMontantTotal()));
            
            throw new RuntimeException("Aucun frais validé à facturer pour ce dossier");
        }

        // ✅ Calculer le montant HT depuis les TarifDossier
        Double montantHT = tarifsValides.stream()
                .mapToDouble(t -> t.getMontantTotal() != null ? t.getMontantTotal().doubleValue() : 0.0)
                .sum();

        logger.info("💰 [GENERER-FACTURE] Montant HT calculé depuis {} tarifs: {}", 
                tarifsValides.size(), montantHT);

        // Créer la facture
        Facture facture = Facture.builder()
                .numeroFacture(genererNumeroFacture())
                .dossier(dossier)
                .periodeDebut(periodeDebut)
                .periodeFin(periodeFin)
                .dateEmission(LocalDate.now())
                .dateEcheance(LocalDate.now().plusDays(30))
                .montantHT(montantHT)
                .tva(TAUX_TVA_DEFAULT)
                .statut(FactureStatut.BROUILLON)
                .envoyee(false)
                .relanceEnvoyee(false)
                .build();

        facture.setMontantTTC(calculerMontantTTC(montantHT, TAUX_TVA_DEFAULT));

        facture = factureRepository.save(facture);
        
        // Recalcul automatique des statistiques (asynchrone)
        try {
            statistiqueService.recalculerStatistiquesAsync();
        } catch (Exception e) {
            logger.warn("Erreur lors du recalcul automatique des statistiques après génération de facture automatique: {}", e.getMessage());
        }

        // ✅ Note : Les TarifDossier ne sont pas modifiés car ils restent liés au dossier
        // Le statut de validation dans Finance sera mis à jour par TarifDossierServiceImpl

        logger.info("✅ Facture générée avec succès: numero={}, montantHT={}, montantTTC={}", 
                facture.getNumeroFacture(), facture.getMontantHT(), facture.getMontantTTC());

        return facture;
    }

    @Override
    public Facture finaliserFacture(Long id) {
        logger.info("Finalisation de la facture ID: {}", id);
        
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'ID: " + id));

        if (facture.getStatut() != FactureStatut.BROUILLON) {
            throw new RuntimeException("Seules les factures en brouillon peuvent être finalisées");
        }

        facture.setStatut(FactureStatut.EMISE);
        return factureRepository.save(facture);
    }

    @Override
    public Facture envoyerFacture(Long id) {
        logger.info("Envoi de la facture ID: {}", id);
        
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'ID: " + id));

        facture.setEnvoyee(true);
        if (facture.getStatut() == FactureStatut.BROUILLON) {
            facture.setStatut(FactureStatut.EMISE);
        }

        return factureRepository.save(facture);
    }

    @Override
    public Facture relancerFacture(Long id) {
        logger.info("Relance de la facture ID: {}", id);
        
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'ID: " + id));

        facture.setRelanceEnvoyee(true);
        return factureRepository.save(facture);
    }

    @Override
    public byte[] genererPdfFacture(Long id) {
        logger.info("Génération PDF pour la facture ID: {}", id);
        
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'ID: " + id));
        
        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(baos);
            com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdfDoc);
            
            // En-tête
            document.add(new com.itextpdf.layout.element.Paragraph("FACTURE")
                    .setFontSize(24)
                    .setBold()
                    .setMarginBottom(20));
            
            // Informations facture
            document.add(new com.itextpdf.layout.element.Paragraph("Numéro: " + facture.getNumeroFacture())
                    .setFontSize(12));
            document.add(new com.itextpdf.layout.element.Paragraph("Date d'émission: " + facture.getDateEmission())
                    .setFontSize(12));
            if (facture.getDateEcheance() != null) {
                document.add(new com.itextpdf.layout.element.Paragraph("Date d'échéance: " + facture.getDateEcheance())
                        .setFontSize(12));
            }
            
            // Informations dossier
            Dossier dossier = facture.getDossier();
            if (dossier != null) {
                document.add(new com.itextpdf.layout.element.Paragraph("Dossier: " + dossier.getNumeroDossier())
                        .setFontSize(12)
                        .setMarginTop(10));
            }
            
            // Période
            if (facture.getPeriodeDebut() != null && facture.getPeriodeFin() != null) {
                document.add(new com.itextpdf.layout.element.Paragraph(
                        "Période: " + facture.getPeriodeDebut() + " - " + facture.getPeriodeFin())
                        .setFontSize(12));
            }
            
            // ✅ Tableau des frais depuis TarifDossier (au lieu de FluxFrais)
            if (dossier != null) {
                List<TarifDossier> tarifsValides = tarifDossierRepository.findByDossierIdAndStatut(
                    dossier.getId(), StatutTarif.VALIDE);
                
                if (tarifsValides != null && !tarifsValides.isEmpty()) {
                    document.add(new com.itextpdf.layout.element.Paragraph("Détail des frais:")
                            .setFontSize(14)
                            .setBold()
                            .setMarginTop(20));
                    
                    com.itextpdf.layout.element.Table table = new com.itextpdf.layout.element.Table(5);
                    table.addHeaderCell("Phase");
                    table.addHeaderCell("Catégorie");
                    table.addHeaderCell("Quantité");
                    table.addHeaderCell("Tarif unitaire");
                    table.addHeaderCell("Montant");
                    
                    for (TarifDossier tarif : tarifsValides) {
                        table.addCell(tarif.getPhase() != null ? tarif.getPhase().toString() : "");
                        table.addCell(tarif.getCategorie() != null ? tarif.getCategorie() : "");
                        table.addCell(String.valueOf(tarif.getQuantite() != null ? tarif.getQuantite() : 1));
                        table.addCell(String.format("%.2f TND", 
                            tarif.getCoutUnitaire() != null ? tarif.getCoutUnitaire().doubleValue() : 0.0));
                        table.addCell(String.format("%.2f TND", 
                            tarif.getMontantTotal() != null ? tarif.getMontantTotal().doubleValue() : 0.0));
                    }
                    
                    document.add(table);
                }
            }
            
            // Totaux
            document.add(new com.itextpdf.layout.element.Paragraph("Montant HT: " + String.format("%.2f TND", facture.getMontantHT()))
                    .setFontSize(12)
                    .setMarginTop(20));
            document.add(new com.itextpdf.layout.element.Paragraph("TVA (" + facture.getTva() + "%): " + 
                    String.format("%.2f TND", facture.getMontantHT() * facture.getTva() / 100))
                    .setFontSize(12));
            document.add(new com.itextpdf.layout.element.Paragraph("Montant TTC: " + String.format("%.2f TND", facture.getMontantTTC()))
                    .setFontSize(14)
                    .setBold()
                    .setMarginTop(10));
            
            // Statut
            document.add(new com.itextpdf.layout.element.Paragraph("Statut: " + facture.getStatut())
                    .setFontSize(12)
                    .setMarginTop(20));
            
            document.close();
            
            byte[] pdfBytes = baos.toByteArray();
            logger.info("PDF généré avec succès, taille: {} bytes", pdfBytes.length);
            return pdfBytes;
            
        } catch (Exception e) {
            logger.error("Erreur lors de la génération du PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la génération du PDF: " + e.getMessage(), e);
        }
    }

    @Override
    public String genererNumeroFacture() {
        String prefix = "FACT-" + LocalDate.now().getYear() + "-";
        Integer maxNum = factureRepository.findMaxNumeroFacture(prefix);
        int nextNum = (maxNum != null ? maxNum : 0) + 1;
        return prefix + String.format("%04d", nextNum);
    }

    @Override
    public Facture updateFacture(Long id, FactureDTO dto) {
        logger.info("Mise à jour de la facture ID: {}", id);
        
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'ID: " + id));

        facture.setPeriodeDebut(dto.getPeriodeDebut());
        facture.setPeriodeFin(dto.getPeriodeFin());
        facture.setDateEmission(dto.getDateEmission());
        facture.setDateEcheance(dto.getDateEcheance());
        facture.setMontantHT(dto.getMontantHT());
        facture.setTva(dto.getTva());
        facture.setMontantTTC(calculerMontantTTC(facture.getMontantHT(), facture.getTva()));

        return factureRepository.save(facture);
    }

    @Override
    public void deleteFacture(Long id) {
        logger.info("Suppression de la facture ID: {}", id);
        factureRepository.deleteById(id);
    }

    @Override
    public Double calculerMontantHT(List<Long> tarifDossierIds) {
        // ✅ MODIFIÉ : Utilise TarifDossier au lieu de FluxFrais
        return tarifDossierIds.stream()
                .map(tarifDossierRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .mapToDouble(t -> t.getMontantTotal() != null ? t.getMontantTotal().doubleValue() : 0.0)
                .sum();
    }

    @Override
    public Double calculerMontantTTC(Double montantHT, Double tauxTVA) {
        if (montantHT == null) return 0.0;
        if (tauxTVA == null) tauxTVA = TAUX_TVA_DEFAULT;
        return montantHT * (1 + tauxTVA / 100);
    }
    
    @Override
    public SoldeFactureDTO calculerSoldeRestant(Long factureId) {
        logger.info("Calcul du solde restant pour la facture ID: {}", factureId);
        
        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'ID: " + factureId));
        
        // Calculer le total des paiements validés
        List<Paiement> paiementsValides = paiementRepository.findByFactureIdAndStatut(
                factureId, StatutPaiement.VALIDE);
        
        BigDecimal totalPaiementsValides = paiementsValides.stream()
                .map(p -> BigDecimal.valueOf(p.getMontant() != null ? p.getMontant() : 0.0))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal montantTTC = BigDecimal.valueOf(facture.getMontantTTC() != null ? facture.getMontantTTC() : 0.0);
        BigDecimal soldeRestant = montantTTC.subtract(totalPaiementsValides);
        
        boolean estEntierementPayee = soldeRestant.compareTo(BigDecimal.ZERO) <= 0;
        
        return SoldeFactureDTO.builder()
                .factureId(factureId)
                .montantTTC(montantTTC)
                .totalPaiementsValides(totalPaiementsValides)
                .soldeRestant(soldeRestant)
                .estEntierementPayee(estEntierementPayee)
                .build();
    }
    
    @Override
    public Facture verifierEtMettreAJourStatutFacture(Long factureId) {
        logger.info("Vérification et mise à jour du statut de la facture ID: {}", factureId);
        
        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'ID: " + factureId));
        
        SoldeFactureDTO solde = calculerSoldeRestant(factureId);
        
        // Si le solde est <= 0 et tous les paiements sont validés
        if (solde.getEstEntierementPayee()) {
            // Vérifier que tous les paiements sont validés
            List<Paiement> tousPaiements = paiementRepository.findByFactureId(factureId);
            boolean tousValides = tousPaiements.stream()
                    .allMatch(p -> p.getStatut() == StatutPaiement.VALIDE || p.getStatut() == StatutPaiement.REFUSE);
            
            if (tousValides && facture.getStatut() != FactureStatut.PAYEE) {
                logger.info("Mise à jour du statut de la facture {} à PAYEE", factureId);
                facture.setStatut(FactureStatut.PAYEE);
                facture = factureRepository.save(facture);
                
                // Mettre à jour tous les frais liés
                mettreAJourStatutFrais(facture.getDossierId());
            }
        }
        
        return facture;
    }
    
    /**
     * Met à jour le statut de tous les tarifs du dossier en PAYE
     */
    private void mettreAJourStatutFrais(Long dossierId) {
        logger.info("Mise à jour du statut des frais pour le dossier ID: {}", dossierId);
        
        List<TarifDossier> tarifs = tarifDossierRepository.findByDossierId(dossierId);
        tarifs.forEach(tarif -> {
            if (tarif.getStatut() == StatutTarif.VALIDE || tarif.getStatut() == StatutTarif.FACTURE) {
                tarif.setStatut(StatutTarif.PAYE);
            }
        });
        tarifDossierRepository.saveAll(tarifs);
        
        logger.info("{} tarifs mis à jour en statut PAYE pour le dossier {}", tarifs.size(), dossierId);
    }
}


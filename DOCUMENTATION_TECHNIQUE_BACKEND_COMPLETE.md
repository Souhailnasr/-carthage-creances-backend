# 📚 Documentation Technique Complète - Backend Carthage Créances

## 📋 Table des Matières

1. [Vue d'Ensemble](#vue-densemble)
2. [Architecture et Stack Technique](#architecture-et-stack-technique)
3. [Structure du Projet](#structure-du-projet)
4. [Sécurité et Authentification](#sécurité-et-authentification)
5. [Modèle de Données](#modèle-de-données)
6. [Services et Logique Métier](#services-et-logique-métier)
7. [Contrôleurs et API REST](#contrôleurs-et-api-rest)
8. [Base de Données et Migrations](#base-de-données-et-migrations)
9. [Fonctionnalités Avancées](#fonctionnalités-avancées)
10. [Configuration et Déploiement](#configuration-et-déploiement)

---

## 🎯 Vue d'Ensemble

### Description du Projet

**Carthage Créances** est une application backend complète de gestion de créances et de recouvrement, développée en **Spring Boot 3.5.5** avec **Java 17**. Le système permet la gestion complète du cycle de vie des dossiers de créances, depuis la création jusqu'au recouvrement, en passant par les phases amiable et juridique.

### Objectifs Principaux

- ✅ Gestion complète des dossiers de créances
- ✅ Workflow de validation multi-niveaux
- ✅ Prédiction IA des risques de recouvrement
- ✅ Gestion financière et tarifaire
- ✅ Système de notifications automatiques
- ✅ Statistiques et analytics
- ✅ Gestion des utilisateurs avec contrôle d'accès basé sur les rôles (RBAC)

---

## 🏗️ Architecture et Stack Technique

### Stack Technologique

#### Framework et Core
- **Spring Boot 3.5.5** : Framework principal
- **Java 17** : Langage de programmation
- **Maven** : Gestionnaire de dépendances

#### Persistance et Base de Données
- **Spring Data JPA** : Abstraction ORM
- **Hibernate 6** : Implémentation JPA
- **MySQL 8.0+** : Base de données relationnelle
- **HikariCP** : Pool de connexions haute performance
- **Flyway** : Gestion des migrations de base de données (optionnel)

#### Sécurité
- **Spring Security** : Framework de sécurité
- **JWT (JSON Web Tokens)** : Authentification stateless
- **BCrypt** : Hachage des mots de passe
- **JJWT 0.11.5** : Bibliothèque JWT

#### Communication et API
- **Spring Web MVC** : Framework web REST
- **Jackson** : Sérialisation/désérialisation JSON
- **CORS** : Configuration cross-origin

#### Services Externes
- **JavaMailSender** : Envoi d'emails (SMTP Gmail)
- **iText 7.2.5** : Génération de PDF
- **Apache POI 5.2.5** : Export Excel
- **OpenCSV 5.9** : Traitement CSV

#### Utilitaires
- **Lombok** : Réduction du code boilerplate
- **Commons FileUpload** : Gestion des uploads de fichiers
- **Commons IO** : Utilitaires I/O

### Architecture Logicielle

Le backend suit une **architecture en couches (3-tier)** :

```
┌─────────────────────────────────────┐
│     Controllers (REST API)          │  ← Couche Présentation
├─────────────────────────────────────┤
│     Services (Business Logic)       │  ← Couche Métier
├─────────────────────────────────────┤
│     Repositories (Data Access)      │  ← Couche Données
├─────────────────────────────────────┤
│     Database (MySQL)                │  ← Persistance
└─────────────────────────────────────┘
```

#### Couche Présentation (Controllers)
- Gestion des requêtes HTTP
- Validation des entrées
- Transformation DTO ↔ Entity
- Gestion des erreurs

#### Couche Métier (Services)
- Logique métier complexe
- Orchestration des opérations
- Transactions
- Notifications et événements

#### Couche Données (Repositories)
- Accès aux données via JPA
- Requêtes personnalisées
- Optimisation des performances

---

## 📁 Structure du Projet

### Organisation des Packages

```
src/main/java/projet/carthagecreance_backend/
├── Config/                          # Configurations Spring
│   ├── AsyncConfig.java            # Configuration asynchrone
│   ├── FileUploadConfig.java       # Configuration upload fichiers
│   ├── JacksonConfig.java          # Configuration JSON
│   ├── PasswordResetScheduler.java # Planificateur reset MDP
│   └── WebMvcConfig.java           # Configuration MVC
│
├── Controller/                      # Contrôleurs REST (38 fichiers)
│   ├── AuthenticationController.java
│   ├── DossierController.java
│   ├── UtilisateurController.java
│   ├── AdminUtilisateurController.java
│   ├── FinanceController.java
│   ├── StatistiqueController.java
│   └── ... (35 autres contrôleurs)
│
├── Service/                         # Interfaces de services
│   ├── DossierService.java
│   ├── UtilisateurService.java
│   ├── FinanceService.java
│   ├── IaPredictionService.java
│   └── ... (35 autres services)
│
├── Service/Impl/                    # Implémentations des services
│   ├── DossierServiceImpl.java
│   ├── UtilisateurServiceImpl.java
│   ├── FinanceServiceImpl.java
│   ├── IaPredictionServiceImpl.java
│   └── ... (35 autres implémentations)
│
├── Entity/                          # Entités JPA (68 entités)
│   ├── Dossier.java
│   ├── Utilisateur.java
│   ├── Finance.java
│   ├── TarifDossier.java
│   └── ... (64 autres entités)
│
├── Repository/                      # Repositories JPA (30 repositories)
│   ├── DossierRepository.java
│   ├── UtilisateurRepository.java
│   ├── FinanceRepository.java
│   └── ... (27 autres repositories)
│
├── DTO/                             # Data Transfer Objects (46 DTOs)
│   ├── DossierRequest.java
│   ├── DossierResponse.java
│   └── ... (44 autres DTOs)
│
├── SecurityConfig/                   # Configuration sécurité
│   ├── SecurityConfiguration.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtService.java
│   └── ApplicationConfig.java
│
├── SecurityServices/                 # Services de sécurité
│   ├── AuthenticationService.java
│   ├── LogoutService.java
│   └── UserExtractionService.java
│
├── PayloadRequest/                   # Requêtes d'authentification
│   ├── AuthenticationRequest.java
│   └── RegisterRequest.java
│
├── PayloadResponse/                  # Réponses d'authentification
│   ├── AuthenticationResponse.java
│   └── UserProfileResponse.java
│
├── Mapper/                           # Mappers DTO ↔ Entity
│   ├── FactureMapper.java
│   ├── FinanceMapper.java
│   └── TacheUrgenteMapper.java
│
├── Event/                            # Événements applicatifs
│   └── DossierDataChangedEvent.java
│
├── Listener/                         # Écouteurs d'événements
│   └── DossierDataChangedListener.java
│
└── Scheduler/                        # Tâches planifiées
    └── LegalDelayScheduler.java
```

### Structure des Ressources

```
src/main/resources/
├── application.properties            # Configuration principale
├── db/
│   ├── migration/                    # Migrations Flyway
│   │   ├── V1_1__Add_file_path_columns_to_dossier.sql
│   │   ├── V1_2__Create_Finance_Tables.sql
│   │   ├── V1_3__Add_Unique_Constraint_TarifDossier.sql
│   │   ├── V1_4__Create_Password_Reset_Token_Table.sql
│   │   └── V1_5__Add_Createur_To_Utilisateur.sql
│   └── changelog/                    # Changelog Liquibase (optionnel)
│       └── db.changelog-master.xml
└── ia/                               # Modèles IA Python
    ├── model_classification.pkl
    ├── model_regression.pkl
    ├── feature_columns.pkl
    └── predict.py
```

---

## 🔐 Sécurité et Authentification

### Architecture de Sécurité

Le système utilise **Spring Security** avec **JWT** pour une authentification stateless et sécurisée.

#### Composants de Sécurité

1. **JwtAuthenticationFilter** : Filtre HTTP qui intercepte les requêtes et valide les tokens JWT
2. **JwtService** : Service de génération et validation des tokens JWT
3. **AuthenticationService** : Service d'authentification et d'enregistrement
4. **UserExtractionService** : Service d'extraction de l'utilisateur depuis le token JWT
5. **SecurityConfiguration** : Configuration globale de Spring Security

### Flux d'Authentification

#### 1. Inscription (`POST /auth/register`)

```java
1. Validation des données (email, mot de passe, rôle)
2. Vérification de l'unicité de l'email
3. Hachage du mot de passe avec BCrypt
4. Création de l'utilisateur
5. Génération du token JWT
6. Retour de AuthenticationResponse avec token
```

#### 2. Connexion (`POST /auth/authenticate`)

```java
1. Vérification des credentials (email + mot de passe)
2. Validation avec UserDetailsService
3. Génération du token JWT
4. Sauvegarde du token dans la table `token`
5. Mise à jour de `derniere_connexion`
6. Retour de AuthenticationResponse avec token
```

#### 3. Authentification des Requêtes

```java
1. Client envoie: Authorization: Bearer {token}
2. JwtAuthenticationFilter intercepte la requête
3. Extraction du token depuis le header
4. Validation du token (signature, expiration)
5. Vérification dans la table `token` (non expiré, non révoqué)
6. Chargement de UserDetails
7. Création de Authentication dans SecurityContext
8. Continuation de la requête
```

### Gestion des Tokens JWT

#### Configuration

```properties
# JWT Configuration
application.security.jwt.secret-key=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jdj.secure.token.validity=86400  # 24 heures (en secondes)
```

#### Structure du Token

```json
{
  "sub": "user@example.com",
  "role": ["ROLE_AGENT"],
  "userId": 123,
  "iat": 1704067200,
  "exp": 1704153600
}
```

#### Gestion de l'Expiration

- **Durée de validité** : 24 heures (86400 secondes)
- **Vérification** : À chaque requête via `JwtAuthenticationFilter`
- **Renouvellement** : Nouvelle connexion requise après expiration
- **Révocation** : Tokens marqués comme `revoked` dans la table `token`

### Contrôle d'Accès Basé sur les Rôles (RBAC)

#### Rôles Disponibles

```java
public enum RoleUtilisateur {
    SUPER_ADMIN,                    // Administrateur système
    CHEF_DEPARTEMENT_DOSSIER,       // Chef département dossiers
    CHEF_DEPARTEMENT_ENQUETE,       // Chef département enquêtes
    AGENT,                          // Agent standard
    AVOCAT,                         // Avocat
    HUISSIER                        // Huissier
}
```

#### Sécurisation des Endpoints

```java
@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('CHEF_DEPARTEMENT_DOSSIER')")
@DeleteMapping("/{id}")
public ResponseEntity<?> deleteDossier(@PathVariable Long id) {
    // Seuls SUPER_ADMIN et CHEF peuvent supprimer
}
```

### Gestion des Mots de Passe

#### Hachage

- **Algorithme** : BCrypt
- **Force** : 10 rounds (par défaut Spring Security)
- **Stockage** : Hash dans la colonne `mot_de_passe` de `utilisateur`

#### Réinitialisation de Mot de Passe

1. **Demande** (`POST /api/password/reset-request`)
   - Génération d'un token unique
   - Envoi d'email avec lien de réinitialisation
   - Token stocké dans `password_reset_token` avec expiration (1 heure)

2. **Réinitialisation** (`POST /api/password/reset`)
   - Validation du token
   - Vérification de l'expiration
   - Mise à jour du mot de passe (nouveau hash BCrypt)
   - Invalidation du token

---

## 💾 Modèle de Données

### Entités Principales

#### 1. Utilisateur

```java
@Entity
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String email;
    private String motDePasse;  // Hash BCrypt
    private String nom;
    private String prenom;
    
    @Enumerated(EnumType.STRING)
    private RoleUtilisateur roleUtilisateur;
    
    private Boolean actif;
    
    // Relation créateur (self-referencing)
    @ManyToOne
    @JoinColumn(name = "createur_id")
    private Utilisateur createur;
    
    @OneToMany(mappedBy = "createur")
    private List<Utilisateur> utilisateursCrees;
    
    // Dates
    private LocalDateTime dateCreation;
    private LocalDateTime derniereConnexion;
    private LocalDateTime derniereDeconnexion;
}
```

**Relations** :
- `createur` : Relation self-referencing pour tracer qui a créé l'utilisateur
- `utilisateursCrees` : Liste des utilisateurs créés par cet utilisateur
- `@OneToMany` avec `Dossier`, `Enquette`, `TacheUrgente`, `Notification`

#### 2. Dossier

```java
@Entity
public class Dossier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String titre;
    private String description;
    private String numeroDossier;
    private Double montantCreance;
    private Double montantTotal;
    private Double montantRecouvre;
    private Double montantRecouvrePhaseAmiable;  // NOUVEAU
    private Double montantRecouvrePhaseJuridique; // NOUVEAU
    private Double montantRestant;
    
    @Enumerated(EnumType.STRING)
    private EtatDossier etatDossier;
    
    // Prédiction IA
    private EtatDossier etatPrediction;
    private Double riskScore;
    private String riskLevel;
    private LocalDateTime datePrediction;
    
    // Relations
    @ManyToOne
    private Creancier creancier;
    
    @ManyToOne
    private Debiteur debiteur;
    
    @ManyToOne
    private Utilisateur agentCreateur;
    
    @ManyToOne
    private Utilisateur agentResponsable;
    
    @ManyToOne
    private Avocat avocat;
    
    @ManyToOne
    private Huissier huissier;
    
    // Fichiers
    private String contratSigneFilePath;
    private String pouvoirFilePath;
    
    // Dates
    private Date dateCreation;
    private Date dateCloture;
    private Date dateArchivage;
    private Boolean archive;
}
```

**Relations** :
- `@ManyToOne` avec `Creancier`, `Debiteur`, `Utilisateur`, `Avocat`, `Huissier`
- `@OneToMany` avec `Action`, `Enquette`, `Audience`, `ValidationDossier`, `TarifDossier`, `Finance`, `TacheUrgente`

#### 3. Finance

```java
@Entity
public class Finance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "dossier_id")
    private Dossier dossier;
    
    // Coûts
    private Double fraisGestionDossier;
    private Double coutActionsAmiable;
    private Double coutActionsJuridique;
    private Double coutGestion;
    
    // Durées
    private Integer dureeGestionMois;
    
    // Montants
    private Double montantTotalRecouvre;
    private Double montantRecouvreAmiable;
    private Double montantRecouvreJuridique;
    
    // Calculs automatiques
    @PrePersist
    @PreUpdate
    private void calculateTotals() {
        // Calculs automatiques des totaux
    }
}
```

#### 4. TarifDossier

```java
@Entity
@Table(name = "tarif_dossier",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"audience_id", "categorie"})
       })
public class TarifDossier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "dossier_id", nullable = false)
    private Dossier dossier;
    
    @Enumerated(EnumType.STRING)
    private PhaseFrais phase;  // AMIABLE ou JURIDIQUE
    
    private String categorie;
    private String typeElement;
    private BigDecimal coutUnitaire;
    private Integer quantite;
    private BigDecimal montantTotal;
    
    @Enumerated(EnumType.STRING)
    private StatutTarif statut;  // EN_ATTENTE_VALIDATION, VALIDE, REJETE
    
    // Relations optionnelles
    @ManyToOne
    private DocumentHuissier documentHuissier;
    
    @ManyToOne
    private ActionHuissier actionHuissier;
    
    @ManyToOne
    private Audience audience;
    
    @ManyToOne
    private Action action;  // Action amiable
    
    @ManyToOne
    private Enquette enquete;
    
    @PrePersist
    @PreUpdate
    private void calculateMontantTotal() {
        montantTotal = coutUnitaire.multiply(BigDecimal.valueOf(quantite));
    }
}
```

### Autres Entités Importantes

- **Action** : Actions de recouvrement amiable
- **ActionHuissier** : Actions de recouvrement juridique
- **Audience** : Audiences judiciaires
- **Enquette** : Enquêtes précontentieuses
- **ValidationDossier** : Validations des dossiers par les chefs
- **ValidationEnquete** : Validations des enquêtes
- **TacheUrgente** : Tâches urgentes assignées aux agents
- **Notification** : Notifications système
- **Facture** : Factures générées
- **Paiement** : Paiements reçus
- **PerformanceAgent** : Performances des agents
- **Statistique** : Statistiques système
- **Token** : Tokens JWT actifs
- **PasswordResetToken** : Tokens de réinitialisation de mot de passe

---

## ⚙️ Services et Logique Métier

### Services Principaux

#### 1. DossierService

**Interface** : `DossierService.java`  
**Implémentation** : `DossierServiceImpl.java`

**Fonctionnalités** :

- **Création de dossier** (`createDossier`)
  - Validation des données
  - Création automatique de `ValidationDossier` si créé par un agent
  - Validation automatique si créé par un chef
  - Création de `Finance` associée
  - Génération du numéro de dossier unique
  - Envoi de notifications

- **Mise à jour** (`updateDossier`)
  - Validation des droits
  - Mise à jour des montants
  - Recalcul des coûts si nécessaire
  - Historique des modifications

- **Suppression** (`deleteDossier`)
  - Vérification des validations EN_ATTENTE
  - Suppression en cascade des `TarifDossier` associés
  - Suppression des `Finance` associées
  - Suppression des relations `dossier_utilisateurs`
  - Envoi de notification

- **Recherche et filtrage** (`getAllDossiers`, `searchDossiers`)
  - Pagination
  - Tri dynamique
  - Filtres multiples (statut, urgence, créancier, etc.)
  - Spécifications JPA pour requêtes dynamiques

- **Workflow** (`validerDossier`, `cloturerDossier`, `archiverDossier`)
  - Transitions d'état contrôlées
  - Validation des règles métier
  - Notifications automatiques

#### 2. UtilisateurService

**Interface** : `UtilisateurService.java`  
**Implémentation** : `UtilisateurServiceImpl.java`

**Fonctionnalités** :

- **Création** (`createUtilisateur`)
  - Attribution automatique du `createur_id` si créé par un chef
  - Hachage du mot de passe
  - Validation de l'unicité de l'email
  - Génération du token JWT

- **Filtrage par créateur** (`getAgentsByChef`)
  - SUPER_ADMIN voit tous les utilisateurs
  - CHEF voit uniquement les agents qu'il a créés
  - Utilise `findByCreateurIdAndRoleUtilisateur`

- **Suppression** (`deleteUtilisateur`)
  - Suppression en cascade des `PerformanceAgent`
  - Suppression des `Token` JWT
  - Suppression des `PasswordResetToken`
  - Vérification des relations restantes

- **Gestion des rôles** (`updateRole`, `bloquerUtilisateur`, `debloquerUtilisateur`)

#### 3. FinanceService

**Interface** : `FinanceService.java`  
**Implémentation** : `FinanceServiceImpl.java`

**Fonctionnalités** :

- **Calcul automatique des coûts**
  - `calculerCoutActionsAmiable` : Somme des coûts des actions amiable
  - `calculerCoutActionsJuridique` : Somme des coûts des actions juridique
  - `calculerDureeGestion` : Durée en mois entre création et clôture
  - `calculerCoutGestion` : `fraisGestionDossier * dureeGestionMois`

- **Synchronisation** (`synchroniserActionsAvecFinance`)
  - Recalcul automatique lors des modifications d'actions
  - Mise à jour des montants recouvrés par phase

- **Recalcul complet** (`recalculerTousLesCouts`)
  - Recalcul de tous les coûts d'un dossier
  - Mise à jour de la `Finance` associée

#### 4. TarifDossierService

**Interface** : `TarifDossierService.java`  
**Implémentation** : `TarifDossierServiceImpl.java`

**Fonctionnalités** :

- **Création de tarifs** (`createTarifDossier`)
  - Validation des prix fixes, avances, commissions
  - Calcul automatique du `montantTotal` (coutUnitaire × quantite)
  - Statut initial : `EN_ATTENTE_VALIDATION`

- **Validation** (`validerTarif`, `rejeterTarif`)
  - Changement de statut
  - Mise à jour de `dateValidation`
  - Recalcul des coûts du dossier

- **Recherche** (`findByDossierId`, `findByDossierIdAndPhase`)
  - Filtrage par dossier et phase
  - Agrégation des montants

#### 5. IaPredictionService

**Interface** : `IaPredictionService.java`  
**Implémentation** : `IaPredictionServiceImpl.java`

**Fonctionnalités** :

- **Prédiction de risque** (`predictRisk`)
  - Construction des features depuis les données réelles
  - Exécution du script Python `predict.py`
  - Parsing de la réponse JSON
  - Retour de `riskScore` (0-100) et `riskLevel` (Faible/Moyen/Élevé)

- **Intégration Python**
  - Utilise `IaFeatureBuilderService` pour construire les features
  - Communication via fichiers JSON temporaires
  - Gestion des erreurs avec fallback

#### 6. StatistiqueService

**Interface** : `StatistiqueService.java`  
**Implémentation** : `StatistiqueServiceImpl.java`

**Fonctionnalités** :

- **Statistiques globales**
  - Nombre total de dossiers
  - Montant total des créances
  - Montant total recouvré
  - Taux de recouvrement

- **Statistiques par phase**
  - Montant recouvré phase amiable
  - Montant recouvré phase juridique
  - Nombre d'actions par phase

- **Statistiques par période**
  - Filtrage par date de création
  - Agrégations mensuelles/annuelles

#### 7. NotificationService

**Interface** : `NotificationService.java`  
**Implémentation** : `NotificationServiceImpl.java`

**Fonctionnalités** :

- **Création de notifications** (`createNotification`)
  - Notifications système
  - Notifications utilisateur
  - Notifications par email (via `EmailService`)

- **Notifications automatiques** (`AutomaticNotificationService`)
  - Création de dossier
  - Validation de dossier
  - Clôture de dossier
  - Assignation de tâche

#### 8. EmailService

**Interface** : `EmailService.java`  
**Implémentation** : `EmailServiceImpl.java`

**Fonctionnalités** :

- **Envoi d'emails** (`sendEmail`)
  - SMTP Gmail (port 465 avec SSL)
  - Templates HTML
  - Pièces jointes (optionnel)

- **Emails spécifiques**
  - Réinitialisation de mot de passe
  - Notifications système
  - Rappels de tâches

### Patterns de Conception Utilisés

#### 1. Service Layer Pattern
- Séparation claire entre logique métier et accès aux données
- Interfaces pour faciliter les tests et la maintenance

#### 2. Repository Pattern
- Abstraction de l'accès aux données
- Requêtes personnalisées via `@Query`
- Spécifications JPA pour requêtes dynamiques

#### 3. DTO Pattern
- Transformation entre entités JPA et objets de transfert
- Réduction de la surface d'exposition de l'API
- Optimisation des performances (évite les relations lazy)

#### 4. Builder Pattern
- Utilisé avec Lombok `@Builder`
- Construction d'objets complexes de manière lisible

#### 5. Strategy Pattern
- Différentes stratégies de calcul selon le contexte
- Exemple : `CoutCalculationService` avec différentes méthodes de calcul

#### 6. Observer Pattern
- Événements applicatifs (`DossierDataChangedEvent`)
- Écouteurs pour réagir aux changements

---

## 🌐 Contrôleurs et API REST

### Structure des Endpoints

Tous les endpoints suivent le pattern :
```
http://localhost:8089/carthage-creance/api/{resource}
```

### Contrôleurs Principaux

#### 1. AuthenticationController

**Base URL** : `/auth`

| Méthode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| POST | `/register` | Inscription d'un nouvel utilisateur | Public |
| POST | `/authenticate` | Connexion et génération de token JWT | Public |
| POST | `/logout` | Déconnexion et invalidation du token | Authentifié |

#### 2. DossierController

**Base URL** : `/api/dossiers`

| Méthode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| POST | `/` | Créer un nouveau dossier | Authentifié |
| GET | `/` | Liste paginée et filtrée des dossiers | Authentifié |
| GET | `/{id}` | Détails d'un dossier | Authentifié |
| PUT | `/{id}` | Mettre à jour un dossier | Authentifié |
| DELETE | `/{id}` | Supprimer un dossier | CHEF/SUPER_ADMIN |
| GET | `/search` | Recherche avancée avec filtres | Authentifié |
| POST | `/{id}/valider` | Valider un dossier | CHEF |
| POST | `/{id}/cloturer` | Clôturer un dossier | Authentifié |
| POST | `/{id}/archiver` | Archiver un dossier | Authentifié |

**Exemple de requête de création** :
```json
POST /api/dossiers
{
  "titre": "Dossier Telecom",
  "description": "Créance impayée",
  "montantCreance": 5000.0,
  "nomCreancier": "Telecom",
  "nomDebiteur": "John Doe",
  "urgence": "HAUTE"
}
```

#### 3. UtilisateurController

**Base URL** : `/api/users`

| Méthode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| GET | `/me` | Profil de l'utilisateur connecté | Authentifié |
| PUT | `/me` | Mettre à jour son profil | Authentifié |
| POST | `/` | Créer un utilisateur (public) | Public/Authentifié |

#### 4. AdminUtilisateurController

**Base URL** : `/api/admin/utilisateurs`

| Méthode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| GET | `/` | Liste des utilisateurs (filtrée par rôle) | SUPER_ADMIN/CHEF |
| POST | `/` | Créer un utilisateur (admin) | SUPER_ADMIN/CHEF |
| GET | `/{id}` | Détails d'un utilisateur | SUPER_ADMIN/CHEF |
| PUT | `/{id}` | Mettre à jour un utilisateur | SUPER_ADMIN/CHEF |
| DELETE | `/{id}` | Supprimer un utilisateur | SUPER_ADMIN |
| PUT | `/{id}/bloquer` | Bloquer un utilisateur | SUPER_ADMIN/CHEF |
| PUT | `/{id}/debloquer` | Débloquer un utilisateur | SUPER_ADMIN/CHEF |
| GET | `/agents` | Liste des agents (filtrée par créateur) | CHEF |

**Filtrage intelligent** :
- `SUPER_ADMIN` voit tous les utilisateurs
- `CHEF` voit uniquement les utilisateurs qu'il a créés
- Autres rôles voient une liste vide

#### 5. FinanceController

**Base URL** : `/api/finance`

| Méthode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| GET | `/dossier/{dossierId}` | Finance d'un dossier | Authentifié |
| PUT | `/dossier/{dossierId}` | Mettre à jour la finance | Authentifié |
| POST | `/dossier/{dossierId}/recalculer` | Recalculer tous les coûts | Authentifié |
| GET | `/analytics` | Statistiques financières | Authentifié |

#### 6. StatistiqueController

**Base URL** : `/api/statistiques`

| Méthode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| GET | `/globales` | Statistiques globales | Authentifié |
| GET | `/dossiers` | Statistiques des dossiers | Authentifié |
| GET | `/paiements` | Statistiques des paiements | Authentifié |
| GET | `/enquetes` | Statistiques des enquêtes | Authentifié |
| GET | `/montants-recouvres` | Montants recouvrés par phase | Authentifié |

#### 7. PasswordResetController

**Base URL** : `/api/password`

| Méthode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| POST | `/reset-request` | Demander une réinitialisation | Public |
| POST | `/reset` | Réinitialiser le mot de passe | Public |

### Gestion des Erreurs

#### GlobalExceptionHandler

Tous les contrôleurs utilisent un gestionnaire d'exceptions global :

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", "Not Found", "message", e.getMessage()));
    }
    
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(SQLIntegrityConstraintViolationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("error", "Constraint Violation", "message", e.getMessage()));
    }
    
    // ... autres exceptions
}
```

### Validation des Données

Utilisation de **Bean Validation** (Jakarta Validation) :

```java
@PostMapping
public ResponseEntity<?> createDossier(@Valid @RequestBody DossierRequest request,
                                      BindingResult result) {
    if (result.hasErrors()) {
        // Retourner les erreurs de validation
    }
    // ...
}
```

---

## 🗄️ Base de Données et Migrations

### Configuration de la Base de Données

#### MySQL 8.0+

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/carthage_creances
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

#### HikariCP (Pool de Connexions)

```properties
spring.datasource.hikari.connection-timeout=60000
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=600000
```

#### JPA/Hibernate

```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```

### Migrations Flyway

Le projet utilise **Flyway** pour la gestion des migrations (optionnel, actuellement désactivé).

#### Migrations Disponibles

1. **V1_1__Add_file_path_columns_to_dossier.sql**
   - Ajout des colonnes `contrat_signe_file_path` et `pouvoir_file_path`

2. **V1_2__Create_Finance_Tables.sql**
   - Création des tables `tarifs_catalogue`, `flux_frais`, `finance`, `tarif_dossier`

3. **V1_3__Add_Unique_Constraint_TarifDossier.sql**
   - Ajout de contrainte d'unicité sur `(audience_id, categorie)`

4. **V1_4__Create_Password_Reset_Token_Table.sql**
   - Création de la table `password_reset_token`

5. **V1_5__Add_Createur_To_Utilisateur.sql**
   - Ajout de la colonne `createur_id` avec relation self-referencing
   - Foreign key avec `ON DELETE SET NULL`

### Relations et Contraintes

#### Contraintes de Clés Étrangères

Toutes les relations utilisent des contraintes de clés étrangères avec gestion de la suppression :

- **ON DELETE CASCADE** : Pour les entités dépendantes (ex: `TarifDossier` → `Dossier`)
- **ON DELETE SET NULL** : Pour les relations optionnelles (ex: `Utilisateur.createur_id`)
- **ON DELETE RESTRICT** : Pour les relations critiques (ex: `Dossier` → `Creancier`)

#### Index

Index créés pour optimiser les requêtes fréquentes :

- `idx_dossier_agent_createur` sur `dossier(agent_createur_id)`
- `idx_dossier_creancier` sur `dossier(creancier_id)`
- `idx_dossier_debiteur` sur `dossier(debiteur_id)`
- `idx_tarif_dossier` sur `tarif_dossier(dossier_id)`
- `idx_utilisateur_createur` sur `utilisateur(createur_id)`

---

## 🚀 Fonctionnalités Avancées

### 1. Prédiction IA

#### Architecture

- **Modèles Python** : `model_classification.pkl` et `model_regression.pkl`
- **Script Python** : `predict.py` exécuté depuis Java
- **Features** : Construites depuis les données réelles du dossier

#### Flux de Prédiction

```java
1. Récupération des données du dossier
2. Construction des features (IaFeatureBuilderService)
3. Création d'un fichier JSON temporaire
4. Exécution du script Python
5. Parsing de la réponse JSON
6. Mise à jour du dossier (riskScore, riskLevel, datePrediction)
```

#### Features Utilisées

- Montant de la créance
- Durée depuis la création
- Nombre d'actions amiable/juridique
- Montants recouvrés
- Taux de recouvrement
- Données de l'enquête
- Historique des paiements

### 2. Système de Notifications

#### Types de Notifications

- **Système** : Notifications générées automatiquement
- **Utilisateur** : Notifications ciblées
- **Email** : Notifications par email (optionnel)

#### Canaux

- **In-app** : Stockées dans la table `notifications`
- **Email** : Envoi via SMTP
- **Push** : (À implémenter)

#### Déclencheurs Automatiques

- Création de dossier
- Validation de dossier
- Clôture de dossier
- Assignation de tâche
- Échéance de tâche
- Nouveau paiement

### 3. Gestion des Fichiers

#### FileStorageService

- **Stockage local** : Répertoire `./uploads`
- **Types supportés** : PDF, images, documents
- **Taille max** : 20MB par fichier
- **URLs** : `http://localhost:8089/carthage-creance/api/files/{filename}`

#### Types de Fichiers

- **Contrat signé** : `contrat_signe_file_path`
- **Pouvoir** : `pouvoir_file_path`
- **Documents huissier** : Stockés séparément dans `DocumentHuissier`

### 4. Export de Données

#### Formats Supportés

- **PDF** : Génération avec iText 7
- **Excel** : Export avec Apache POI
- **CSV** : Export avec OpenCSV

#### Endpoints d'Export

- `GET /api/admin/export/dossiers/pdf`
- `GET /api/admin/export/dossiers/excel`
- `GET /api/admin/export/statistiques/csv`

### 5. Tâches Planifiées

#### PasswordResetScheduler

- **Fréquence** : Toutes les heures
- **Action** : Marquer les tokens expirés comme `EXPIRE`

#### LegalDelayScheduler

- **Fréquence** : Quotidienne
- **Action** : Vérifier les délais légaux et envoyer des alertes

### 6. Analytics et Statistiques

#### Métriques Calculées

- **Taux de recouvrement global** : `(montantRecouvre / montantTotal) * 100`
- **Taux de recouvrement par phase** : Séparé amiable/juridique
- **Performance des agents** : Nombre de dossiers traités, validés, enquêtes complétées
- **Tendances** : Évolution dans le temps

#### Agrégations

- Par période (jour, mois, année)
- Par créancier
- Par agent
- Par phase de recouvrement

---

## ⚙️ Configuration et Déploiement

### Configuration Application

#### Server

```properties
server.port=8089
server.servlet.context-path=/carthage-creance
```

#### File Upload

```properties
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=20MB
spring.servlet.multipart.max-request-size=20MB
spring.servlet.multipart.file-size-threshold=2MB
file.upload-dir=./uploads
file.base-url=http://localhost:8089/carthage-creance/api/files
```

#### Email (SMTP Gmail)

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=465
spring.mail.username=souhailnasrpro98@gmail.com
spring.mail.password=oddbwteuxamfvyfq
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.ssl.enable=true
spring.mail.properties.mail.smtp.ssl.required=true
spring.mail.properties.mail.smtp.starttls.enable=false
```

#### Logging

```properties
logging.level.root=INFO
logging.level.projet.carthagecreance_backend=DEBUG
logging.level.org.hibernate.engine.jdbc=DEBUG
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %-5level - %logger{60} - %msg%n
```

### Déploiement

#### Prérequis

- Java 17+
- MySQL 8.0+
- Maven 3.6+
- Python 3.8+ (pour l'IA)

#### Étapes de Déploiement

1. **Cloner le projet**
   ```bash
   git clone <repository-url>
   cd Carthage-Creance_backend
   ```

2. **Configurer la base de données**
   - Créer la base `carthage_creances`
   - Mettre à jour `application.properties` avec les credentials

3. **Configurer l'email**
   - Générer un mot de passe d'application Gmail
   - Mettre à jour `spring.mail.username` et `spring.mail.password`

4. **Compiler et lancer**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

5. **Vérifier**
   - Accéder à `http://localhost:8089/carthage-creance/api/dossiers`
   - Vérifier les logs pour les erreurs

### Variables d'Environnement (Recommandé pour Production)

Pour la production, utiliser des variables d'environnement :

```properties
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
application.security.jwt.secret-key=${JWT_SECRET_KEY}
```

### Sécurité en Production

#### Recommandations

1. **JWT Secret Key** : Utiliser une clé forte générée aléatoirement
2. **HTTPS** : Activer HTTPS pour toutes les communications
3. **CORS** : Restreindre les origines autorisées
4. **Rate Limiting** : Implémenter la limitation de débit
5. **Logging** : Ne pas logger les mots de passe ou tokens
6. **Backup** : Mettre en place des sauvegardes régulières de la base de données

---

## 📊 Résumé des Technologies et Fonctionnalités

### Technologies Utilisées

| Catégorie | Technologies |
|-----------|-------------|
| **Framework** | Spring Boot 3.5.5, Spring Security, Spring Data JPA |
| **Langage** | Java 17 |
| **Base de Données** | MySQL 8.0+, Hibernate 6, HikariCP |
| **Sécurité** | JWT, BCrypt, Spring Security |
| **API** | REST, Jackson, CORS |
| **Email** | JavaMailSender, SMTP Gmail |
| **Fichiers** | Commons FileUpload, iText, Apache POI |
| **IA** | Python 3.8+, scikit-learn |
| **Build** | Maven |
| **Utilitaires** | Lombok, OpenCSV |

### Fonctionnalités Implémentées

✅ **Gestion des Utilisateurs**
- Inscription, authentification, gestion des rôles
- Contrôle d'accès basé sur les rôles (RBAC)
- Filtrage par créateur (chef → agents créés)
- Blocage/déblocage d'utilisateurs
- Réinitialisation de mot de passe par email

✅ **Gestion des Dossiers**
- CRUD complet avec workflow de validation
- Gestion des phases (amiable/juridique)
- Archivage et clôture
- Upload de fichiers (contrat, pouvoir)
- Recherche et filtrage avancés

✅ **Gestion Financière**
- Calcul automatique des coûts
- Gestion des tarifs par dossier
- Validation des tarifs
- Suivi des montants recouvrés par phase
- Synchronisation avec les actions

✅ **Prédiction IA**
- Prédiction du risque de recouvrement
- Score de risque (0-100)
- Niveau de risque (Faible/Moyen/Élevé)
- Intégration Python/Java

✅ **Statistiques et Analytics**
- Statistiques globales
- Statistiques par phase
- Statistiques par période
- Performance des agents
- Export PDF/Excel/CSV

✅ **Notifications**
- Notifications système
- Notifications utilisateur
- Notifications par email
- Notifications automatiques

✅ **Gestion des Tâches**
- Tâches urgentes assignées
- Priorités et échéances
- Suivi de l'avancement

✅ **Gestion des Enquêtes**
- Enquêtes précontentieuses
- Validation des enquêtes
- Association aux dossiers

---

## 📝 Conclusion

Ce backend **Carthage Créances** est une application complète et robuste qui implémente toutes les fonctionnalités nécessaires à la gestion de créances et de recouvrement. L'architecture en couches, l'utilisation de Spring Boot, et les bonnes pratiques de sécurité en font une solution professionnelle et maintenable.

### Points Forts

- ✅ Architecture claire et modulaire
- ✅ Sécurité robuste avec JWT et RBAC
- ✅ Gestion complète du cycle de vie des dossiers
- ✅ Intégration IA pour la prédiction
- ✅ Système de notifications automatiques
- ✅ Calculs financiers automatiques
- ✅ Export de données multi-formats

### Améliorations Futures Possibles

- 🔄 Cache Redis pour améliorer les performances
- 🔄 WebSockets pour les notifications en temps réel
- 🔄 Elasticsearch pour la recherche avancée
- 🔄 Microservices pour la scalabilité
- 🔄 Docker et Kubernetes pour le déploiement
- 🔄 Tests automatisés (JUnit, Mockito)
- 🔄 Documentation API avec Swagger/OpenAPI

---

**Date de création** : 2025-01-05  
**Version** : 1.0  
**Auteur** : Documentation technique complète du backend Carthage Créances


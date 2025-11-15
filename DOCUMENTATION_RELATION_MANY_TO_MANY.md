# 📚 Documentation - Relation Many-to-Many Dossier ↔ Utilisateur

## 🎯 Besoin Métier

- **Un utilisateur** peut être associé à **un ou plusieurs dossiers**
- **Un dossier** peut être associé à **un ou plusieurs utilisateurs**

C'est une relation **Many-to-Many** (Plusieurs-à-Plusieurs).

## ✅ Configuration Actuelle (DÉJÀ CORRECTE)

### Dans Dossier.java

```java
// Utilisateurs associés
@ManyToMany
@JoinTable(
    name = "dossier_utilisateurs",
    joinColumns = @JoinColumn(name = "dossier_id"),
    inverseJoinColumns = @JoinColumn(name = "utilisateur_id")
)
@Builder.Default
private List<Utilisateur> utilisateurs = new ArrayList<>();
```

**Explication** :
- `@ManyToMany` : Définit une relation plusieurs-à-plusieurs
- `@JoinTable` : Spécifie la table de jointure `dossier_utilisateurs`
- `joinColumns` : Colonne `dossier_id` (côté Dossier)
- `inverseJoinColumns` : Colonne `utilisateur_id` (côté Utilisateur)
- `@Builder.Default` : Initialise la liste à vide par défaut

### Dans Utilisateur.java

```java
@ManyToMany(mappedBy = "utilisateurs")
@JsonIgnore // Évite la récursion infinie
private List<Dossier> dossiers;
```

**Explication** :
- `@ManyToMany(mappedBy = "utilisateurs")` : Côté inverse de la relation
- `mappedBy` pointe vers le champ `utilisateurs` dans Dossier
- `@JsonIgnore` : Évite la récursion infinie lors de la sérialisation JSON

## 📊 Structure de la Table de Jointure

La table `dossier_utilisateurs` doit avoir cette structure :

```sql
CREATE TABLE dossier_utilisateurs (
    dossier_id BIGINT NOT NULL,
    utilisateur_id BIGINT NOT NULL,
    PRIMARY KEY (dossier_id, utilisateur_id),
    CONSTRAINT fk_dossier_utilisateurs_dossier 
        FOREIGN KEY (dossier_id) REFERENCES dossier(id) ON DELETE CASCADE,
    CONSTRAINT fk_dossier_utilisateurs_utilisateur 
        FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**Colonnes** :
- `dossier_id` : ID du dossier (FK vers `dossier.id`)
- `utilisateur_id` : ID de l'utilisateur (FK vers `utilisateur.id`)
- **Clé primaire composite** : (dossier_id, utilisateur_id) - empêche les doublons

## 🔧 Utilisation dans le Code

### 1. Ajouter un utilisateur à un dossier

```java
Dossier dossier = dossierRepository.findById(dossierId).orElseThrow();
Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId).orElseThrow();

// Initialiser la liste si null
if (dossier.getUtilisateurs() == null) {
    dossier.setUtilisateurs(new ArrayList<>());
}

// Ajouter l'utilisateur (éviter les doublons)
if (!dossier.getUtilisateurs().contains(utilisateur)) {
    dossier.getUtilisateurs().add(utilisateur);
}

// Sauvegarder - JPA gère automatiquement la table de jointure
dossierRepository.save(dossier);
```

### 2. Ajouter plusieurs utilisateurs à un dossier

```java
Dossier dossier = dossierRepository.findById(dossierId).orElseThrow();
List<Utilisateur> utilisateurs = utilisateurRepository.findAllById(utilisateurIds);

// Initialiser la liste si null
if (dossier.getUtilisateurs() == null) {
    dossier.setUtilisateurs(new ArrayList<>());
}

// Ajouter tous les utilisateurs (éviter les doublons)
for (Utilisateur utilisateur : utilisateurs) {
    if (!dossier.getUtilisateurs().contains(utilisateur)) {
        dossier.getUtilisateurs().add(utilisateur);
    }
}

// Sauvegarder
dossierRepository.save(dossier);
```

### 3. Récupérer tous les dossiers d'un utilisateur

```java
Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId).orElseThrow();
List<Dossier> dossiers = utilisateur.getDossiers();
```

### 4. Récupérer tous les utilisateurs d'un dossier

```java
Dossier dossier = dossierRepository.findById(dossierId).orElseThrow();
List<Utilisateur> utilisateurs = dossier.getUtilisateurs();
```

### 5. Retirer un utilisateur d'un dossier

```java
Dossier dossier = dossierRepository.findById(dossierId).orElseThrow();
Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId).orElseThrow();

if (dossier.getUtilisateurs() != null) {
    dossier.getUtilisateurs().remove(utilisateur);
    dossierRepository.save(dossier);
}
```

### 6. Retirer tous les utilisateurs d'un dossier

```java
Dossier dossier = dossierRepository.findById(dossierId).orElseThrow();
dossier.setUtilisateurs(new ArrayList<>());
dossierRepository.save(dossier);
```

## 📝 Exemple Réel : Affectation au Recouvrement Amiable

Le code actuel dans `DossierServiceImpl.affecterAuRecouvrementAmiable()` :

```java
// Initialiser la liste utilisateurs si elle est null
if (dossier.getUtilisateurs() == null) {
    dossier.setUtilisateurs(new ArrayList<>());
}

// Ajouter le chef à la liste des utilisateurs associés (éviter les doublons)
if (!dossier.getUtilisateurs().contains(chefAmiable)) {
    dossier.getUtilisateurs().add(chefAmiable);
}

// Récupérer tous les agents du département recouvrement amiable
List<Utilisateur> agentsAmiables = utilisateurRepository.findByRoleUtilisateur(
    RoleUtilisateur.AGENT_RECOUVREMENT_AMIABLE
);

// Ajouter les agents à la liste des utilisateurs associés
for (Utilisateur agent : agentsAmiables) {
    if (!dossier.getUtilisateurs().contains(agent)) {
        dossier.getUtilisateurs().add(agent);
    }
}

// Sauvegarder - JPA insère automatiquement dans dossier_utilisateurs
return dossierRepository.save(dossier);
```

**Résultat** :
- Le dossier est associé au chef amiable
- Le dossier est associé à tous les agents amiable
- Un même utilisateur peut être dans plusieurs dossiers
- Un même dossier peut avoir plusieurs utilisateurs

## ✅ Vérification de la Configuration

La configuration actuelle permet bien :
- ✅ **Un utilisateur peut être dans plusieurs dossiers** : Un chef amiable peut gérer plusieurs dossiers
- ✅ **Un dossier peut avoir plusieurs utilisateurs** : Un dossier peut avoir le chef + plusieurs agents
- ✅ **Pas de doublons** : La clé primaire composite empêche les doublons
- ✅ **Cascade DELETE** : Si un dossier est supprimé, les associations sont supprimées automatiquement

## 🔍 Requêtes SQL Utiles

### Voir tous les utilisateurs d'un dossier

```sql
SELECT u.id, u.nom, u.prenom, u.email, u.role_Utilisateur
FROM utilisateur u
INNER JOIN dossier_utilisateurs du ON u.id = du.utilisateur_id
WHERE du.dossier_id = ?;
```

### Voir tous les dossiers d'un utilisateur

```sql
SELECT d.id, d.numero_dossier, d.titre, d.type_recouvrement
FROM dossier d
INNER JOIN dossier_utilisateurs du ON d.id = du.dossier_id
WHERE du.utilisateur_id = ?;
```

### Compter les dossiers par utilisateur

```sql
SELECT u.id, u.nom, u.prenom, COUNT(du.dossier_id) as nombre_dossiers
FROM utilisateur u
LEFT JOIN dossier_utilisateurs du ON u.id = du.utilisateur_id
GROUP BY u.id, u.nom, u.prenom
ORDER BY nombre_dossiers DESC;
```

### Compter les utilisateurs par dossier

```sql
SELECT d.id, d.numero_dossier, d.titre, COUNT(du.utilisateur_id) as nombre_utilisateurs
FROM dossier d
LEFT JOIN dossier_utilisateurs du ON d.id = du.dossier_id
GROUP BY d.id, d.numero_dossier, d.titre
ORDER BY nombre_utilisateurs DESC;
```

## 🎯 Conclusion

La relation Many-to-Many est **déjà correctement configurée** dans le code. Elle permet bien :
- ✅ Un utilisateur peut être associé à un ou plusieurs dossiers
- ✅ Un dossier peut être associé à un ou plusieurs utilisateurs

Il suffit de :
1. Corriger la structure de la table `dossier_utilisateurs` (supprimer les colonnes redondantes)
2. Utiliser `dossier.getUtilisateurs().add(utilisateur)` pour associer des utilisateurs
3. JPA gère automatiquement les insertions dans la table de jointure

La configuration actuelle respecte parfaitement le besoin métier.


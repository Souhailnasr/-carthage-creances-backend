# ✅ Vérification Relation Many-to-Many Dossier ↔ Utilisateur

## 🎯 Relation Requise

- **Un utilisateur** peut être associé à **un ou plusieurs dossiers**
- **Un dossier** peut être associé à **un ou plusieurs utilisateurs**

C'est une relation **Many-to-Many** (Plusieurs-à-Plusieurs).

## ✅ Configuration Actuelle

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

✅ **Configuration CORRECTE** : 
- `@ManyToMany` : Relation plusieurs-à-plusieurs
- `@JoinTable` : Table de jointure `dossier_utilisateurs`
- `joinColumns` : Colonne `dossier_id` (côté Dossier)
- `inverseJoinColumns` : Colonne `utilisateur_id` (côté Utilisateur)

### Dans Utilisateur.java

```java
@ManyToMany(mappedBy = "utilisateurs")
@JsonIgnore // Évite la récursion infinie
private List<Dossier> dossiers;
```

✅ **Configuration CORRECTE** :
- `@ManyToMany(mappedBy = "utilisateurs")` : Côté inverse de la relation
- `mappedBy` pointe vers le champ `utilisateurs` dans Dossier

## 📊 Structure de la Table de Jointure

La table `dossier_utilisateurs` doit avoir :

| Colonne          | Type      | Description                    |
|------------------|-----------|--------------------------------|
| dossier_id       | BIGINT    | ID du dossier (FK vers dossier)|
| utilisateur_id   | BIGINT    | ID de l'utilisateur (FK vers utilisateur)|
| PRIMARY KEY      | (dossier_id, utilisateur_id) | Clé primaire composite |

## ✅ Vérification

La configuration actuelle permet bien :
- ✅ Un utilisateur peut être dans plusieurs dossiers
- ✅ Un dossier peut avoir plusieurs utilisateurs
- ✅ La table de jointure est correctement configurée

## 🔧 Utilisation dans le Code

### Ajouter un utilisateur à un dossier

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

### Récupérer tous les dossiers d'un utilisateur

```java
Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId).orElseThrow();
List<Dossier> dossiers = utilisateur.getDossiers();
```

### Récupérer tous les utilisateurs d'un dossier

```java
Dossier dossier = dossierRepository.findById(dossierId).orElseThrow();
List<Utilisateur> utilisateurs = dossier.getUtilisateurs();
```

## 📝 Exemple d'Utilisation dans DossierServiceImpl

Le code actuel dans `affecterAuRecouvrementAmiable()` est correct :

```java
// Initialiser la liste utilisateurs si elle est null
if (dossier.getUtilisateurs() == null) {
    dossier.setUtilisateurs(new ArrayList<>());
}

// Ajouter le chef à la liste des utilisateurs associés (éviter les doublons)
if (!dossier.getUtilisateurs().contains(chefAmiable)) {
    dossier.getUtilisateurs().add(chefAmiable);
}

// Ajouter les agents
for (Utilisateur agent : agentsAmiables) {
    if (!dossier.getUtilisateurs().contains(agent)) {
        dossier.getUtilisateurs().add(agent);
    }
}

// Sauvegarder - JPA insère automatiquement dans dossier_utilisateurs
return dossierRepository.save(dossier);
```

## ✅ Conclusion

La relation Many-to-Many est **déjà correctement configurée** dans le code. Il suffit de :
1. Corriger la structure de la table `dossier_utilisateurs` (supprimer les colonnes redondantes)
2. Utiliser `dossier.getUtilisateurs().add(utilisateur)` pour associer des utilisateurs
3. JPA gère automatiquement les insertions dans la table de jointure

La configuration actuelle respecte bien le besoin :
- ✅ Un utilisateur peut être dans plusieurs dossiers
- ✅ Un dossier peut avoir plusieurs utilisateurs


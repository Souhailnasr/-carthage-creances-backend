# 🔐 Guide : Générer un Mot de Passe d'Application Gmail

## 🎯 Pourquoi un Mot de Passe d'Application ?

Gmail ne permet plus d'utiliser votre mot de passe normal pour les applications tierces. Vous devez créer un **"mot de passe d'application"** spécialement pour votre application backend.

---

## 📋 Étapes Détaillées

### Étape 1 : Accéder à votre Compte Google

1. **Ouvrez votre navigateur** (Chrome, Firefox, Edge, etc.)
2. **Allez sur :** https://myaccount.google.com/
3. **Connectez-vous** avec votre compte Gmail : `souhailnasr80@gmail.com`

---

### Étape 2 : Activer la Validation en 2 Étapes (Obligatoire)

**⚠️ IMPORTANT :** La validation en 2 étapes doit être activée pour pouvoir générer un mot de passe d'application.

#### Si la validation en 2 étapes n'est PAS activée :

1. **Dans la page de votre compte Google**, cliquez sur **"Sécurité"** (menu de gauche ou en haut)
2. **Cherchez la section "Connexion à Google"**
3. **Trouvez "Validation en deux étapes"**
4. **Cliquez sur "Validation en deux étapes"**
5. **Suivez les instructions** pour l'activer :
   - Vous devrez confirmer votre numéro de téléphone
   - Vous recevrez un code par SMS
   - Entrez le code pour confirmer
6. **Activez la validation en 2 étapes**

#### Si la validation en 2 étapes est DÉJÀ activée :

✅ Vous pouvez passer directement à l'Étape 3.

---

### Étape 3 : Accéder aux Mots de Passe des Applications

1. **Toujours dans la page "Sécurité"** de votre compte Google
2. **Cherchez la section "Connexion à Google"**
3. **Trouvez "Mots de passe des applications"**
   - C'est généralement juste en dessous de "Validation en deux étapes"
4. **Cliquez sur "Mots de passe des applications"**

**Note :** Si vous ne voyez pas "Mots de passe des applications", c'est que la validation en 2 étapes n'est pas activée. Retournez à l'Étape 2.

---

### Étape 4 : Générer un Nouveau Mot de Passe d'Application

1. **Dans la page "Mots de passe des applications"**, vous verrez :
   - Une liste des mots de passe d'application existants (si vous en avez déjà créé)
   - Un menu déroulant "Sélectionner une application"
   - Un champ "Sélectionner un appareil"

2. **Dans le menu déroulant "Sélectionner une application"** :
   - Cliquez sur le menu
   - **Sélectionnez "Autre (nom personnalisé)"** (en bas de la liste)

3. **Dans le champ qui apparaît** :
   - Entrez un nom descriptif : `Carthage Creances Backend`
   - Ou simplement : `Backend Email`

4. **Cliquez sur "Générer"**

---

### Étape 5 : Copier le Mot de Passe Généré

1. **Google va afficher un mot de passe de 16 caractères**
   - Format : `xxxx xxxx xxxx xxxx` (4 groupes de 4 caractères séparés par des espaces)
   - Exemple : `abcd efgh ijkl mnop`

2. **⚠️ IMPORTANT : COPIEZ ce mot de passe immédiatement !**
   - Vous ne pourrez **plus jamais le voir** après avoir fermé cette page
   - Google ne le stocke pas en clair

3. **Notez-le dans un endroit sûr** (temporairement, pour le copier dans `application.properties`)

4. **Cliquez sur "Terminé"**

---

## 📝 Exemple Visuel du Processus

```
1. https://myaccount.google.com/
   ↓
2. Menu "Sécurité"
   ↓
3. Section "Connexion à Google"
   ↓
4. "Mots de passe des applications"
   ↓
5. Menu "Sélectionner une application" → "Autre (nom personnalisé)"
   ↓
6. Nom : "Carthage Creances Backend"
   ↓
7. Cliquez "Générer"
   ↓
8. Copiez le mot de passe : "abcd efgh ijkl mnop"
   ↓
9. Cliquez "Terminé"
```

---

## 🔧 Utiliser le Mot de Passe dans `application.properties`

Une fois que vous avez copié le mot de passe, ajoutez-le dans votre fichier `application.properties` :

**Fichier :** `src/main/resources/application.properties`

```properties
# Email Configuration - Gmail
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=souhailnasr80@gmail.com
spring.mail.password=abcd efgh ijkl mnop
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

**⚠️ Remplacez :**
- `souhailnasr80@gmail.com` → Votre email (déjà correct)
- `abcd efgh ijkl mnop` → Le mot de passe d'application que vous venez de générer

**Note :** Vous pouvez mettre le mot de passe avec ou sans espaces, les deux fonctionnent.

---

## 🆘 Problèmes Courants

### Problème 1 : "Mots de passe des applications" n'apparaît pas

**Cause :** La validation en 2 étapes n'est pas activée.

**Solution :**
1. Activez d'abord la validation en 2 étapes (Étape 2)
2. Attendez quelques minutes
3. Rechargez la page "Sécurité"
4. "Mots de passe des applications" devrait maintenant apparaître

---

### Problème 2 : "Vous devez activer la validation en 2 étapes"

**Cause :** Google exige la validation en 2 étapes pour les mots de passe d'application.

**Solution :**
1. Allez dans "Sécurité" → "Validation en deux étapes"
2. Activez-la en suivant les instructions
3. Confirmez avec votre téléphone
4. Retournez ensuite aux "Mots de passe des applications"

---

### Problème 3 : J'ai perdu le mot de passe généré

**Cause :** Google ne stocke pas les mots de passe d'application en clair.

**Solution :**
1. Allez dans "Mots de passe des applications"
2. **Supprimez** l'ancien mot de passe (icône poubelle)
3. **Générez un nouveau** mot de passe d'application
4. **Copiez-le immédiatement** cette fois !

---

### Problème 4 : Le menu "Autre (nom personnalisé)" n'apparaît pas

**Cause :** Interface Google mise à jour.

**Solution :**
1. Cherchez un bouton **"Créer"** ou **"Générer"** directement
2. Ou cherchez **"Autre"** dans la liste
3. Si rien ne fonctionne, essayez depuis un autre navigateur

---

## 📱 Alternative : Via l'Application Google

Vous pouvez aussi générer un mot de passe d'application depuis l'application Google sur votre téléphone :

1. **Ouvrez l'application Google** sur votre téléphone
2. **Allez dans "Gérer votre compte Google"**
3. **Onglet "Sécurité"**
4. **"Mots de passe des applications"**
5. Suivez les mêmes étapes

---

## ✅ Checklist Rapide

- [ ] Aller sur https://myaccount.google.com/
- [ ] Se connecter avec `souhailnasr80@gmail.com`
- [ ] Aller dans "Sécurité"
- [ ] Activer "Validation en deux étapes" (si pas déjà fait)
- [ ] Cliquer sur "Mots de passe des applications"
- [ ] Sélectionner "Autre (nom personnalisé)"
- [ ] Entrer le nom : `Carthage Creances Backend`
- [ ] Cliquer sur "Générer"
- [ ] **COPIER le mot de passe** (16 caractères)
- [ ] Ajouter dans `application.properties`
- [ ] Redémarrer l'application backend

---

## 🔒 Sécurité

**⚠️ IMPORTANT :**
- Ne partagez **jamais** votre mot de passe d'application
- Ne le commitez **pas** dans Git (utilisez des variables d'environnement en production)
- Si vous pensez qu'il est compromis, **supprimez-le** et générez-en un nouveau

---

## 📝 Résumé

1. **Activer la validation en 2 étapes** (obligatoire)
2. **Aller dans "Mots de passe des applications"**
3. **Sélectionner "Autre (nom personnalisé)"**
4. **Entrer un nom** : `Carthage Creances Backend`
5. **Générer** le mot de passe
6. **Copier** le mot de passe (16 caractères)
7. **Ajouter** dans `application.properties`

---

**Date :** 2025-01-05  
**Status :** ✅ Guide complet pour générer un mot de passe d'application Gmail

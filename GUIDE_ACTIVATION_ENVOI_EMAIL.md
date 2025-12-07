# 📧 Guide Complet : Activation de l'Envoi d'Emails

## 🎯 Objectif

Activer l'envoi réel d'emails pour la réinitialisation de mot de passe et recevoir les emails dans votre boîte mail.

---

## 📋 Prérequis : Ce que vous devez fournir

### Option 1 : Gmail (Recommandé pour débuter)

**Ce dont vous avez besoin :**
1. ✅ Un compte Gmail
2. ✅ Un **mot de passe d'application** (pas votre mot de passe normal)
3. ✅ Accès à votre compte Google

**Informations à récupérer :**
- Email Gmail : `votre-email@gmail.com`
- Mot de passe d'application : (à générer, voir étapes ci-dessous)
- Serveur SMTP : `smtp.gmail.com`
- Port : `587`

### Option 2 : Outlook/Hotmail

**Ce dont vous avez besoin :**
1. ✅ Un compte Outlook/Hotmail
2. ✅ Votre mot de passe Outlook
3. ✅ Accès à votre compte Microsoft

**Informations à récupérer :**
- Email Outlook : `votre-email@outlook.com`
- Mot de passe : Votre mot de passe Outlook
- Serveur SMTP : `smtp-mail.outlook.com`
- Port : `587`

### Option 3 : SendGrid (Recommandé pour production)

**Ce dont vous avez besoin :**
1. ✅ Un compte SendGrid (gratuit : 100 emails/jour)
2. ✅ Une clé API SendGrid

**Informations à récupérer :**
- Clé API SendGrid : (à générer depuis le dashboard SendGrid)
- Serveur SMTP : `smtp.sendgrid.net`
- Port : `587`
- Username : `apikey`
- Password : Votre clé API

---

## 🔧 Étape 1 : Générer un Mot de Passe d'Application (Gmail)

### Pour Gmail uniquement

**IMPORTANT :** Gmail ne permet plus d'utiliser votre mot de passe normal. Vous devez créer un "mot de passe d'application".

**Procédure :**

1. **Aller sur votre compte Google**
   - Allez sur : https://myaccount.google.com/
   - Connectez-vous avec votre compte Gmail

2. **Activer la validation en 2 étapes** (si pas déjà fait)
   - Allez dans "Sécurité"
   - Activez "Validation en deux étapes" (obligatoire pour les mots de passe d'application)

3. **Créer un mot de passe d'application**
   - Allez dans "Sécurité"
   - Cherchez "Mots de passe des applications"
   - Cliquez sur "Mots de passe des applications"
   - Sélectionnez "Autre (nom personnalisé)"
   - Entrez un nom : `Carthage Creances Backend`
   - Cliquez sur "Générer"
   - **COPIEZ le mot de passe généré** (16 caractères, espaces séparés)
   - ⚠️ **IMPORTANT :** Vous ne pourrez plus voir ce mot de passe après. Notez-le bien !

**Exemple de mot de passe d'application :**
```
abcd efgh ijkl mnop
```
(16 caractères, espaces entre chaque groupe de 4)

---

## 🔧 Étape 2 : Vérifier la Dépendance Maven

**Fichier :** `pom.xml`

**Vérification :**
Ouvrez `pom.xml` et vérifiez que cette dépendance existe :

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

**Si elle n'existe pas :**
Ajoutez-la dans la section `<dependencies>` de votre `pom.xml`.

---

## 🔧 Étape 3 : Configurer SMTP dans `application.properties`

**Fichier :** `src/main/resources/application.properties`

### Configuration pour Gmail

**Remplacez les valeurs commentées par :**

```properties
# Email Configuration - Gmail
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=votre-email@gmail.com
spring.mail.password=abcd efgh ijkl mnop
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000
```

**⚠️ IMPORTANT :**
- Remplacez `votre-email@gmail.com` par votre email Gmail réel
- Remplacez `abcd efgh ijkl mnop` par votre mot de passe d'application (sans espaces ou avec espaces, les deux fonctionnent)
- Ne commitez JAMAIS ce fichier avec les mots de passe dans Git !

### Configuration pour Outlook

```properties
# Email Configuration - Outlook
spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587
spring.mail.username=votre-email@outlook.com
spring.mail.password=votre-mot-de-passe-outlook
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

### Configuration pour SendGrid

```properties
# Email Configuration - SendGrid
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=votre-cle-api-sendgrid
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

## 🔧 Étape 4 : Activer le Code d'Envoi dans `EmailServiceImpl.java`

**Fichier :** `src/main/java/projet/carthagecreance_backend/Service/Impl/EmailServiceImpl.java`

### Modifications à faire :

1. **Ajouter l'injection de `JavaMailSender`**

   Au début de la classe, ajoutez :
   ```java
   @Autowired
   private JavaMailSender mailSender;
   ```

2. **Décommenter le code d'envoi**

   Trouvez cette section (lignes 37-53) :
   ```java
   // Exemple d'intégration avec JavaMailSender (à décommenter et configurer) :
   /*
   try {
       MimeMessage message = mailSender.createMimeMessage();
       ...
   */
   ```

   **Supprimez les commentaires `/*` et `*/`** pour activer le code.

3. **Commenter ou supprimer l'appel à `logEmail()`**

   Remplacez :
   ```java
   logEmail(email, subject, body);
   ```
   
   Par :
   ```java
   // logEmail(email, subject, body); // Désactivé pour envoi réel
   ```

### Code final attendu :

```java
@Override
public void sendPasswordResetEmail(String email, String nom, String token) {
    log.info("Envoi d'un email de réinitialisation à: {}", email);
    
    // Construire le lien de réinitialisation
    String resetLink = frontendUrl + "/reset-password?token=" + token;
    
    // Construire le sujet
    String subject = "Réinitialisation de votre mot de passe - " + appName;
    
    // Construire le corps de l'email
    String body = buildEmailBody(nom, resetLink);
    
    // Envoyer l'email réellement
    try {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setTo(email);
        helper.setSubject(subject);
        helper.setText(body, true); // true = HTML
        
        mailSender.send(message);
        log.info("Email de réinitialisation envoyé avec succès à: {}", email);
    } catch (MessagingException e) {
        log.error("Erreur lors de l'envoi de l'email: {}", e.getMessage(), e);
        throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
    }
}
```

---

## 🔧 Étape 5 : Ajouter les Imports Nécessaires

**Fichier :** `EmailServiceImpl.java`

**Ajoutez ces imports en haut du fichier :**

```java
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
```

---

## 🔧 Étape 6 : Redémarrer l'Application

1. **Arrêter** le serveur backend (si en cours d'exécution)
2. **Redémarrer** le serveur backend
3. **Vérifier les logs** pour confirmer que la configuration SMTP est chargée

**Logs attendus au démarrage :**
```
Spring Mail configuration loaded
```

---

## 🧪 Étape 7 : Tester l'Envoi d'Email

### Test 1 : Demander une Réinitialisation

1. **Ouvrir l'application frontend**
2. **Aller sur la page "Mot de passe oublié"**
3. **Entrer un email valide** (celui configuré ou un autre)
4. **Cliquer sur "Envoyer"**

### Test 2 : Vérifier les Logs

**Dans les logs du serveur, vous devriez voir :**
```
Envoi d'un email de réinitialisation à: utilisateur@example.com
Email de réinitialisation envoyé avec succès à: utilisateur@example.com
```

**Si erreur :**
```
Erreur lors de l'envoi de l'email: [détails de l'erreur]
```

### Test 3 : Vérifier la Boîte Mail

1. **Ouvrir la boîte mail** de l'utilisateur
2. **Vérifier le dossier "Spam"** aussi (au cas où)
3. **Chercher un email** avec le sujet : "Réinitialisation de votre mot de passe - Carthage Créances"
4. **Ouvrir l'email** et vérifier le contenu
5. **Cliquer sur le lien** pour tester la réinitialisation

---

## ⚠️ Problèmes Courants et Solutions

### Problème 1 : "Authentication failed"

**Cause :** Mot de passe incorrect ou mot de passe d'application non utilisé (Gmail)

**Solution :**
- Vérifiez que vous utilisez un **mot de passe d'application** (pas votre mot de passe normal) pour Gmail
- Vérifiez que le mot de passe est correct dans `application.properties`
- Pour Gmail, assurez-vous que la validation en 2 étapes est activée

### Problème 2 : "Connection timeout"

**Cause :** Problème de connexion réseau ou firewall

**Solution :**
- Vérifiez votre connexion internet
- Vérifiez que le port 587 n'est pas bloqué par un firewall
- Essayez le port 465 avec SSL au lieu de 587 avec STARTTLS

### Problème 3 : "Email dans Spam"

**Cause :** Configuration SPF/DKIM manquante

**Solution :**
- Vérifiez le dossier "Spam" de la boîte mail
- Pour production, configurez SPF/DKIM pour votre domaine
- Utilisez un service professionnel (SendGrid, AWS SES) qui gère cela automatiquement

### Problème 4 : "JavaMailSender is null"

**Cause :** `JavaMailSender` n'est pas injecté

**Solution :**
- Vérifiez que `@Autowired` est présent
- Vérifiez que la dépendance `spring-boot-starter-mail` est dans `pom.xml`
- Redémarrez l'application

### Problème 5 : "Port 587 refused"

**Cause :** Port bloqué ou serveur SMTP incorrect

**Solution :**
- Essayez le port 465 avec SSL :
  ```properties
  spring.mail.port=465
  spring.mail.properties.mail.smtp.ssl.enable=true
  ```

---

## 📊 Checklist Complète

### Avant de Commencer

- [ ] Avoir un compte email (Gmail, Outlook, ou SendGrid)
- [ ] Pour Gmail : Avoir activé la validation en 2 étapes
- [ ] Pour Gmail : Avoir généré un mot de passe d'application
- [ ] Avoir accès au fichier `application.properties`
- [ ] Avoir accès au fichier `EmailServiceImpl.java`

### Configuration

- [ ] Vérifier que `spring-boot-starter-mail` est dans `pom.xml`
- [ ] Configurer SMTP dans `application.properties`
- [ ] Ajouter l'injection de `JavaMailSender` dans `EmailServiceImpl.java`
- [ ] Décommenter le code d'envoi dans `EmailServiceImpl.java`
- [ ] Ajouter les imports nécessaires
- [ ] Commenter l'appel à `logEmail()`

### Test

- [ ] Redémarrer l'application
- [ ] Vérifier les logs au démarrage
- [ ] Tester la demande de réinitialisation
- [ ] Vérifier les logs d'envoi
- [ ] Vérifier la boîte mail (et spam)
- [ ] Tester le lien de réinitialisation

---

## 🔒 Sécurité : Protection des Mots de Passe

### ⚠️ IMPORTANT : Ne jamais commiter les mots de passe

**Solution 1 : Variables d'Environnement (Recommandé)**

1. **Créer un fichier `.env`** (ne pas le commiter dans Git)
   ```
   SPRING_MAIL_USERNAME=votre-email@gmail.com
   SPRING_MAIL_PASSWORD=votre-mot-de-passe
   ```

2. **Modifier `application.properties`** :
   ```properties
   spring.mail.username=${SPRING_MAIL_USERNAME}
   spring.mail.password=${SPRING_MAIL_PASSWORD}
   ```

3. **Ajouter `.env` dans `.gitignore`**

**Solution 2 : Fichier de Configuration Externe**

1. Créer un fichier `application-local.properties` (ne pas commiter)
2. Y mettre les configurations SMTP
3. Utiliser ce fichier en local uniquement

---

## 📝 Résumé des Informations à Fournir

### Pour Gmail :
- ✅ Email Gmail : `votre-email@gmail.com`
- ✅ Mot de passe d'application : `abcd efgh ijkl mnop` (16 caractères)
- ✅ Serveur SMTP : `smtp.gmail.com`
- ✅ Port : `587`

### Pour Outlook :
- ✅ Email Outlook : `votre-email@outlook.com`
- ✅ Mot de passe Outlook : `votre-mot-de-passe`
- ✅ Serveur SMTP : `smtp-mail.outlook.com`
- ✅ Port : `587`

### Pour SendGrid :
- ✅ Clé API SendGrid : `SG.xxxxxxxxxxxxx`
- ✅ Serveur SMTP : `smtp.sendgrid.net`
- ✅ Port : `587`
- ✅ Username : `apikey`

---

## 🎯 Prochaines Étapes

1. **Choisir un service email** (Gmail recommandé pour débuter)
2. **Générer les identifiants** (mot de passe d'application pour Gmail)
3. **Configurer `application.properties`** avec vos identifiants
4. **Modifier `EmailServiceImpl.java`** pour activer l'envoi
5. **Tester** avec un vrai email
6. **Vérifier** que l'email est bien reçu

---

**Date :** 2025-01-05  
**Status :** ✅ Guide complet - Prêt pour activation


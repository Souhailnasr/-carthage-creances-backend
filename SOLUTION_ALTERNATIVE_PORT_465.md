# 🔧 Solution Alternative : Port 465 avec SSL

## 🎯 Problème

L'erreur d'authentification persiste même avec le nouveau mot de passe d'application.

**Erreur :** `AuthenticationFailedException: 535-5.7.8 Username and Password not accepted`

---

## ✅ Solution Appliquée : Port 465 avec SSL

J'ai modifié la configuration pour utiliser le **port 465 avec SSL** au lieu du port 587 avec STARTTLS.

**Raison :** Certains réseaux ou configurations bloquent le port 587, mais le port 465 avec SSL fonctionne souvent mieux.

---

## 📋 Configuration Modifiée

**Fichier :** `src/main/resources/application.properties`

**Nouvelle configuration :**
```properties
# Email Configuration - Gmail (Port 465 avec SSL)
spring.mail.host=smtp.gmail.com
spring.mail.port=465
spring.mail.username=souailnasrpro98@gmail.com
spring.mail.password=oddbwteuxamfvyfq
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.ssl.enable=true
spring.mail.properties.mail.smtp.ssl.required=true
spring.mail.properties.mail.smtp.starttls.enable=false
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000
```

**Changements :**
- ✅ Port : `465` (au lieu de `587`)
- ✅ `mail.smtp.ssl.enable=true` (SSL direct)
- ✅ `mail.smtp.ssl.required=true`
- ✅ `mail.smtp.starttls.enable=false` (pas de STARTTLS)

---

## 🚀 Prochaines Étapes

### 1. Redémarrer l'Application Backend

1. **Arrêter** le serveur backend (si en cours d'exécution)
2. **Redémarrer** le serveur backend
3. **Vérifier les logs** - L'erreur d'authentification ne devrait plus apparaître

---

### 2. Vérifier les Logs

**Logs attendus (succès) :**
```
Spring Mail configuration loaded
```

**Logs à éviter (erreur) :**
```
AuthenticationFailedException: 535-5.7.8 Username and Password not accepted
```

---

### 3. Tester l'Envoi d'Email

1. **Ouvrir l'application frontend**
2. **Aller sur la page "Mot de passe oublié"**
3. **Entrer un email valide**
4. **Cliquer sur "Envoyer"**
5. **Vérifier la boîte mail** (et le dossier Spam)

---

## ⚠️ Si l'Erreur Persiste Encore

### Vérification 1 : Vérifier le Mot de Passe d'Application

1. **Allez sur :** https://myaccount.google.com/apppasswords
2. **Vérifiez** que le mot de passe `oddb wteu xamf vyfq` existe toujours
3. **Vérifiez** que vous utilisez le bon email : `souailnasrpro98@gmail.com`
4. **Si le mot de passe n'existe plus :** Régénérez-en un nouveau

---

### Vérification 2 : Vérifier la Validation en 2 Étapes

1. **Allez sur :** https://myaccount.google.com/security
2. **Vérifiez** que "Validation en deux étapes" est **activée**
3. **Si non activée :** Activez-la d'abord, puis régénérez un mot de passe d'application

---

### Vérification 3 : Vérifier les Restrictions du Compte

**Certains comptes Gmail peuvent avoir des restrictions :**

1. **Compte "moins sécurisé" :** Vérifiez que votre compte n'est pas marqué comme "moins sécurisé"
2. **Accès par application :** Vérifiez que l'accès par application est autorisé
3. **Blocage temporaire :** Gmail peut bloquer temporairement après plusieurs tentatives échouées

**Solution :** Attendre quelques minutes et réessayer.

---

### Vérification 4 : Vérifier le Format du Mot de Passe

**Dans `application.properties`, le mot de passe doit être :**
- Sans espaces : `oddbwteuxamfvyfq` ✅
- Pas avec espaces : `oddb wteu xamf vyfq` ❌

**Vérifiez** que dans votre fichier `application.properties`, le mot de passe est bien :
```properties
spring.mail.password=oddbwteuxamfvyfq
```

---

## 🔄 Comparaison : Port 587 vs Port 465

| Aspect | Port 587 (STARTTLS) | Port 465 (SSL) |
|--------|---------------------|-----------------|
| **Protocole** | STARTTLS (upgrade) | SSL direct |
| **Sécurité** | ✅ Sécurisé | ✅ Sécurisé |
| **Compatibilité** | Parfois bloqué | Généralement accepté |
| **Configuration** | `starttls.enable=true` | `ssl.enable=true` |

---

## 📋 Checklist de Dépannage

- [x] Configuration modifiée pour port 465 avec SSL
- [ ] **Redémarrer l'application backend**
- [ ] Vérifier les logs (pas d'erreur d'authentification)
- [ ] Vérifier que le mot de passe d'application existe sur Google
- [ ] Vérifier que la validation en 2 étapes est activée
- [ ] Tester l'envoi d'email
- [ ] Vérifier la boîte mail (et spam)

---

## 🆘 Si Rien ne Fonctionne

### Option Alternative : Désactiver le Health Check Mail

L'erreur peut venir du **health check** de Spring Boot Actuator qui teste la connexion au démarrage.

**Pour désactiver temporairement le health check mail :**

**Fichier :** `application.properties`

```properties
# Désactiver le health check mail (temporaire)
management.health.mail.enabled=false
```

**Note :** Cela désactive uniquement le health check, pas l'envoi d'emails. L'envoi d'emails fonctionnera toujours normalement.

---

## 📝 Résumé

✅ **Configuration modifiée :**
- Port : `465` avec SSL (au lieu de `587` avec STARTTLS)
- Email : `souailnasrpro98@gmail.com`
- Mot de passe : `oddbwteuxamfvyfq` (sans espaces)

**Prochaine étape :** Redémarrer l'application backend et tester !

---

**Date :** 2025-01-05  
**Status :** ✅ Configuration modifiée pour port 465 avec SSL - À tester


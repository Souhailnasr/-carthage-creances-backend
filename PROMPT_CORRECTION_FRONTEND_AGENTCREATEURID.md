# Prompts pour corriger le frontend - Problème agentCreateurId

## Contexte
Le frontend envoie `agentCreateurId` dans le JSON lors de la création d'une enquête, mais le backend a été modifié pour accepter ce champ. Cependant, il faut s'assurer que le frontend envoie correctement ce champ et gère les réponses du backend.

## Prompt 1 : Vérifier et corriger l'interface TypeScript Enquette

**Prompt à utiliser :**
```
Dans le projet Angular, localisez l'interface TypeScript qui définit le modèle Enquette (probablement dans un fichier models/enquette.ts ou similaire).

Vérifiez que l'interface contient les champs suivants :
- agentCreateurId?: number; (pour l'ID de l'agent créateur)
- agentResponsableId?: number; (pour l'ID de l'agent responsable)

Si ces champs n'existent pas, ajoutez-les à l'interface Enquette.

Assurez-vous également que l'interface contient :
- agentCreateur?: Utilisateur; (objet Utilisateur complet, optionnel)
- agentResponsable?: Utilisateur; (objet Utilisateur complet, optionnel)

Ces champs sont utilisés pour recevoir les données complètes depuis le backend lors de la lecture d'une enquête.
```

## Prompt 2 : Corriger le composant de création d'enquête

**Prompt à utiliser :**
```
Dans le composant Angular qui gère la création d'enquête (probablement create-enquete.component.ts), localisez la méthode qui envoie la requête POST pour créer une enquête.

Assurez-vous que lors de la création de l'objet Enquette à envoyer au backend :
1. Le champ agentCreateurId est correctement défini avec l'ID de l'utilisateur connecté
2. Le champ agentCreateur (objet complet) n'est PAS envoyé dans la requête POST (seulement l'ID)
3. Si agentResponsableId est utilisé, il doit également être défini correctement

Exemple de structure attendue :
{
  rapportCode: "...",
  agentCreateurId: 33,  // ID numérique, pas l'objet complet
  agentResponsableId: null,  // ou un ID si applicable
  // ... autres champs
}

Modifiez le code pour s'assurer que seul agentCreateurId (et agentResponsableId si nécessaire) est envoyé, et non l'objet agentCreateur complet.
```

## Prompt 3 : Extraire l'ID utilisateur depuis le token JWT

**Prompt à utiliser :**
```
Dans le service Angular qui gère l'authentification (probablement auth.service.ts ou token.service.ts), créez ou modifiez une méthode pour extraire l'ID de l'utilisateur depuis le token JWT stocké.

La méthode doit :
1. Récupérer le token JWT depuis le localStorage ou sessionStorage
2. Décoder le token (le payload contient userId)
3. Retourner l'ID de l'utilisateur (userId)

Exemple de structure du token JWT (d'après les logs) :
{
  userId: 33,
  email: "souhailnsrpro98@gmail.com",
  nom: "nasr",
  prenom: "souhail",
  role: "AGENT_DOSSIER"
}

Créez une méthode comme getCurrentUserId(): number | null qui retourne l'ID de l'utilisateur connecté.
```

## Prompt 4 : Utiliser l'ID utilisateur dans le formulaire de création

**Prompt à utiliser :**
```
Dans le composant create-enquete.component.ts, modifiez la méthode qui prépare les données avant l'envoi au backend.

Assurez-vous que :
1. Vous injectez le service d'authentification (AuthService ou similaire)
2. Vous récupérez l'ID de l'utilisateur connecté via la méthode créée précédemment
3. Vous assignez cet ID au champ agentCreateurId de l'objet Enquette avant l'envoi

Exemple de code :
const currentUserId = this.authService.getCurrentUserId();
if (currentUserId) {
  enquetteData.agentCreateurId = currentUserId;
}

Vérifiez que cette assignation se fait AVANT l'appel au service qui envoie la requête POST.
```

## Prompt 5 : Gérer les erreurs du backend

**Prompt à utiliser :**
```
Dans le service Angular qui gère les appels API pour les enquêtes (probablement enquete.service.ts), modifiez la méthode createEnquette pour mieux gérer les erreurs.

Assurez-vous que :
1. Les erreurs 500 (Internal Server Error) sont capturées et affichées de manière claire
2. Les messages d'erreur du backend sont affichés à l'utilisateur
3. Si l'erreur concerne un Utilisateur non trouvé (agentCreateurId invalide), un message spécifique est affiché

Exemple de gestion d'erreur :
catch (error: any) {
  if (error.status === 500) {
    const errorMessage = error.error?.message || 'Erreur lors de la création de l\'enquête';
    console.error('Erreur serveur:', errorMessage);
    // Afficher le message à l'utilisateur
  }
  throw error;
}
```

## Prompt 6 : Vérifier le format des données envoyées

**Prompt à utiliser :**
```
Dans le composant create-enquete.component.ts, ajoutez un console.log juste avant l'envoi de la requête POST pour vérifier le format des données.

Le log doit afficher l'objet Enquette complet qui sera envoyé au backend.

Vérifiez que :
- agentCreateurId est un nombre (number), pas un objet
- agentCreateur (objet complet) n'est PAS présent dans l'objet envoyé
- Tous les autres champs sont correctement formatés

Exemple :
console.log('Données à envoyer:', JSON.stringify(enquetteData, null, 2));

Si agentCreateur (objet) est présent, supprimez-le avant l'envoi :
delete enquetteData.agentCreateur;
delete enquetteData.agentResponsable;
```

## Prompt 7 : Corriger le service d'enquête

**Prompt à utiliser :**
```
Dans le service enquete.service.ts, vérifiez la méthode createEnquette.

Assurez-vous que :
1. La méthode accepte un objet Enquette avec agentCreateurId
2. L'objet est correctement sérialisé en JSON
3. Les headers HTTP sont correctement configurés (Content-Type: application/json)
4. Le token JWT est inclus dans les headers d'autorisation

Exemple de structure :
createEnquette(enquette: Enquette): Observable<Enquette> {
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${this.getToken()}`
  };
  
  return this.http.post<Enquette>(`${this.apiUrl}/enquettes`, enquette, { headers });
}
```

## Prompt 8 : Gérer correctement l'erreur 404 lors de la vérification d'existence d'enquête

**Prompt à utiliser :**
```
Dans le composant create-enquete.component.ts (ligne 234 environ), localisez l'appel à getEnqueteByDossier qui vérifie si une enquête existe déjà pour un dossier.

Le problème : Cette méthode retourne une erreur 404 si aucune enquête n'existe pour le dossier. C'est un comportement NORMAL lors de la création d'une nouvelle enquête, mais le frontend traite actuellement cette erreur comme une erreur critique.

Solution : Modifiez la gestion de cette erreur pour traiter le 404 comme un cas normal (pas d'enquête existante) et non comme une erreur.

Exemple de code à modifier :
// AVANT (traite 404 comme erreur)
this.enqueteService.getEnqueteByDossier(dossierId).subscribe({
  next: (enquete) => { /* enquête existe */ },
  error: (error) => { 
    console.error('Erreur:', error); // ← Affiche 404 comme erreur
  }
});

// APRÈS (traite 404 comme cas normal)
this.enqueteService.getEnqueteByDossier(dossierId).subscribe({
  next: (enquete) => { 
    // Enquête existe déjà
    if (enquete) {
      // Gérer le cas où l'enquête existe déjà
    }
  },
  error: (error) => { 
    // 404 est normal si aucune enquête n'existe
    if (error.status === 404) {
      // Pas d'enquête existante, on peut continuer la création
      console.log('Aucune enquête existante pour ce dossier, création possible');
      // Ne pas afficher d'erreur à l'utilisateur
    } else {
      // Autres erreurs (500, 401, etc.) doivent être gérées
      console.error('Erreur lors de la vérification:', error);
    }
  }
});

OU mieux encore, modifiez le service pour retourner null au lieu de lever une erreur :
// Dans enquete.service.ts
getEnqueteByDossier(dossierId: number): Observable<Enquette | null> {
  return this.http.get<Enquette>(`${this.apiUrl}/enquettes/dossier/${dossierId}`)
    .pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 404) {
          // 404 = pas d'enquête, c'est normal
          return of(null);
        }
        // Autres erreurs doivent être propagées
        return throwError(() => error);
      })
    );
}
```

## Résumé des modifications nécessaires

1. **Interface TypeScript** : Ajouter `agentCreateurId?: number` et `agentResponsableId?: number`
2. **Service d'authentification** : Créer une méthode pour extraire `userId` du token JWT
3. **Composant de création** : Assigner `agentCreateurId` avec l'ID de l'utilisateur connecté
4. **Service d'enquête** : S'assurer que seul l'ID est envoyé, pas l'objet complet
5. **Gestion d'erreurs** : Améliorer la gestion des erreurs 500
6. **Gestion 404** : Traiter le 404 lors de la vérification d'existence comme un cas normal (pas d'enquête existante)
7. **Debug** : Ajouter des logs pour vérifier le format des données envoyées

## Fichiers à modifier (probablement)

- `src/app/models/enquette.ts` ou similaire (interface)
- `src/app/services/auth.service.ts` ou `token.service.ts` (extraction userId)
- `src/app/components/create-enquete/create-enquete.component.ts` (logique de création)
- `src/app/services/enquete.service.ts` (appel API)

## Test après correction

1. Ouvrir la console du navigateur
2. Créer une nouvelle enquête
3. Vérifier dans l'onglet Network que la requête POST contient `agentCreateurId` (nombre) et non `agentCreateur` (objet)
4. Vérifier que la création réussit sans erreur 500


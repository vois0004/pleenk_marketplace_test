# Marketplace Pleenk

Application de marketplace avec intégration du système de paiement Pleenk.

## Prérequis

- Java 21
- Node.js 18+
- Maven 3.9+
- [ngrok](https://ngrok.com/) (pour les webhooks en local)

## Installation

### 1. Backend (Spring Boot)

Le back nécessite un fichier de secrets. Veuillez indiquer les variables dans le fichier martketplace/main/src/resources/application.properties.

Les tests nécessitent aussi un fichier de secrets. Veuillez indiquer les variables dans le fichier martketplace/test/src/resources/application-test.properties.

avec tests :
```bash
cd marketplace
mvn clean install
```
sans tests :
```bash
cd marketplace
mvn clean install -Dmaven.test.skip=true

```

### 2. Frontend (Angular)

```bash
cd marketplace_front
npm install
```

## Lancement

### Étape 1 : Démarrer ngrok

Pleenk a besoin d'envoyer des webhooks à votre backend. En local, ngrok crée un tunnel public vers votre machine.

```bash
ngrok http 8080
```

Vous obtiendrez une URL du type :
```
https://xxxx-xxxx-xxxx.ngrok-free.app
```

### Étape 2 : Configurer l'URL du webhook

1. Allez sur https://app.sandbox.pleenk.com/fr/account/confidentiality-wallet
2. Modifier Url de webhook avec l'url ngrok en laissant /api/webhooks/pleenk à la fin

### Étape 3 : Démarrer le backend

```bash
cd marketplace
mvn spring-boot:run
```

Le backend démarre sur http://localhost:8080

- **Swagger UI** : [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui/index.html#/)

### Étape 4 : Démarrer le frontend

```bash
cd marketplace_front
npm start
```

Le frontend démarre sur http://localhost:4200
## Utilisation

1. Ouvrez http://localhost:4200
2. Parcourez les produits disponibles
3. Cliquez sur **Payer** sur un produit
4. Choisissez la quantité et cliquez sur **Procéder au paiement**
5. Complétez le paiement dans le widget Pleenk
6. Le résultat (succès/échec) s'affiche automatiquement

## Tests

Les tests nécessitent un fichier de secrets. Veuillez indiquer les variables dans le fichier martketplace/test/src/resources/application-test.properties.

```bash
mvn -f marketplace/pom.xml test

```

## Troubleshooting
### Base de données H2
La base est en mémoire (`create-drop`). Les données sont réinitialisées à chaque redémarrage.

### Utilisation de pleenk
- Des réponses **403** ont été observées lorsque la signature du webhook était invalide (ex. URL incorrecte).
- Après réception d’une signature invalide, il semble que l’adresse IP soit temporairement bloquée, ce qui complique les tests en local.
- Lors des essais où l’URL du webhook était :
    - définie dynamiquement dans le code
    - puis transmise dans le lien de paiement  
      => des erreurs 403 sont apparues.
- Depuis la suppression de cette configuration dynamique (en utilisant uniquement l’URL de webhook définie dans l’account Pleenk), le comportement est redevenu nominal.
- En pratique, j'ai eu l'impression que l’URL de webhook utilisée était celle configurée dans l’account, indépendamment de celle indiquée dans le code et fournie dans le lien de paiement.

#### API GraphQL
- Lors de la générération du lien de paiement, des erreurs GraphQL apparaissent parfois selon le nom, la description ou le montant du produit.

#### Parcours de paiement
- Si l’utilisateur initie un paiement, puis se déconnecte et se reconnecte avant de finaliser :
    - le paiement ne peut plus être effectué.

#### Rechargement par carte
- Lors des tests de rechargement par carte, j'ai rencontré des erreurs techniques intermittentes.
- Le comportement varie selon la carte utilisée, sans schéma clairement reproductible.

#### Annulation de transaction
- Lorsqu’une transaction est annulée, aucun événement ne semble être envoyé au webhook.

#### Tests des cas d’échec
- Je n'ai pas trouvé le moyen de simuler ou forcer un paiement en statut `FAILED` à des fins de test.

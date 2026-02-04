# Marketplace Pleenk

Application de marketplace avec intégration du système de paiement Pleenk.

## Prérequis

- Java 21
- Node.js 18+
- Maven 3.9+
- [ngrok](https://ngrok.com/) (pour les webhooks en local)

## Installation

### 1. Backend (Spring Boot)

```bash
cd marketplace
mvn clean install
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

Copiez l'URL ngrok et mettez à jour le webhook dans le wallet utilisé.

### Étape 3 : Démarrer le backend

```bash
cd marketplace
mvn spring-boot:run
```

Le backend démarre sur http://localhost:8080

- **Swagger UI** : http://localhost:8080/swagger-ui.html

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

## Docker

### Prérequis

- Docker
- Docker Compose

### Lancement avec Docker

1. Copiez le fichier d'environnement :
```bash
cp .env.example .env
```

2. Remplissez les valeurs dans `.env` avec vos clés Pleenk

3. Lancez les conteneurs :
```bash
docker-compose up --build
```

L'application est accessible sur :
- **Frontend** : http://localhost:4200
- **Backend API** : http://localhost:8080
- **Swagger UI** : http://localhost:8080/swagger-ui.html

### Arrêter les conteneurs

```bash
docker-compose down
```

## Tests

```bash
cd marketplace
mvn test
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
      des erreurs 403 sont apparues.
- Depuis la suppression de cette configuration dynamique (en utilisant uniquement l’URL de webhook définie dans l’account Pleenk), le comportement est redevenu nominal.
- En pratique, il semble que **l’URL de webhook utilisée soit celle configurée dans l’account**, indépendamment de celle éventuellement fournie dans le lien de paiement.

#### Utilisation des wallets
- Il est possible d’utiliser le **même wallet pour encaisser et effectuer un paiement**.
- Ce comportement est pratique pour les tests mais peut surprendre d’un point de vue fonctionnel.

#### API GraphQL
- Certaines requêtes GraphQL retournent des erreurs lorsque la **description du lien de paiement contient des caractères numériques**.
- Ce point n’est pas mentionné dans la documentation.

#### Parcours de paiement
- Si l’utilisateur initie un paiement, puis se **déconnecte et se reconnecte** avant de finaliser :
    - le paiement ne peut plus être effectué.

#### Rechargement par carte
- Lors des tests de rechargement par carte, des **erreurs techniques intermittentes** ont été observées.
- Le comportement varie selon la carte utilisée, sans schéma clairement reproductible.

#### Annulation de transaction
- Lorsqu’une transaction est annulée, **aucun événement ne semble être envoyé au webhook**.

#### Tests des cas d’échec
- Aucun moyen clair n’a été identifié pour **simuler ou forcer un paiement en statut `FAILED`** à des fins de test.
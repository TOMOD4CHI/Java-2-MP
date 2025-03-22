# Système de Gestion Auto-École

Ce système de gestion est conçu pour les auto-écoles, permettant la gestion des moniteurs, candidats, véhicules, séances de conduite et examens.

## Fonctionnalités

- Gestion des auto-écoles (informations, logo, directeur)
- Gestion des candidats (inscription, suivi, documents, examens)
- Gestion des moniteurs et leurs spécialités
- Gestion des véhicules et leur entretien
- Planification et suivi des séances (code, conduite, examens blancs)
- Génération de factures et suivi des paiements

## Prérequis

- Java 8 ou supérieur
- JavaFX
- MySQL 5.7 ou supérieur

## Installation

1. Cloner le dépôt:
   ```
   git clone https://github.com/ayoub/systeme-gestion-auto-ecole.git
   ```

2. Importer le projet dans votre IDE (Eclipse, IntelliJ, etc.)

3. Configurer la base de données:
   - Créer une base de données MySQL nommée `auto_ecole`
   - Exécuter le script SQL de création des tables: `database_fixes.sql`

4. Configurer la connexion à la base de données:
   - Ouvrir le fichier `src/main/java/org/cpi2/utils/DatabaseConnection.java`
   - Modifier les paramètres de connexion si nécessaire:
     ```java
     private static final String DB_URL = "jdbc:mysql://localhost:3306/auto_ecole?useSSL=false&serverTimezone=UTC";
     private static final String DB_USER = "root";
     private static final String DB_PASSWORD = "";
     ```

5. Exécuter l'application via la classe `Main.java`

## Structure du Projet

- **src/main/java/org/cpi2/**
  - **controllers/** - Contient les contrôleurs JavaFX
  - **entitties/** - Contient les classes modèles
  - **service/** - Contient les services métier
  - **repository/** - Contient les classes d'accès aux données
  - **utils/** - Contient les classes utilitaires

- **src/main/resources/**
  - **css/** - Fichiers CSS pour le style de l'application
  - **fxmls/** - Fichiers FXML définissant les interfaces
  - **images/** - Images et logos utilisés dans l'application

## Guide de Développement

Pour ajouter une nouvelle fonctionnalité:

1. Créer une classe entité si nécessaire
2. Créer un service pour gérer la logique métier
3. Créer un fichier FXML pour l'interface utilisateur
4. Créer un contrôleur pour gérer les interactions

## Configuration de la Base de Données

Si vous devez réinitialiser ou mettre à jour la structure de la base de données, exécutez le script `database_fixes.sql` qui contient toutes les instructions SQL nécessaires.

# Auto-Ecole Management System
Feel free to edit this file and/or modify it with new features
**Ideas**:
- **Use Icons instead of buttons** (kinda cool tbh)
## Emphasis on Threads and Parallel Programming
The application could leverages **multithreading and parallel programming** to enhance performance and responsiveness:
- **Background Tasks**: Real-time notifications for document expiration and session reminders
- **User Input Prioritization**: Ensures that while performing background tasks the system should remains highly responsive to user input providing a smooth user experience during form filling or navigation (Ayoub's Idea tho)
- **Data Processing**: Efficient data retrieval and updates in the dashboard using asynchronous threads

## Business Rules
### B6
- Each candidate should consistently use the same vehicle and  preferably  the same instructor for all practical sessions

### B7
- Vehicles older than 10 years must undergo a technical inspection every year
- Comprehensive history of each vehicle including repairs and maintenance  is maintained

### B8
- Future updates will include advanced features for tracking success rates and optimizing resource allocation

## Architecture
The application must use the layered Architecture (yall aleardy know this)


## UML Diagrams
Still don't know shit about this 

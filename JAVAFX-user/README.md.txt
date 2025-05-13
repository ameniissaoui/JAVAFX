# ChronoSerena - Application JavaFX pour la Gestion des Maladies Chroniques

## Aperçu
Ce projet a été développé dans le cadre d'un cours de développement d'applications Java à **Esprit School of Engineering**. ChronoSerena est une application de bureau complète construite avec JavaFX qui vise à aider les prestataires de soins de santé et les patients à gérer plus efficacement les maladies chroniques grâce à une plateforme intégrée avec plusieurs modules de gestion.

## Table des matières
- [Description du projet](#description-du-projet)
- [Fonctionnalités](#fonctionnalités)
- [Stack technologique](#stack-technologique)
- [Structure des répertoires](#structure-des-répertoires)
- [Installation](#installation)
- [Utilisation](#utilisation)
- [Membres de l'équipe et responsabilités](#membres-de-léquipe-et-responsabilités)
- [Captures d'écran](#captures-décran)
- [Directives de contribution](#directives-de-contribution)
- [Licence](#licence)
- [Remerciements](#remerciements)

## Description du projet
ChronoSerena est une application de bureau JavaFX robuste conçue pour simplifier la gestion des maladies chroniques. Le système fournit une suite complète d'outils pour les médecins, les patients et les administrateurs, permettant un suivi efficace, une surveillance et une gestion de divers aspects liés aux affections chroniques. 

Notre solution répond aux défis de la gestion des maladies chroniques en créant une plateforme intégrée qui couvre:
- La gestion des utilisateurs et authentification
- Le suivi des traitements médicaux
- La planification des rendez-vous
- Le monitoring des modifications du mode de vie
- La gestion des produits de santé
- L'organisation d'événements éducatifs

L'application propose trois interfaces distinctes adaptées aux besoins spécifiques des médecins, des patients et des administrateurs système.

## Fonctionnalités

### Module de gestion des utilisateurs
- Inscription et authentification avec trois rôles : médecin, patient et administrateur
- Fonctionnalités de base: changement de mot de passe, login avec Google, gestion de profil
- Gestion des profils utilisateurs et contrôle d'accès basé sur les rôles
- Tableau de bord personnalisé selon le type d'utilisateur (médecin, patient, admin)
- Gestion des droits d'accès et des autorisations spécifiques à chaque rôle
- Tableau de bord administrateur avec statistiques et informations détaillées sur l'application
- Validation des comptes médecins par l'administrateur en fonction de leurs diplômes

### Module de gestion des traitements
- Suivi et gestion des médicaments
- Création et suivi des plans de traitement
- Rappels de médication et suivi de l'observance

### Module de gestion des rendez-vous
- Planification des rendez-vous entre patients et médecins
- Calendriers personnalisés pour les médecins
- Système de notification pour les patients et médecins
- Génération de fichiers PDF avec QR codes pour chaque rendez-vous
- Synchronisation avec Google Calendar pour la gestion des créneaux
- Recherche avancée pour vérifier la disponibilité des créneaux
- Historique des rendez-vous passés et suivis

### Module de gestion du mode de vie
- Création de demandes journalières par les patients (eau consommée, repas, activité physique)
- Recommandations personnalisées des médecins basées sur les demandes des patients
- Génération de plans alimentaires via IA (API Gemini)
- Dashboard analytique avec score de santé et courbes de suivi
- Téléchargement des demandes et recommandations en PDF
- Taux de suivi des recommandations

### Module de gestion des produits
- Catalogage avec images générées par IA
- Gestion des stocks en temps réel
- Système de paiement intégré avec Stripe
- Vérification par SMS via Twilio
- Système de favoris pour les utilisateurs
- Historique des commandes et facturation

### Module de gestion des événements
- Calendrier d'événements liés à la santé
- Consultation des événements avec détails (lieu, date, météo, image)
- Réservation de places aux événements via formulaire
- Génération de billets avec QR codes
- Affichage des événements passés et à venir

## Stack technologique
- **Frontend** : JavaFX
- **Backend** : Java
- **Base de données** : MySQL
- **Outil de construction** : Maven
- **API d'intégration** :
  - Stripe (paiement en ligne)
  - Twilio (vérification SMS)
  - Google Calendar (synchronisation de rendez-vous)
  - Gemini (génération de plans alimentaires IA)

## Structure des répertoires
```
JAVAFX-user/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── org/example/
│   │   │   │   ├── controllers/
|   |   |   |   ├── interfaces/
│   │   │   │   ├── models/
│   │   │   │   ├── services/
│   │   │   │   ├── utils/
│   │   │   │   └── ChronoSerenaApp
│   │   ├── resources/
│   │   │   ├── fxml/
│   │   │   ├── css/
│   │   │   |── images/
|   |   |   |── soundes/
│   |
│   │   
├── pom.xml
└── README.md
```

## Installation

1. Clonez le dépôt :
   ```bash
   git clone https://github.com/ameniissaoui/JAVAFX.git
   cd JAVAFX
   ```

2. Assurez-vous d'avoir installé :
   * Kit de développement Java (JDK) 11 ou supérieur
   * JavaFX SDK 17 ou supérieur
   * Serveur MySQL
   * Maven ou Gradle (selon votre configuration de build)
   * XAMPP (pour la base de données MySQL)

3. Configurez la base de données :
   * Créez une base de données MySQL nommée `finalee`
   * Importez le schéma de base de données depuis `database/schema.sql`

4. Configurez la connexion à la base de données :
   * Ouvrez `src/main/resources/config.properties`
   * Mettez à jour les paramètres de connexion à la base de données

5. Configurez les API externes (facultatif) :
   * Créez un compte Stripe et configurez les clés API
   * Créez un compte Twilio pour les notifications SMS
   * Configurez l'accès à l'API Google Calendar
   * Configurez l'accès à l'API Gemini pour la génération IA

6. Construisez le projet :
   ```bash
   mvn clean install
   ```

## Utilisation

1. Exécutez l'application :
   ```bash
   mvn javafx:run
   ```

2. Connectez-vous avec l'un des rôles suivants :
   * **Administrateur**
     - Nom d'utilisateur : admin
     - Mot de passe : admin123
     - Accès à tous les modules et fonctions de gestion système
     - Tableau de bord avec statistiques complètes
     - Validation des comptes médecins
   
   * **Médecin**
     - Interface dédiée à la gestion des patients, traitements et rendez-vous
     - Création de recommandations personnalisées pour les patients
     - Gestion de planning et de rendez-vous
     - Accès au suivi médical et aux historiques des patients
   
   * **Patient**
     - Interface personnalisée pour la gestion de sa propre santé
     - Création de demandes journalières de mode de vie
     - Réservation de rendez-vous médicaux
     - Commande de produits et réservation d'événements
     - Visualisation de son dashboard analytique

3. Naviguez à travers les différents modules à l'aide du menu latéral, adapté à votre rôle.

## Membres de l'équipe et responsabilités

Notre équipe est composée de 6 membres, chacun responsable d'un module de gestion spécifique :

1. **[Ameni Issaoui]** - Module de gestion des utilisateurs
   * Authentification et inscription des utilisateurs
   * Intégration de Google Login
   * Gestion des profils et des permissions
   * Tableau de bord administrateur
   * Contrôle d'accès basé sur les rôles

2. **[Oumaima Ouerfelli]** - Module de gestion des traitements
   * Suivi et planification des médicaments
   * Plans et protocoles de traitement
   * Surveillance de l'observance des médicaments

3. **[Mohamed Jaffel]** - Module de gestion des rendez-vous
   * Planification des rendez-vous des patients
   * Calendriers des prestataires de soins de santé
   * Intégration avec Google Calendar
   * Génération de QR codes et PDF
   * Rappels de rendez-vous et suivis

4. **[Anas Selmi]** - Module de gestion du mode de vie
   * Formulaires de demandes journalières
   * Recommandations personnalisées
   * Intégration avec l'API Gemini pour plans IA
   * Visualisation et analyse des données de mode de vie
   * Dashboard analytique avec courbes de suivi

5. **[Yomna Bouallegue]** - Module de gestion des produits
   * Inventaire des fournitures médicales
   * Intégration avec Stripe pour paiements
   * Système de favoris et panier
   * Génération d'images par IA
   * Vérification par SMS via Twilio

6. **[Mariem Negra]** - Module de gestion des événements
   * Événements d'éducation à la santé
   * Système de réservation avec places limitées
   * Génération de billets avec QR codes
   * Affichage des informations météo
   * Planification des groupes de soutien

## Captures d'écran
![image](https://github.com/user-attachments/assets/8f710169-2978-42db-83b1-f57c7accfc0e)
![image](https://github.com/user-attachments/assets/a4cbbf72-71cb-4491-8a39-07cd0afc168a)

### Interface Administrateur
![image](https://github.com/user-attachments/assets/2eb6204d-63ef-4406-afca-16719e48cf63)
![image](https://github.com/user-attachments/assets/9bc0f93b-2b6d-4bb8-8be8-743f801e5e43)
![Capture d'écran 2025-04-23 194825](https://github.com/user-attachments/assets/57d0a7c2-87f1-46b2-9369-9cbd292dbd38)

### Interface Médecin
![image](https://github.com/user-attachments/assets/76556b20-4e5a-4144-a208-51abd9da1ba0)
![image](https://github.com/user-attachments/assets/62df63a3-21ff-4462-82e2-f8fdc8de2800)
![image](https://github.com/user-attachments/assets/ec73f57d-41e1-46a3-8259-02f11a3db559)
![image](https://github.com/user-attachments/assets/724b10d0-5feb-4382-b866-85067017547a)
![image](https://github.com/user-attachments/assets/03ae8d7c-e05a-4a64-b737-a1bb3d1ed36b)

### Interface Patient
![image](https://github.com/user-attachments/assets/a0a23ab1-7bec-45db-821b-bf89a05267a0)
![image](https://github.com/user-attachments/assets/ad38b7b6-9085-4c8d-84d6-84f35e227b82)
![image](https://github.com/user-attachments/assets/d16468cc-00e0-4143-a096-ef13c7632b62)
![image](https://github.com/user-attachments/assets/89ba9b50-3f7a-4bfb-8707-7a7ed347cbfd)

## Directives de contribution

Nous accueillons favorablement les contributions pour améliorer ChronoSerena. Pour contribuer :

1. Forkez le dépôt
2. Créez une nouvelle branche (`git checkout -b branche-fonctionnalite`)
3. Effectuez vos modifications
4. Committez vos modifications (`git commit -m 'Ajouter une fonctionnalité'`)
5. Poussez vers la branche (`git push origin branche-fonctionnalite`)
6. Ouvrez une Pull Request

Veuillez vous assurer que votre code respecte nos normes de codage et inclut la documentation appropriée.

## Licence

Ce projet est sous licence MIT - voir le fichier [LICENSE](LICENSE) pour plus de détails.

## Remerciements

Ce projet a été réalisé sous la direction de **[Emna Charfi]** à **Esprit School of Engineering**.

Remerciements spéciaux à tous les contributeurs et ressources qui ont rendu ce projet possible.

---
© 2025 Équipe ChronoSerena - Tous droits réservés
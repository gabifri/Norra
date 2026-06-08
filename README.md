# 🌿 Norra - Votre Compagnon Quotidien de Bien-être Mental

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-M3-green.svg?style=flat&logo=android)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Firebase_Auth-Secure-orange.svg?style=flat&logo=firebase)](https://firebase.google.com)
[![Privacy](https://img.shields.io/badge/Privacy-100%25_Local_Data-brightgreen.svg?style=flat)](#-sécurité--vie-privée-privacy-first)

**Norra** est une application Android moderne, bienveillante et sécurisée, conçue pour vous accompagner au quotidien dans l'exploration de vos émotions, votre introspection et votre parcours de soin thérapeutique. 

Pensée comme un sanctuaire intime et respectueux, l'application applique une philosophie stricte de **respect de la vie privée (Privacy-First)** : vos pensées, notes, émotions, rendez-vous et données de santé restent exclusivement stockés localement sur votre appareil.

---

## 🚀 Fonctionnalités Clés

### 📖 1. Journal Intime & Suivi des Émotions
*   **Bilan Émotionnel :** Renseignez quotidiennement votre humeur globale pour visualiser vos variations d'états d'esprit dans le temps.
*   **Journal Personnel de Pensées :** Un espace d'écriture libre pour décharger votre mental, noter vos victoires ou comprendre vos doutes, sans aucune pression.
*   **Mémoire 100% Locale :** Tout votre historique est conservé en local sur votre appareil pour une confidentialité absolue.

### 🧘‍♀️ 2. Exercices de Respiration Guidés
*   **Séances d'Apaisement :** Des cycles de respiration rythmés pour abaisser le niveau de stress, surmonter des épisodes d'angoisse ou de panique, ou simplement se recentrer.
*   **Gestion des Favoris :** Enregistrez les techniques de respiration qui vous font le plus de bien pour les retrouver instantanément en cas de besoin urgent.

### 👩‍⚕️ 3. Suivi Thérapeutique & Médical
*   **Préparation de Consultations :** Notez à l'avance les thèmes ou questions clés à partager avec votre psychologue ou thérapeute pour optimiser vos séances.
*   **Journal de Séance :** Gardez une trace écrite de vos réflexions et avancées post-consultation.
*   **Gestion Calendaire :** Enregistrez la date et de l'heure de votre prochain rendez-vous thérapeutique.
*   **Rappel de Traitement Intelligent :** Configurez un rappel quotidien discret et bienveillant à l'heure souhaitée pour assurer la régularité de vos éventuels traitements ou rituels de soin.

### ⚙️ 4. Paramètres Personnels & Ergonomie Sensory-Friendly
*   **Interface Apaisante :** Un design fluide basé sur le Material Design 3, pensé pour limiter la fatigue visuelle et cognitive.
*   **Ajustements Sensoriels :** 
    *   Gestion globale des notifications et rappels d'heure.
    *   Activation/Désactivation des vibrations immersives.
    *   Activation/Désactivation de l'environnement sonore.
*   **Mode Sombre (Dark Mode) :** Un affichage nocturne optimal et enveloppant pour vos sessions d'introspection du soir.

---

## 🔒 Sécurité & Vie Privée (Privacy-First)

Nous croyons fermement que les données de santé mentale et les confidences écrites doivent rester strictement personnelles. 

*   **Stockage Décentralisé :** Votre journal de pensées, vos humeurs, vos notes de séances et vos calendriers de consultations ne transitent **jamais** sur un serveur distant. Ils sont stockés localement sur votre téléphone.
*   **Zéro Traceur (No Analytics) :** L'application n'intègre aucun tracker publicitaire, comportemental ou marketing. Aucune donnée d'utilisation n'est revendue à des tiers.
*   **Authentification Robuste :** L'authentification lors de la création de compte est déléguée aux systèmes hautement sécurisés de **Google Firebase Authentication** (adresse email & mot de passe chiffré).
*   **Messages Transactionnels Limités :** Un e-mail de bienvenue unique vous est envoyé lors de votre enregistrement sécurisé via l'API transactionnelle **Brevo**.

---

## 🛠️ Stack Technique

L'application est entièrement développée selon les meilleures pratiques du développement mobile Android moderne :

*   **Langage :** Kotlin (1.9+)
*   **UI Framework :** Jetpack Compose (Material Design 3 pour des composants fluides, modernes et adaptatifs)
*   **Architecture :** MVVM (Model-View-ViewModel) pour une séparation claire des responsabilités de code.
*   **Stockage Local :** `Room Database` (pour la persistance sécurisée locale) & `SharedPreferences` (pour les paramètres de préférences de l'utilisateur).
*   **Network (REST Client) :** Retrofit & OkHttp pour la communication ponctuelle (authentification et messages de bienvenue).
*   **Authentification Cloud :** Firebase Auth.
*   **Emails Transactionnels :** Brevo API intégrée nativement.

---

## 📂 Structure du Projet

```text
app/
├── src/main/java/com/example/
│   ├── api/             # Retrofit client, API interface, Brevo services et Email repository
│   ├── data/            # Datastructures, Models, Entities et DAO (Room Database)
│   ├── ui/               # Vues et Composables Jetpack Compose, ViewModel complet et thèmes graphiques
│   └── MainActivity.kt  # Point d'entrée de l'application Android
└── build.gradle.kts     # Configuration de build et dépendances Gradle
```

---

## 🚀 Installation & Développement

### Prérequis
*   Android Studio (Ladybug ou version ultérieure)
*   JDK 17 ou version supérieure
*   Un appareil Android ou émulateur sous Android 8.0+ (API level 26+)

### Cloner et exécuter le projet
1.  Cloner le dépôt sur votre machine :
    ```bash
    git clone https://github.com/votre-compte/norra-android.git
    ```
2.  Ouvrir le projet dans **Android Studio**.
3.  Synchroniser le projet avec vos fichiers Gradle (Gradle Sync).
4.  *(Optionnel)* Renseigner vos propres clés de services Firebase et clé d'API Brevo dans votre configuration d'environnement ou Secrets (au niveau build/BuildConfig) :
    *   `BREVO_API_KEY` : pour l'envoi d'emails transactionnels.
5.  Lancer l'application sur un terminal connecté ou émulateur.

---

## 📜 Licence & Confidentialité
Ce projet est distribué sous licence propriétaire. Pour toute modification ou utilisation tierce, veuillez contacter l'auteur. Les règles de traitement de vos données personnelles sont consignées de manière détaillée dans le fichier `PRIVACY_POLICY-fr.md`.

## ✉️ Contact & Support
Une remarque, une question de confidentialité ou besoin d'assistance ?
Contactez notre équipe de support à l'adresse suivante : **helpmenorra@gmail.com**

---
*Dévoué à votre bien-être quotidien, un souffle à la fois. 🌿*

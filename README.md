# Tower for Two

**Tower for Two** est une application Android privée pour deux adultes consentants, conçue pour accompagner une tour physique de 48 blocs (16 étages × 3 blocs).

L'application ne remplace pas la tour : elle suit simplement les nouveaux étages créés et révèle automatiquement une **Action** ou une **Vérité** à chaque bloc replacé.

## Règle de progression

- Tour initiale : 16 étages / 48 blocs
- Étage 17 → Niveau 1
- Étage 18 → Niveau 2
- …
- Étage 26 → Niveau 10
- Chaque niveau contient :
  - Bloc 1 → Action
  - Bloc 2 → Vérité 1
  - Bloc 3 → Vérité 2

Il suffit d'appuyer sur **BLOC POSÉ** après avoir replacé un bloc. Aucun numéro d'étage n'est saisi manuellement.

## Contenu

Le projet embarque **150 défis** :

- 10 niveaux
- 3 positions par niveau
- 5 variantes par position
- 60 variantes Sensuel
- 60 variantes Torride
- 30 variantes Très torride

Les variantes sont tirées aléatoirement et filtrées selon les préférences de la partie.

## Fonctionnalités

- Kotlin + Jetpack Compose + Material 3
- Architecture MVVM
- Thème sombre
- Joueurs personnalisables ou `Joueur 1` / `Joueur 2`
- Alternance automatique des joueurs
- Joker sans consommation de bloc ni changement de tour
- 10 indicateurs de progression
- Intensité : Sensuel / Torride / Très torride
- Filtre pour les défis avec retrait de vêtements
- Filtre pour les questions sur les fantasmes
- Défis personnalisés créables, modifiables et supprimables localement
- Sauvegarde automatique de la partie avec DataStore
- Reprise d'une partie interrompue
- Écran « La tour est tombée »
- Mode libre utilisant uniquement les niveaux déjà débloqués
- Aucun compte
- Aucun backend
- Aucune permission Internet

## Stockage local

- `DataStore Preferences` : état de partie + préférences
- `assets/challenges.json` : 150 défis intégrés
- `filesDir/custom_challenges.json` : défis créés par l'utilisateur

## Structure

```text
app/src/main/
├── assets/
│   └── challenges.json
├── java/com/crabscode/towerfortwo/
│   ├── data/
│   │   ├── ChallengeRepository.kt
│   │   └── PreferencesRepository.kt
│   ├── model/
│   │   └── Models.kt
│   ├── ui/
│   │   ├── TowerForTwoApp.kt
│   │   └── theme/Theme.kt
│   ├── viewmodel/
│   │   └── TowerViewModel.kt
│   └── MainActivity.kt
└── res/
```

## Build

Configuration du projet :

- Android Gradle Plugin 9.4.0
- Gradle 9.6.0
- Compose BOM 2026.09.00
- DataStore 1.2.1
- minSdk 26
- targetSdk 36
- Java 17

Ouvrir le dossier dans Android Studio puis laisser Gradle synchroniser les dépendances. Le workflow GitHub Actions `.github/workflows/android.yml` installe Gradle 9.6.0 directement, valide le catalogue, exécute les tests unitaires et assemble un APK debug.

> Note : le fichier binaire officiel `gradle-wrapper.jar` n'est pas généré par ce dépôt source. Si nécessaire, Android Studio ou une installation locale de Gradle peut régénérer le wrapper avec `gradle wrapper --gradle-version 9.6.0`.

## Vie privée

Le manifeste ne déclare pas la permission `android.permission.INTERNET`. Les données du jeu restent locales à l'appareil.

## Statut

Version initiale : `1.0.0`.

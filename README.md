# PNSA — application mobile Android / iPhone

Application Kotlin Multiplatform (Compose Multiplatform) du **Programme National de Santé de l’Adolescent**. Elle permet aux jeunes de consulter le catalogue SSR, de faire des quiz, de chercher une structure, de suivre une orientation, et d’accéder au forum et au conseil privé.

L’interface est partagée entre Android et iOS. Un module `desktopApp` sert de prévisualisation téléphone sur ordinateur.

## Modules

| Module | Rôle |
| --- | --- |
| `shared` | UI Compose, navigation, thème, réseau, session, fonctionnalités |
| `androidApp` | Point d’entrée Android |
| `iosApp` | Point d’entrée iOS (SwiftUI + `MainViewController`) |
| `desktopApp` | Fenêtre de prévisualisation 390×844 |

## Architecture

Le code partagé est organisé par **fonctionnalité** (`features/*`) et par **cœur** (`core/*`) :

```
shared/src/commonMain/kotlin/app/partners/pnsa/
  core/           config, réseau, session, cache, thème, composants, navigation
  features/
    auth/         connexion, inscription, consentements
    home/         accueil et raccourcis
    content/      catalogue Apprendre
    quiz/         quiz versionnés + reprise
    structure/    annuaire + orientations
    forum/        forum public + conseil privé
    user/         profil, notifications, aide
```

Chaque fonctionnalité expose `data` (API), `domain` (modèles) et `ui` (écrans). L’API mobile documentée (`/api/mobile/v1/*` + Sanctum) est le contrat unique : pas de second catalogue local.

## API

Racine configurée dans `AppConfig` :

`https://uxfqst-ip-167-86-108-98.tunnelmole.net/api`

- Authentification : `POST /auth/login` et `/auth/register`, puis `Authorization: Bearer {token}`
- Catalogue et annuaire réservés aux comptes connectés
- Sync différentielle : `GET /mobile/v1/sync/catalog` et `/sync/structures`
- Quiz : `client_attempt_id` (UUID) pour l’idempotence
- Orientations : `client_request_id` (UUID)

## Lancer

- Android : `./gradlew :androidApp:assembleDebug`
- Desktop (prévisualisation) : `./gradlew :desktopApp:run`
- Tests partagés : `./gradlew :shared:jvmTest` ou `:shared:testAndroidHostTest`
- iOS : ouvrir `iosApp` dans Xcode

## Parcours couverts

Accès (E01), profil et consentements (E02), accueil (E03), contenus (E04), quiz avec reprise (E05), forum (E06), conseil privé (E07), structures (E08), orientations (E09), notifications (E10), aide (E11). Les états chargement / vide / erreur / hors-ligne sont prévus sur les listes principales.

## Qualité

- Secrets : le jeton Sanctum est stocké dans les préférences de la plateforme, jamais dans les journaux d’écran
- HTTPS uniquement
- Textes d’erreur compréhensibles, sans révéler l’existence d’un compte à un tiers
- Ce service n’est pas une urgence médicale

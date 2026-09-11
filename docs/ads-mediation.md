# Ad mediation architecture

The app exposes typed ads through `Ads.showAd(zoneId, type)`. The game UI and XO engine do not select a provider. `AdManager` reads the locally persisted provider selector and resolves one of these strategies:

- `TapsellStrategy`
- `AdiveryStrategy`
- `MixedAdStrategy` (Tapsell first, then Adivery on any non-success)

Supported ad formats are `BANNER`, `INTERSTITIAL`, `NATIVE`, and `REWARDED`. Banner rendering is integrated in the game screen, and an interstitial is requested every five completed rounds. Native is represented by a safe gateway stub until a native UI is added.

The SDK-specific code is isolated in `SdkAdGateways.kt`. Network callbacks are bridged to suspend functions, expected no-fill/network failures become `AdResult.Failed`, and ad calls are launched from an owned coroutine scope. SDK objects are released through `AdManager.close()` and no Activity is retained by the manager; the factory uses a weak reference to the currently resumed Activity.

## Local configuration

Do not commit credentials or local placement configuration. Add these keys to local `local.properties` or CI Gradle properties:

```properties
AD_PROVIDER=mixed
TAPSELL_APP_ID=your-tapsell-app-id
TAPSELL_BANNER_PLACEMENT=your-tapsell-banner-placement
TAPSELL_INTERSTITIAL_PLACEMENT=your-tapsell-interstitial-placement
TAPSELL_NATIVE_PLACEMENT=your-tapsell-native-placement
TAPSELL_REWARDED_PLACEMENT=your-tapsell-rewarded-placement
ADIVERY_APP_ID=your-adivery-app-id
ADIVERY_BANNER_PLACEMENT=your-adivery-banner-placement
ADIVERY_INTERSTITIAL_PLACEMENT=your-adivery-interstitial-placement
ADIVERY_NATIVE_PLACEMENT=your-adivery-native-placement
ADIVERY_REWARDED_PLACEMENT=your-adivery-rewarded-placement
AD_REMOTE_CONFIG_URL=https://your-backend.example/ad-provider
SENTRY_DSN=https://public-dsn@example.ingest.sentry.io/project
```

`AD_REMOTE_CONFIG_URL` must return exactly `tapsell`, `adivery`, or `mixed` as a UTF-8 response. Empty URLs, timeouts, network failures, non-success responses, and invalid values are persisted as the strict fallback `mixed`.

The Tapsell adapter uses its official request/show callback APIs for rewarded and interstitial ads. The Adivery adapter follows the documented prepare/isLoaded/show flow for rewarded and interstitial ads. Verify current provider SDK terms, consent requirements, and placement APIs before releasing.

## Privacy and observability

- `INTERNET` is required by ad SDKs.
- Sentry auto-initialization is disabled in the manifest; it is initialized manually only when `SENTRY_DSN` is non-empty.
- A missing or malformed DSN is ignored so observability configuration can never prevent the game from starting.
- `sendDefaultPii` is explicitly `false`.
- Ad no-fill, timeout, and ordinary network failures are returned as results and recorded only as breadcrumbs; they are not captured as Sentry exceptions.
- Breadcrumbs use generic provider flow messages and never include placement IDs, app IDs, response IDs, user identifiers, URLs, or ad payloads.
- Provider SDKs may have their own data processing behavior. Review their privacy disclosures and configure consent/data flags separately before distribution.

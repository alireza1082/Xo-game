# Ad mediation architecture

The app exposes ads through `Ads.showAd(zoneId)`; game UI and the XO engine do not select a provider. `AdManager` reads the locally persisted provider selector and resolves one of these strategies:

- `TapsellStrategy`
- `AdiveryStrategy`
- `MixedAdStrategy` (Tapsell first, then Adivery on any non-success)

The SDK-specific code is isolated in `SdkAdGateways.kt`. Network callbacks are bridged to suspend functions, expected no-fill/network failures become `AdResult.Failed`, and ad calls are launched from an owned coroutine scope. SDK objects are released through `AdManager.close()` and no Activity is retained by the manager; the factory uses a weak reference to the currently resumed Activity.

## Local configuration

Do not commit credentials. Add these keys to local `local.properties` or CI Gradle properties:

```properties
AD_PROVIDER=mixed
TAPSELL_APP_ID=your-tapsell-app-id
TAPSELL_REWARDED_PLACEMENT=your-tapsell-rewarded-placement
ADIVERY_APP_ID=your-adivery-app-id
ADIVERY_REWARDED_PLACEMENT=your-adivery-rewarded-placement
AD_REMOTE_CONFIG_URL=https://your-backend.example/ad-provider
SENTRY_DSN=https://public-dsn@example.ingest.sentry.io/project
```

`AD_REMOTE_CONFIG_URL` must return exactly `tapsell`, `adivery`, or `mixed` as a UTF-8 response. Empty URLs, timeouts, network failures, non-success responses, and invalid values are persisted as the strict fallback `mixed`.

The app currently uses rewarded placements. The Tapsell adapter follows the official Tapsell Plus request/show callback API. The Adivery adapter follows the documented prepare/isLoaded/show flow. Verify current provider SDK terms, consent requirements, and placement APIs before releasing.

## Privacy and observability

- `INTERNET` is required by ad SDKs.
- Sentry auto-initialization is disabled in the manifest; it is initialized manually only when `SENTRY_DSN` is non-empty.
- A missing or malformed DSN is ignored so observability configuration can never prevent the game from starting.
- `sendDefaultPii` is explicitly `false`.
- Ad no-fill, timeout, and ordinary network failures are returned as results and recorded only as breadcrumbs; they are not captured as Sentry exceptions.
- Breadcrumbs use generic provider flow messages and never include placement IDs, app IDs, response IDs, user identifiers, URLs, or ad payloads.
- Provider SDKs may have their own data processing behavior. Review their privacy disclosures and configure consent/data flags separately before distribution.

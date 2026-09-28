# Deployment Guide: Trotro Rush
**Document ID:** TUC-DOC-DEP-2026-001  
**Institution:** Techbridge University College (TUC), Oyibi, Ghana  
**Owner:** Daniel Twum, Head of ICT  

---

## 1. Hosting Architecture
Trotro Rush is built with modular flexibility:
- **Android Target:** Native Android build powered by Jetpack Compose and Kotlin DSL.
- **Web SPA Target:** React/TypeScript PWA served statically under `/trotro-rush/` using nginx static-SPA caching (no backend server process required).

## 2. Android Build Commands
```bash
# Debug Build
gradle assembleDebug

# Run Unit & Robolectric Tests
gradle :app:testDebugUnitTest

# Release Bundle (AAB for Google Play)
gradle bundleRelease
```

## 3. Web & Static Deployment
- Root path: `/trotro-rush/`
- Zero server process requirement (no pm2 process or port allocation needed).
- Full offline caching through service workers.

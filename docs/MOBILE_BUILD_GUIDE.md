# Mobile Build Workflow & Debugging Guide
**Document ID:** TUC-DOC-BLD-2026-001  
**Project:** Trotro Rush  
**Institution:** Techbridge University College (TUC), Oyibi, Ghana  
**Engineer:** Daniel Twum, Head of ICT  

---

## 1. Native Android Build Workflow
```bash
# Clean compilation test
gradle assembleDebug

# Run Unit and Robolectric Tests
gradle :app:testDebugUnitTest

# Assemble Release APK
gradle assembleRelease

# Generate Android App Bundle for Google Play
gradle bundleRelease
```

## 2. Capacitor Hybrid Packaging (Pattern 3)
```bash
# 1. Install Capacitor dependencies
pnpm add @capacitor/core @capacitor/cli @capacitor/ios @capacitor/android

# 2. Initialise with app identity
npx cap init "Trotro Rush" "com.techbridge.trotrorush" --web-dir "dist"

# 3. Add mobile platforms
npx cap add android
npx cap add ios

# 4. Sync web assets
pnpm build
npx cap sync
```

## 3. Common Errors and Resolutions
- **Issue:** AAPT2 error `attribute android:strokeDasharray not found`.
  - **Fix:** Android Vector Drawables do not support `strokeDasharray`. Use solid paths or custom canvas line drawing.
- **Issue:** Google Services plugin missing `google-services.json`.
  - **Fix:** Keep `missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN` in Gradle when no Firebase runtime is needed.
- **Issue:** AAPT2 duplicate resource conflicts.
  - **Fix:** Ensure only `.png` or vector `.xml` icons are present; remove conflicting `.webp` templates.

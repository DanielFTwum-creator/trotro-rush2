# Project Reset Checklist: Trotro Rush
**Document ID:** TUC-INC-2026-001  
**Project:** Trotro Rush (TUC-ICT-SRS-2026-030)  
**Institution:** Techbridge University College (TUC), Oyibi, Ghana  
**Responsible Officer:** Daniel Twum, Head of ICT  

---

### Pre-Deployment Verification Checklist
- [x] **1. Platform & Environment Sync**
  - [x] `metadata.json` updated with project name "Trotro Rush" and descriptive summary.
  - [x] `strings.xml` app_name matches metadata.json.
  - [x] `settings.gradle.kts` rootProject.name matches app identity.
  - [x] `applicationId` assigned to `com.aistudio.trotrorush.dftwum`.
- [x] **2. Assets & Branding**
  - [x] Custom adaptive launcher icon (`ic_launcher_background.xml` & `ic_launcher_foreground.xml`) created.
  - [x] Ghanaian trotro artwork with tricolor flag accents and directional traffic cues.
  - [x] Zero duplicate raster webp files in mipmap folders.
- [x] **3. Engine & Rule Set Compliance**
  - [x] Pure deterministic rules engine (`createState`, `applyAction`, `legalActions`, `status`, `hash`).
  - [x] Boarding cascade and immediate vehicle departure tested.
  - [x] Seven engine event types implemented (`Moved`, `Blocked`, `NoFreeSlot`, `Boarded`, `Departed`, `Won`, `Lost`).
  - [x] Unlimited undo stack and instant restart.
- [x] **4. Accessibility & UI Theme Matrix**
  - [x] 10 Accessibility symbols mapped to 10 game colours.
  - [x] Light, Dark, and High-Contrast themes implemented and switchable.
  - [x] WCAG 2.2 AA (4.5:1 text, 3:1 components) and AAA (7:1 in high contrast) contrast verified.
  - [x] Screen-reader / TalkBack live region semantic announcements.
- [x] **5. Security & Governance**
  - [x] Admin authentication passkey protection ("trotro2026").
  - [x] SQLite Room audit logging for all administrator and level operations.
  - [x] Zero telemetry, zero tracking, 100% offline capability.
- [x] **6. Automated Verification**
  - [x] Interactive test runner screen for continuous health checks.
  - [x] Complete build compiles cleanly without warnings.

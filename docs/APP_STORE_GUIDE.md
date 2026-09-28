# App Store & Google Play Submission Guide: Trotro Rush
**Document ID:** TUC-DOC-STORE-2026-001  
**Project:** Trotro Rush (TUC-ICT-SRS-2026-030)  
**App ID:** `com.techbridge.trotrorush` / `com.aistudio.trotrorush.dftwum`  
**Institution:** Techbridge University College (TUC), Oyibi, Ghana  
**Engineer:** Daniel Twum, Head of ICT  

---

## 1. Account Setup & Prerequisites
- **Apple Developer Account:** Enrolled under Techbridge University College organization ($99/year).
- **Google Play Console Account:** Registered organization account ($25 one-time fee).
- **DUNS Number:** Verified for Techbridge University College, Oyibi, Ghana.

## 2. Store Record Creation & Metadata
- **App Name:** `Trotro Rush` (Short, catchy, exactly under 30 characters for Play Store policy).
- **Subtitle / Short Description:** `Ghanaian Trotro Traffic Puzzle`
- **Full Description:**
  ```
  Step into the bustling streets of Accra and manage the traffic jam at the station! 
  Trotro Rush is an authentic Ghanaian colour-matching traffic-jam puzzle game. 
  Navigate jammed trotros, cars, and buses carrying famous slogans like "Nyame Bekyere" and "Sea Never Dry". 
  Send vehicles to parking bays where waiting passengers automatically board matching colours. 
  Features 40 challenging hand-crafted levels, dual colour-symbol coding for full colourblind accessibility, 
  three vibrant themes (Light, Dark, High-Contrast), offline play with zero ads, and zero data tracking.
  ```
- **Category:** Games > Puzzle / Casual.
- **Content Rating:** PEGI 3 / Everyone (No violence, no gambling, no in-app purchases).
- **Privacy Policy URL:** `https://ai-tools.techbridge.edu.gh/trotro-rush/privacy.html`

## 3. Required Screenshots
- **iOS:** 6.7" (iPhone 15 Pro Max: 1290 x 2796 px) and 6.5" displays.
- **Android:** Minimum 4 phone screenshots (1080 x 2400 px), 7" tablet, and 10" tablet.
- **Feature Graphic:** 1024 x 500 px banner highlighting trotro rush station puzzle.

## 4. Build Upload & Release Track
1. Generate signed Android App Bundle (AAB):
   ```bash
   gradle bundleRelease
   ```
2. Upload to Google Play Console Internal Testing track -> Promote to Production.
3. For iOS: Build archive in Xcode, validate with Transporter, submit for App Review.

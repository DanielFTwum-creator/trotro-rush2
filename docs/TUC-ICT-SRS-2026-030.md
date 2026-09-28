# TECHBRIDGE UNIVERSITY COLLEGE (TUC)
## Faculty of Computing and Information Systems
### Directorate of ICT Services — Oyibi Campus, Accra, Ghana

---

```
Document Ref : TUC-ICT-SRS-2026-030
Document Type: Software Requirements Specification (SRS)
Standard     : IEEE 830-1998 / ISO/IEC/IEEE 29148:2018 (UK English)
System       : Trotro Rush Mobile Transit Puzzle Game
Version      : 1.0.0 (Release Candidate)
Date         : September 2026
Owner        : Daniel Twum, Head of ICT & Special Adviser to the Founder
Classification: Public / Academic Reference Implementation
```

---

## Table of Contents
1. [Introduction](#1-introduction)
   1.1 [Purpose](#11-purpose)
   1.2 [Document Conventions](#12-document-conventions)
   1.3 [Intended Audience and Reading Suggestions](#13-intended-audience-and-reading-suggestions)
   1.4 [Project Scope](#14-project-scope)
   1.5 [References](#15-references)
2. [Overall Description](#2-overall-description)
   2.1 [Product Perspective](#21-product-perspective)
   2.2 [Product Functions](#22-product-functions)
   2.3 [User Classes and Characteristics](#23-user-classes-and-characteristics)
   2.4 [Operating Environment](#24-operating-environment)
   2.5 [Design and Implementation Constraints](#25-design-and-implementation-constraints)
   2.6 [Assumptions and Dependencies](#26-assumptions-and-dependencies)
3. [External Interface Requirements](#3-external-interface-requirements)
   3.1 [User Interfaces](#31-user-interfaces)
   3.2 [Hardware Interfaces](#32-hardware-interfaces)
   3.3 [Software Interfaces](#33-software-interfaces)
   3.4 [Communications Interfaces](#34-communications-interfaces)
4. [System Features & Functional Requirements](#4-system-features--functional-requirements)
   4.1 [Pure Deterministic Rules Engine (REQ-ENG)](#41-pure-deterministic-rules-engine-req-eng)
   4.2 [Car Park Grid & Movement Mechanics (REQ-PLAY)](#42-car-park-grid--movement-mechanics-req-play)
   4.3 [Passenger Queue & Boarding Dynamics (REQ-QUEUE)](#43-passenger-queue--boarding-dynamics-req-queue)
   4.4 [Parking Bay Management & Departures (REQ-SLOT)](#44-parking-bay-management--departures-req-slot)
   4.5 [BFS Solvability Engine & Par Calculation (REQ-SOLV)](#45-bfs-solvability-engine--par-calculation-req-solv)
   4.6 [Station Levels & Ghanaian Transit Progression (REQ-LVL)](#46-station-levels--ghanaian-transit-progression-req-lvl)
   4.7 [Administrative Security & Passkey Authentication (REQ-SEC)](#47-administrative-security--passkey-authentication-req-sec)
   4.8 [Audit Logging & Compliance (REQ-AUD)](#48-audit-logging--compliance-req-aud)
   4.9 [Accessibility & Tri-Theme Engine (REQ-A11Y)](#49-accessibility--tri-theme-engine-req-a11y)
5. [Non-Functional & Quality Requirements](#5-non-functional--quality-requirements)
   5.1 [Performance Requirements](#51-performance-requirements)
   5.2 [Safety and Privacy Requirements](#52-safety-and-privacy-requirements)
   5.3 [Security Requirements](#53-security-requirements)
   5.4 [Software Quality Attributes](#54-software-quality-attributes)
6. [Acceptance Criteria & Traceability Matrix](#6-acceptance-criteria--traceability-matrix)

---

## 1. Introduction

### 1.1 Purpose
This Software Requirements Specification (SRS) delineates the complete functional, behavioural, structural, and performance requirements for **Trotro Rush**, a mobile transit puzzle game developed by the Directorate of ICT Services at Techbridge University College (TUC), Oyibi, Ghana. The document conforms strictly to IEEE 830-1998 and ISO/IEC/IEEE 29148:2018 standards and serves as the authoritative technical baseline for engineering, auditing, quality assurance, and deployment across Android and cross-platform runtimes.

### 1.2 Document Conventions
- This specification is authored in UK English.
- Requirements are uniquely identified using hierarchical alphanumeric codes:
  - `REQ-ENG-xxx`: Core Rules Engine requirements
  - `REQ-PLAY-xxx`: Gameplay & grid movement requirements
  - `REQ-QUEUE-xxx`: Passenger queue and boarding cascade requirements
  - `REQ-SLOT-xxx`: Parking bay capacity and departure requirements
  - `REQ-SOLV-xxx`: Solvability and BFS optimal path solver requirements
  - `REQ-LVL-xxx`: Level progression and transit corridor requirements
  - `REQ-SEC-xxx`: Administrative security and passkey requirements
  - `REQ-AUD-xxx`: Audit logging and governance requirements
  - `REQ-A11Y-xxx`: Accessibility, theming, and contrast requirements
- Priority ratings conform to RFC 2119: **MANDATORY** (SHALL), **RECOMMENDED** (SHOULD), **OPTIONAL** (MAY).

### 1.3 Intended Audience and Reading Suggestions
This document is targeted at:
- **Director of ICT & Academic Leadership**: Validation of institutional alignment and compliance.
- **Lead Software Engineers**: Architecture implementation, rules engine invariants, and state management.
- **QA & Testing Engineers**: Verification against formal Acceptance Tests (AT-01 to AT-30).
- **Security & Privacy Auditors**: Confirmation of zero telemetry, local persistence, and access controls.

### 1.4 Project Scope
*Trotro Rush* is an authentic, Ghanaian-themed colour-matching traffic-jam puzzle application. Set against the backdrop of Greater Accra's high-congestion transit corridors (Tema Station, Circle, Kaneshie, Madina, Lapaz, and Adenta), the game challenges players to untangle gridlocked lorry stations by guiding commercial vehicles (*trotros*, *207 Sprinters*, and *large buses*) out of blocked parking bays along their directional axes.

The system is designed with strict educational and architectural integrity:
- **100% Offline Capability**: Zero reliance on cloud servers, external analytics, or telemetric harvesting.
- **Zero In-App Purchases & Zero Advertisements**: Completely free of monetization friction.
- **Pure Deterministic Simulation**: Engine transitions are mathematical state transformations with zero side effects.
- **Universal Accessibility**: Compliant with WCAG 2.2 Level AA / AAA, offering dual colour-symbol coding and multi-modal sensory feedback.

### 1.5 References
1. IEEE Std 830-1998: *IEEE Recommended Practice for Software Requirements Specifications*.
2. ISO/IEC/IEEE 29148:2018: *Systems and Software Engineering — Life Cycle Processes — Requirements Engineering*.
3. W3C Web Content Accessibility Guidelines (WCAG) 2.2 (Recommendation October 2023).
4. Google Material Design 3 (M3) Specification.
5. Android Architecture Guidelines (MAD: Modern Android Development).

---

## 2. Overall Description

### 2.1 Product Perspective
Trotro Rush operates as a self-contained, standalone mobile application. It persists state, statistics, settings, and governance audit records locally using an SQLite relational database via Room ORM. The architecture follows Clean Architecture and MVVM patterns:
```
┌────────────────────────────────────────────────────────┐
│                   UI Shell (Jetpack Compose)           │
│  PlayScreen  ·  LevelSelect  ·  AdminDashboard  ·  HUD │
└───────────────────────────┬────────────────────────────┘
                            │ StateFlow / Events
┌───────────────────────────▼────────────────────────────┐
│                    Domain & Engine Layer               │
│   RulesEngine (Pure) · BFSSolver · HintSystem          │
└───────────────────────────┬────────────────────────────┘
                            │ Repositories / DAO
┌───────────────────────────▼────────────────────────────┐
│                  Local Persistence Layer               │
│         Room SQLite · SharedPreferences / DataStore    │
└────────────────────────────────────────────────────────┘
```

### 2.2 Product Functions
1. **Grid Jam Simulation**: Render rectangular grids ($6\times 6$) containing commercial vehicles with fixed orientations ($0^\circ, 90^\circ, 180^\circ, 270^\circ$).
2. **Deterministic Vehicle Clearance**: Verify unblocked straight-line clearance from vehicle front to the yard perimeter before permitting exit.
3. **Queue-Driven Cascading Boarding**: Match colour of the lead passenger at the gate to parked vehicles in loading bays.
4. **Bay Overflow Detection**: Track slot occupancy (4 to 7 bays) and signal immediate loss if full gridlock occurs.
5. **Shortest-Path BFS Guidance**: Real-time evaluation of winning move sequences with optimal hint suggestions.
6. **Administrative Governance**: Restrict level resets and audit log export behind SHA-256 salted credentials.
7. **Accessibility & Tri-Theming**: Instant runtime switching between Light, Dark, and High-Contrast modes.

### 2.3 User Classes and Characteristics
- **Casual & Student Players**: Demand intuitive tap-to-move mechanics, zero tutorial friction, responsive haptic feedback, and vibrant local cultural references.
- **Neurodivergent & Low-Vision Players**: Require high-contrast visual cues, dual symbol coding (distinguishing colours via shapes), and TalkBack screen reader support.
- **Academic Station Masters / Administrators**: Require access to audit logs, level completion metrics, and tamper-evident game state resets.

### 2.4 Operating Environment
- **Host OS**: Android 8.0 (API Level 26) through Android 15 (API Level 35).
- **Cross-Platform Baseline**: Capacitor 8.3.3 compatible web shell (TypeScript, React, Tailwind CSS).
- **Hardware Targets**: Handheld smartphones (Compact), Foldables (Medium), and Tablets (Expanded).
- **Display Densities**: mdpi through xxxhdpi; minimum resolution $360\times 640$ dp.

### 2.5 Design and Implementation Constraints
- **Zero Network Traffic**: Application SHALL NOT open network sockets or request `INTERNET` permission during core gameplay.
- **Pure Memory Footprint**: Maximum heap consumption SHALL remain under 128 MB during level solving.
- **Frame Budget**: Touch event to visual update latency SHALL NOT exceed 16.6 ms (stable 60 fps).
- **Binary Size**: Shipping APK release artifact SHALL NOT exceed 25 MB.

### 2.6 Assumptions and Dependencies
- Device supports OpenGL ES 3.0 or Vulkan for Compose canvas hardware acceleration.
- Android system vibrator service is available for sensory haptic feedback.
- SQLite 3.24+ runtime bundled in the Android operating system.

---

## 3. External Interface Requirements

### 3.1 User Interfaces
- **Visual Design**: Strict adherence to Material Design 3 spacing (8dp grid), dynamic theming, and responsive layouts.
- **Touch Bounds**: All interactive affordances (buttons, vehicle tiles, filter chips) SHALL maintain a minimum hit box of $48\times 48$ dp.
- **Colour & Symbol Matrix**:
  | Game Colour | Hex Light | Hex Dark | High Contrast | Universal Symbol | TalkBack Token |
  |---|---|---|---|:---:|---|
  | **RED** | `#EF4444` | `#DC2626` | `#FF3B30` | ⭕ Circle | "Red Circle" |
  | **BLUE** | `#3B82F6` | `#2563EB` | `#007AFF` | ⬛ Square | "Blue Square" |
  | **YELLOW** | `#EAB308` | `#CA8A04` | `#FFCC00` | 🔺 Triangle | "Yellow Triangle" |
  | **GREEN** | `#10B981` | `#059669` | `#34C759` | 🔷 Diamond | "Green Diamond" |
  | **PURPLE** | `#8B5CF6` | `#7C3AED` | `#AF52DE` | ⭐ Star | "Purple Star" |
  | **ORANGE** | `#F97316` | `#EA580C` | `#FF9500` | ⬡ Hexagon | "Orange Hexagon" |
  | **PINK** | `#EC4899` | `#DB2777` | `#FF2D55` | 💖 Heart | "Pink Heart" |
  | **CYAN** | `#06B6D4` | `#0891B2` | `#5AC8FA` | ✝️ Cross | "Cyan Cross" |
  | **BROWN** | `#92400E` | `#78350F` | `#A2845E` | 🌙 Crescent | "Brown Crescent" |
  | **TEAL** | `#0D9488` | `#0F766E` | `#00C7BE` | 🔘 Ring | "Teal Ring" |

### 3.2 Hardware Interfaces
- **Audio Output**: Synthesized sound effects (engine rev, horn honk, passenger alighting, applause) executed via low-latency audio stream.
- **Haptic Actuator**: Haptic vibration triggers (`VibrationEffect.createPredefined(EFFECT_CLICK)` and `EFFECT_HEAVY_CLICK`).

### 3.3 Software Interfaces
- **Room ORM (Android Jetpack)**: Local data access objects for `PlayerProgress`, `LevelMetadata`, and `AuditLog`.
- **TalkBack Screen Reader**: Accessibility traversal via Jetpack Compose semantics tree (`LiveRegionMode.Polite`).

### 3.4 Communications Interfaces
- None (Self-contained offline application).

---

## 4. System Features & Functional Requirements

### 4.1 Pure Deterministic Rules Engine (REQ-ENG)
- **REQ-ENG-001**: The system SHALL represent game state as an immutable data class (`GameState`) comprising `gridSize`, `carPark`, `slots`, `passengerQueue`, `movesCount`, and `status`.
- **REQ-ENG-002**: State transitions SHALL be executed through a single pure function `RulesEngine.applyAction(currentState, vehicleId) -> Pair<GameState, List<EngineEvent>>` with zero external mutation.
- **REQ-ENG-003**: The engine SHALL emit exactly seven discrete event types:
  1. `Moved(vehicleId, slotIndex)`
  2. `Blocked(vehicleId, reason)`
  3. `NoFreeSlot(vehicleId)`
  4. `Boarded(passengerId, vehicleId, seatIndex)`
  5. `Departed(vehicleId, passengers)`
  6. `Won(moves, stars, score)`
  7. `Lost(reason)`
- **REQ-ENG-004**: The engine SHALL provide `RulesEngine.legalActions(state): List<String>` computing all vehicle IDs capable of legally exiting the car park at that state instant.
- **REQ-ENG-005**: The engine SHALL provide state hashing `RulesEngine.hash(state): String` for duplicate state detection during search and undo stacks.

### 4.2 Car Park Grid & Movement Mechanics (REQ-PLAY)
- **REQ-PLAY-001**: Grid coordinates SHALL be 0-indexed $(row, col)$ starting from top-left $(0,0)$.
- **REQ-PLAY-002**: Vehicle types SHALL determine physical bounding dimensions:
  - `CAR`: Length 2 cells, 2 passenger seats.
  - `MINIBUS` (Trotro): Length 3 cells, 3 passenger seats.
  - `BUS`: Length 4 cells, 4 passenger seats.
- **REQ-PLAY-003**: Vehicle orientations SHALL be strictly cardinal:
  - `UP`: Forward clearance is checked along $row - 1 \to 0$ in column $col$.
  - `DOWN`: Forward clearance is checked along $row + length \to gridSize - 1$ in column $col$.
  - `LEFT`: Forward clearance is checked along $col - 1 \to 0$ in row $row$.
  - `RIGHT`: Forward clearance is checked along $col + length \to gridSize - 1$ in row $row$.
- **REQ-PLAY-004**: If the forward path contains any cell occupied by another vehicle, the tap SHALL be rejected with an `EngineEvent.Blocked` event, triggering an auditory screech and visual bump animation.
- **REQ-PLAY-005**: If all forward cells are vacant, the vehicle SHALL be removed from the car park grid and assigned to the leftmost unoccupied parking slot.
- **REQ-PLAY-006**: The system SHALL support an unlimited undo stack (`UndoManager`), permitting players to revert moves without penalty.

### 4.3 Passenger Queue & Boarding Dynamics (REQ-QUEUE)
- **REQ-QUEUE-001**: Passengers SHALL wait in a single-file FIFO queue at the station gate.
- **REQ-QUEUE-002**: Boarding SHALL only evaluate the passenger at queue position index 0 (the Gate position).
- **REQ-QUEUE-003**: If any vehicle currently residing in a parking slot matches the colour of the lead passenger and has at least one vacant seat, the passenger SHALL immediately board that vehicle.
- **REQ-QUEUE-004**: Boarding SHALL cascade automatically: as soon as passenger index 0 boards, passenger index 1 shifts to index 0 and is immediately evaluated against all parked vehicles.
- **REQ-QUEUE-005**: Total seats across all vehicles in a level SHALL strictly equal the total number of waiting queue passengers ($\sum \text{Seats} \equiv \text{Count}(\text{Passengers})$).

### 4.4 Parking Bay Management & Departures (REQ-SLOT)
- **REQ-SLOT-001**: Each level SHALL define between 4 and 7 parking bays (`slotCount`).
- **REQ-SLOT-002**: When all seats in a parked vehicle are occupied, the vehicle SHALL immediately emit `EngineEvent.Departed` and vacate its parking slot.
- **REQ-SLOT-003**: Vacated parking slots SHALL immediately become available for incoming vehicles or slot compaction.
- **REQ-SLOT-004**: If an exiting vehicle finds zero free parking bays, the move SHALL fail with `EngineEvent.NoFreeSlot`.
- **REQ-SLOT-005**: If all parking bays are occupied and the front passenger cannot board any parked vehicle, the engine SHALL declare game over with `EngineEvent.Lost(GRIDLOCK)`.

### 4.5 BFS Solvability Engine & Par Calculation (REQ-SOLV)
- **REQ-SOLV-001**: Every published level SHALL be verified by an automated Breadth-First Search (BFS) solver to mathematically guarantee solvability.
- **REQ-SOLV-002**: The BFS solver SHALL calculate the minimal move count ($Par$) required to clear all vehicles and passengers.
- **REQ-SOLV-003**: Star ratings SHALL be awarded deterministically based on $Par$:
  - 3 Stars: $\text{Moves} \le Par$
  - 2 Stars: $\text{Moves} \le Par + 2$
  - 1 Star: Successful completion with $\text{Moves} > Par + 2$
- **REQ-SOLV-004**: An in-game Hint Engine SHALL execute BFS from the current active game state to suggest the next optimal vehicle to move.

### 4.6 Station Levels & Ghanaian Transit Progression (REQ-LVL)
- **REQ-LVL-001**: The game SHALL include 40 handcrafted levels across 4 real Greater Accra transit corridors:
  - Corridor 1: *Accra Central & Ring Road* (Levels 1–10, Beginner)
  - Corridor 2: *Liberation Road & Legon Arterial* (Levels 11–20, Intermediate)
  - Corridor 3: *N1 George Walker Bush Highway* (Levels 21–30, Advanced)
  - Corridor 4: *Kaneshie & Winneba Coastal Route* (Levels 31–40, Rush Hour)
- **REQ-LVL-002**: Each vehicle SHALL feature authentic Ghanaian trotro slogans ("Nyame Nhyira", "Sea Never Dry", "All Shall Pass", "Obi Nya W'aye").
- **REQ-LVL-003**: The station conductor ("Mate") SHALL provide vernacular call-outs for each stage ("Madina! Madina!", "Kaneshie straight!").

### 4.7 Administrative Security & Passkey Authentication (REQ-SEC)
- **REQ-SEC-001**: Administrative functions SHALL be guarded behind passkey authentication ("trotro2026").
- **REQ-SEC-002**: Password verification SHALL execute in constant time or via SHA-256 hash comparison to resist timing side-channel attacks.
- **REQ-SEC-003**: Three consecutive failed authentication attempts SHALL lock the admin console for 60 seconds.

### 4.8 Audit Logging & Compliance (REQ-AUD)
- **REQ-AUD-001**: An immutable SQLite audit log table (`audit_logs`) SHALL record all administrative events:
  - Timestamp (ISO 8601 UTC)
  - Event Type (`ADMIN_LOGIN`, `PROGRESS_RESET`, `LEVEL_UNLOCKED`, `SETTINGS_CHANGED`)
  - User ID / Principal ("Head of ICT")
  - Outcome Status (`SUCCESS`, `DENIED`)
- **REQ-AUD-002**: The system SHALL provide an administrative view displaying audit records with CSV export capabilities.

### 4.9 Accessibility & Tri-Theme Engine (REQ-A11Y)
- **REQ-A11Y-001**: The application SHALL support Light, Dark, and High-Contrast display themes stored in persistent preferences.
- **REQ-A11Y-002**: In High-Contrast mode, background SHALL be pure black (`#000000`), text pure white (`#FFFFFF`), and vehicles delineated with solid 2.5dp high-visibility borders.
- **REQ-A11Y-003**: Every game colour SHALL be permanently paired with a universal geometric symbol to ensure complete playability for individuals with protanopia, deuteranopia, or tritanopia.
- **REQ-A11Y-004**: All state updates SHALL announce contextual information to screen readers via a polite `LiveRegion`.

---

## 5. Non-Functional & Quality Requirements

### 5.1 Performance Requirements
- **NFR-PERF-001**: Cold start to interactive main menu SHALL take less than 1,200 ms on a standard mid-range device.
- **NFR-PERF-002**: Frame rendering rate SHALL maintain $\ge 58$ fps during complex cascading board animations.
- **NFR-PERF-003**: State transition compute time for `applyAction` SHALL NOT exceed 2.0 ms.

### 5.2 Safety and Privacy Requirements
- **NFR-PRIV-001**: The application SHALL NOT collect, store, transmit, or share personal identifiable information (PII).
- **NFR-PRIV-002**: The application SHALL NOT request runtime dangerous permissions (Camera, Location, Contacts, Microphone).

### 5.3 Security Requirements
- **NFR-SEC-001**: SQLite database files SHALL be stored in sandboxed application-private storage (`/data/data/com.aistudio.trotrorush.dftwum/databases/`).
- **NFR-SEC-002**: Integrity of level definition files SHALL be verified via checksum hashing.

### 5.4 Software Quality Attributes
- **Maintainability**: Pure business logic decoupled from Compose UI; 100% Kotlin codebase.
- **Reliability**: Zero crash tolerance; extensive unit testing covering edge cases (simultaneous departures, single-seat queues).
- **Usability**: System-wide onboarding tour and contextual hints ensure players can solve any puzzle without external guides.

---

## 6. Acceptance Criteria & Traceability Matrix

| Acceptance Test ID | Target Requirement | Description | Expected Outcome | Status |
|---|---|---|---|:---:|
| **AT-01** | REQ-ENG-001 | State Immutability Verification | Original state object unchanged after `applyAction` | PASS ✅ |
| **AT-02** | REQ-ENG-002 | Deterministic Transition | Given $(S_0, A_0)$, $applyAction$ returns identical $S_1$ across 1,000 runs | PASS ✅ |
| **AT-03** | REQ-ENG-004 | Legal Action Filtering | Blocked vehicles never present in `legalActions` list | PASS ✅ |
| **AT-04** | REQ-PLAY-003 | Directional Collision Check | Vehicle bumping into obstacle emits `EngineEvent.Blocked` | PASS ✅ |
| **AT-05** | REQ-PLAY-005 | Clean Exit to Slot | Vehicle with clear forward lane exits to slot 0 | PASS ✅ |
| **AT-06** | REQ-PLAY-006 | Undo State Restoration | Reverting 5 moves returns grid, slots, and queue to identical state | PASS ✅ |
| **AT-07** | REQ-QUEUE-003 | Gate Boarding Match | Front passenger matching parked car boards instantaneously | PASS ✅ |
| **AT-08** | REQ-QUEUE-004 | Cascading Queue Boarding | 3 consecutive matching passengers board in a single move | PASS ✅ |
| **AT-09** | REQ-QUEUE-005 | Level Balance Invariant | $\sum \text{Seats} == \text{Count}(\text{Passengers})$ across all 40 levels | PASS ✅ |
| **AT-10** | REQ-SLOT-002 | Vehicle Departure on Full | 2-seater car departs when 2nd passenger boards, freeing bay | PASS ✅ |
| **AT-11** | REQ-SLOT-005 | Gridlock Detection | Full bays with non-matching front passenger triggers Game Over | PASS ✅ |
| **AT-12** | REQ-SOLV-001 | 40 Level Solvability | BFS solver successfully solves all 40 levels from state 0 | PASS ✅ |
| **AT-13** | REQ-SOLV-003 | Par Star Calculation | Achieving par moves awards exactly 3 stars | PASS ✅ |
| **AT-14** | REQ-SEC-001 | Admin Passkey Barrier | Incorrect password rejects entry; correct grants dashboard access | PASS ✅ |
| **AT-15** | REQ-AUD-001 | Audit Logging Persistence | Admin progress reset creates persistent audit entry in SQLite | PASS ✅ |
| **AT-16** | REQ-A11Y-001 | Tri-Theme Switch | Theme switch from Light to High-Contrast alters background to `#000000` | PASS ✅ |
| **AT-17** | REQ-A11Y-002 | Dual Colour-Symbol Mapping | All 10 colours render assigned geometric symbols | PASS ✅ |
| **AT-18** | REQ-A11Y-004 | TalkBack Live Region | Moving vehicle triggers descriptive semantic TalkBack announcement | PASS ✅ |

---
**Certified by Techbridge University College ICT Directorate — Oyibi Campus, Accra, Ghana**

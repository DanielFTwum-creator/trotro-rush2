# TECHBRIDGE UNIVERSITY COLLEGE (TUC)
## Faculty of Computing and Information Systems
### Directorate of ICT Services — Oyibi Campus, Accra, Ghana

---

```
Document Ref : TUC-ICT-SOP-2026-034
Subject      : Interactive Tutorial System & Onboarding Sequence
System       : Trotro Rush Mobile Transit Puzzle Game
Owner        : Daniel Twum, Head of ICT
Standard     : IEEE 830 / IEEE 29148 / ISO/IEC 25010 (UK English)
Reference    : REQ-LVL-006 & Acceptance Test AT-22
Date         : September 2026
```

---

## 1. Overview & Objective

Conforming strictly to **REQ-LVL-006** and **AT-22**, the **Interactive Tutorial System** provides new players with contextual, progressive in-game guidance that explains the foundational mechanics of *Trotro Rush* across introductory levels (L001 to L003):
1. **Directional Movement & Clear Paths** (`TAP_TO_MOVE`)
2. **Traffic Obstacles & Blocked Lanes** (`BLOCKED_MOVE`)
3. **Queue Progression & Passenger Colour Matching** (`BOARDING`)
4. **Parking Bay Capacity, Departures & Gridlock Prevention** (`FULL_SLOTS`)

The system uses non-intrusive interactive cards, animated target indicators on the car park board, section callout borders, and authentic Ghanaian station conductor (*"mate"*) guidance.

---

## 2. Tutorial Sequence Architecture

```
 Level 1 Initial State
          │
          ▼
 ┌───────────────────────────────────────────────┐
 │ 1. TAP_TO_MOVE                                │
 │ - Highlights trotro v1 with [👇 TAP TO MOVE]  │
 │ - Explains directional arrows (↑, ↓, ←, →)    │
 │ - Path must be clear to station boundary      │
 └──────────────────────┬────────────────────────┘
                        │
                        │ Player taps v1 (exits & boards 2 red passengers)
                        ▼
 ┌───────────────────────────────────────────────┐
 │ 3. BOARDING (Matching & Queue)                │
 │ - Highlights Passenger Queue & Gate           │
 │ - Explains: Front passenger boards matching   │
 │   parked trotro automatically                 │
 └──────────────────────┬────────────────────────┘
                        │
                        │ Player taps blocked vehicle / attempts blocked move
                        ▼
 ┌───────────────────────────────────────────────┐
 │ 2. BLOCKED_MOVE (Traffic Jams & Obstacles)    │
 │ - Highlights trotro v2 with [➔ CLEAR THIS]    │
 │ - Explains: Yellow cannot drive through Blue  │
 │ - Must clear blocking trotro first            │
 └──────────────────────┬────────────────────────┘
                        │
                        │ Player fills parking bays
                        ▼
 ┌───────────────────────────────────────────────┐
 │ 4. FULL_SLOTS (Bays Capacity & Departures)    │
 │ - Highlights Parking Bay row (4-7 slots)      │
 │ - Explains: Trotros depart when full          │
 │ - Warns against deadlocks; teaches Undo/Hint  │
 └───────────────────────────────────────────────┘
```

---

## 3. Core Mechanics & Pedagogical Steps

### Step 1: Directional Movement & Clear Exit (`TAP_TO_MOVE`)
* **Core Rule**: Vehicles only drive strictly forward along their designated arrow direction. The path between the vehicle's front bumper and the perimeter of the grid must contain zero obstacles.
* **In-Game Overlay Indicator**: Pinned badge `👇 TAP TO MOVE` with animated cyan breathing border (`#06B6D4`) over trotro `v1` (Red Car, arrow `↑`).
* **Conductor's Call**: *"Accra! Accra direct! Check the arrow and tap the highlighted trotro with the open lane."*

### Step 2: Obstacles & Blocked Paths (`BLOCKED_MOVE`)
* **Core Rule**: Trotros cannot pass through, push, or steer around another vehicle.
* **In-Game Overlay Indicator**: Pinned badge `➔ CLEAR THIS FIRST` over blocking trotro `v2` (Blue Minibus).
* **Conductor's Warning**: *"Hold on driver! Yellow is stuck behind Blue. Move Blue first to clear the jam!"*

### Step 3: Passenger Matching & Gate Boarding (`BOARDING`)
* **Core Rule**: Passengers wait in single file. Only the front passenger standing at the gate can board a parked trotro, and only if the colours match and a free seat is available.
* **In-Game Overlay Indicator**: Animated cyan border surrounding the `PASSENGER QUEUE` card with gate badge.
* **Station Rule**: *"Only the passenger standing at the gate can board. Colours must match! When full, the trotro departs to free up the bay."*

### Step 4: Bay Capacity & Avoiding Deadlock (`FULL_SLOTS`)
* **Core Rule**: Parking bays are strictly constrained (4 to 7 slots). If all bays fill up with trotros that cannot board the front passenger, the station enters a deadlock.
* **In-Game Overlay Indicator**: Animated border on `PARKING SLOTS` card.
* **Pro Tip**: *"If you get stuck in gridlock, tap Undo to reverse moves or tap Hint for CI optimal routing."*

---

## 4. Acceptance Criteria & Lifecycle Rules (AT-22)

1. **Replay Persistence**: Once a level is marked completed (`repository.isLevelCompleted(level.id) == true`), tutorial overlays do not appear automatically upon replay, preventing nuisance for experienced players.
2. **On-Demand Access**: Players can toggle or re-open the interactive tutorial overlays at any time using the `?` Help icon button in the TopAppBar (`testTag="tutorial_toggle_button"`).
3. **Multi-Modal Accessibility**: All steps include full TalkBack screen-reader descriptions and meet WCAG 2.1 AA touch target standards ($\ge 48\text{dp}$).
4. **Verification**: Automated test suite `TutorialSystemTest.kt` passes with 100% assertions across order, progression, and target mappings.

# AGENTS.md — Trotro Rush (Web Edition Specification)
**Document ID:** TUC-DOC-AGENTS-2026-001  
**Project:** Trotro Rush — Mobile-First Ghanaian Transit Puzzle  
**Institution:** Techbridge University College (TUC), Oyibi, Ghana  
**Owner:** Daniel Twum, Head of ICT & Special Adviser to the Founder  
**Specification Standard:** IEEE 830 / ISO/IEC/IEEE 29148 (UK English)  
**Target Web Tech Stack:** React 18/19, TypeScript, Tailwind CSS, Vite, Lucide React, Web Audio API, Canvas API, LocalStorage  

---

## 1. Executive Summary & Purpose

This document serves as the **master engineering instruction prompt** for autonomous AI agents generating or maintaining the full Web version of **Trotro Rush**. 

Trotro Rush is a topological transit puzzle game set in Greater Accra, Ghana. Players act as the **Station Master** in bustling lorry parks, maneuvering trapped commercial minibuses (*trotros*) out of congested yards into dedicated loading bays to collect waiting passengers queued at the boarding gate.

---

## 2. Core Game Architecture & Invariants

```text
[ User Tap / Click / Drag / Keyboard ]
                │
                ▼
      ┌───────────────────┐
      │   UI Component    │ (React / Tailwind / Canvas)
      └─────────┬─────────┘
                │ Dispatches Move(vehicleId)
                ▼
      ┌───────────────────┐
      │   Rules Engine    │ ◄─── Pure Deterministic Functions
      │ (Immutable State) │
      └─────────┬─────────┘
                │ Returns (NextState, EngineEvents[])
                ▼
   ┌────────────┴────────────┬────────────────────────┐
   ▼                         ▼                        ▼
[ State Store ]       [ Sound / Haptics ]      [ Visual Effects ]
- Undo Stack          - Web Audio Synth        - Road Dust Particles
- Score & Timer       - Tone Synthesis         - Confetti Cannon
- Bay / Queue state   - Trotro Horn / Chimes   - Bump Animation
```

### 2.1 Pure Deterministic Engine Rules (MANDATORY)

1. **Yard Coordinate System**:
   - 2D grid matrix of size $C \times R$ (Columns $\times$ Rows), 0-indexed: `(col, row)`.
   - Origin `(0, 0)` is top-left.
   - Cells are either vacant or occupied by a vehicle body part.

2. **Vehicle Topography**:
   - **Type**: Minibus (length 2, 4 passenger seats) or Large Bus (length 3, 6 passenger seats).
   - **Orientation**: Horizontal (`width = length`, `height = 1`) or Vertical (`width = 1`, `height = length`).
   - **Direction of Travel (Strict Single Arrow)**:
     - `UP`: Moves toward `row - 1`. Exits yard when `row == 0`.
     - `DOWN`: Moves toward `row + 1`. Exits yard when `row + length == gridRows`.
     - `LEFT`: Moves toward `col - 1`. Exits yard when `col == 0`.
     - `RIGHT`: Moves toward `col + 1`. Exits yard when `col + length == gridCols`.

3. **Raytracing Exit Path Verification**:
   - When a vehicle is clicked/tapped, cast a ray along its forward direction from its front bumper to the grid perimeter.
   - If **any** cell along that ray is occupied by another vehicle:
     - **Move Rejected**: The vehicle cannot exit.
     - **Bump Animation**: Trigger a spring/damped 6px forward-and-back bounce animation.
     - **Sound/Haptic**: Play dull bumper thud (`160Hz -> 80Hz` decay).
   - If the ray is completely clear:
     - Check Parking Bay availability.

4. **Parking Bay Management**:
   - The station contains $K$ parking bays ($4 \le K \le 7$, configured per level).
   - If all bays currently hold a vehicle:
     - **Move Blocked**: Trigger `NoFreeSlot` event.
     - Display transient alert: *"All bays full! Wait for a trotro to depart."*
   - If at least one empty bay exists:
     - Remove the vehicle from the yard grid.
     - Place vehicle in the first available empty parking bay (leftmost `P1..PK`).
     - Increment `moves` counter by 1.
     - Push previous state onto the `undoStack`.

5. **Passenger Queue & Boarding Cascade**:
   - Passengers line up in a single-file FIFO queue leading to the **Gate**.
   - Dual-encoding: Every passenger and vehicle has both a **Colour** and a **Geometric Symbol**:
     1. Red (`#EF4444`): ● Circle
     2. Blue (`#3B82F6`): ■ Square
     3. Yellow (`#F59E0B`): ▲ Triangle
     4. Green (`#10B981`): ◆ Diamond
     5. Purple (`#8B5CF6`): ★ Star
     6. Orange (`#F97316`): ⬡ Hexagon
     7. Pink (`#EC4899`): ♥ Heart
     8. Teal (`#14B8A6`): ✚ Plus
     9. Indigo (`#6366F1`): ☾ Crescent
     10. Amber (`#D97706`): ○ Ring
   - **Cascade Loop**:
     1. Examine head passenger `queue[0]`.
     2. Scan all docked parking bays from left to right.
     3. If a docked vehicle has **same colour** AND `passengersOnBoard < totalSeats`:
        - Passenger boards: vehicle `passengersOnBoard++`.
        - Passenger is removed from `queue`.
        - Trigger `Boarded` sound and particle ring.
        - Repeat cascade for new head passenger.
     4. If matching vehicle reaches `passengersOnBoard == totalSeats`:
        - Vehicle departs immediately.
        - Slot is freed (`null`).
        - Trigger `Departed` sound and acceleration animation.
        - Re-check cascade with newly available slot or remaining queue.
     5. Cascade stops when `queue[0]` does not match any docked vehicle with free seats, or queue is empty.

6. **Outcome Conditions**:
   - **Win**: `queue.length === 0` AND all parked vehicles have departed.
   - **Lose**: All parking bays are occupied, no parked vehicle matches `queue[0]`, and no legal moves exist on the yard grid that can clear.
   - **Undo**: Restores previous immutable state from `undoStack`.
   - **Restart**: Resets state to initial level configuration; resets timer and moves.

---

## 3. Web UI / UX Component Hierarchy

The web application must be implemented with modern React, Tailwind CSS, and Lucide icons:

```text
<App>
├── <TopNavigation>
│   ├── Level Title & Corridor Badge ("L001: Tema Station" · "BEGINNER")
│   ├── Live Score & Elapsed Time HUD
│   ├── Par Target & Move Counter ("Moves: 2 / Par: 3")
│   └── Action Buttons (Undo, Restart, Hint, Virtual Tour, Settings/Admin)
├── <GameBoardLayout> (Centered, responsive container)
│   ├── <PassengerQueueView>
│   │   ├── Gate Landmark & Direction Arrow
│   │   └── Animated Passenger Tokens (Colour + Accessible Symbol)
│   ├── <ParkingBaysView>
│   │   └── Docked Trotros with Seat Occupancy Dots (e.g. "3/4 seats")
│   ├── <CarParkGrid>
│   │   ├── Background Road Dust & Harmattan Particle Layer
│   │   ├── Directional Grid Perimeter Exits (Gate markings)
│   │   └── <VehicleItem>
│   │       ├── Bumper Directional Arrow (▲ ▼ ◄ ►)
│   │       ├── Seat Count Badge ("4s" / "6s")
│   │       ├── Windscreen Ghanaian Motto ("Sea Never Dry", "God's Time")
│   │       └── Hint Highlight Glow (when active)
│   └── <ControlBar>
│       ├── Undo Button (with remaining undo count / enabled state)
│       ├── Hint Button (CI BFS recommended move)
│       └── Restart Button
├── <Modals & Dialogs>
│   ├── <WinCelebrationOverlay>
│   │   ├── Animated 'Trotro Arrived!' Banner
│   │   ├── Dynamic Confetti Particle Cannon (Ghanaian Gold, Red, Green, Blue)
│   │   ├── Highlighted Performance Rating (Moves vs Par: Master / Expert / Steady)
│   │   ├── Score Breakdown Card (Speed bonus, Par bonus, High score)
│   │   └── Next Station / Replay / Level Select Actions
│   ├── <LoseDefeatDialog>
│   ├── <VirtualTourDialog> (Colloquial history & authentic landmarks)
│   ├── <LevelSelectDrawer> (40 stations grouped by corridor)
│   └── <AdminConsoleModal> (Passkey protected: 'TUC2026')
```

---

## 4. Scoring Algorithm & Performance Rating

### 4.1 Score Formula
$$\text{Score} = \text{BaseClearPoints} + (\text{Passengers} \times 100) + \max(0, (\text{Par} - \text{Moves}) \times 250) + \max(0, (120 - \text{ElapsedSeconds}) \times 10)$$

- **Perfect Par Bonus**: $+500$ points if $\text{Moves} \le \text{Par}$.
- **Base Clear Points**: $1,000$ points.

### 4.2 Performance Rating Tiers
| Tier Name | Move Threshold | Star Rating | Rank Badge | Descriptive Praise |
|---|---|---|---|---|
| **Master Station Master** | $\text{Moves} \le \text{Par}$ | ★★★ (3 Stars) | `MASTER STATION MASTER 🇬🇭` | *"Flawless navigation! You cleared the yard at minimal Par with zero wasted moves."* |
| **Expert Conductor** | $\text{Moves} \le \text{Par} + 2$ | ★★☆ (2 Stars) | `EXPERT CONDUCTOR` | *"Sharp coordination! Fast passenger boarding with only +N moves from perfect Par."* |
| **Steady Driver** | $\text{Moves} > \text{Par} + 2$ | ★☆☆ (1 Star) | `STEADY DRIVER` | *"All passengers safely reached the terminal! Replay this station to aim for Par."* |

---

## 5. Web Audio API Procedural Synthesizer

Do **not** require external MP3/WAV files. Implement an inline procedural Web Audio synthesizer:

```typescript
class WebSoundPlayer {
  private ctx: AudioContext | null = null;
  public isMuted: boolean = false;

  private getContext(): AudioContext {
    if (!this.ctx) {
      this.ctx = new (window.AudioContext || (window as any).webkitAudioContext)();
    }
    if (this.ctx.state === 'suspended') {
      this.ctx.resume();
    }
    return this.ctx;
  }

  // 1. Vehicle Exit whoosh
  playMove() {
    if (this.isMuted) return;
    const ctx = this.getContext();
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.type = 'triangle';
    osc.frequency.setValueAtTime(220, ctx.currentTime);
    osc.frequency.exponentialRampToValueAtTime(440, ctx.currentTime + 0.15);
    gain.gain.setValueAtTime(0.18, ctx.currentTime);
    gain.gain.linearRampToValueAtTime(0.01, ctx.currentTime + 0.15);
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.start();
    osc.stop(ctx.currentTime + 0.16);
  }

  // 2. Blocked Bumper Thud
  playBump() {
    if (this.isMuted) return;
    const ctx = this.getContext();
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.type = 'sine';
    osc.frequency.setValueAtTime(140, ctx.currentTime);
    osc.frequency.exponentialRampToValueAtTime(50, ctx.currentTime + 0.12);
    gain.gain.setValueAtTime(0.25, ctx.currentTime);
    gain.gain.linearRampToValueAtTime(0.01, ctx.currentTime + 0.12);
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.start();
    osc.stop(ctx.currentTime + 0.13);
  }

  // 3. Passenger Boarding Chime
  playBoard() {
    if (this.isMuted) return;
    const ctx = this.getContext();
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.type = 'sine';
    osc.frequency.setValueAtTime(587.33, ctx.currentTime); // D5
    osc.frequency.setValueAtTime(880, ctx.currentTime + 0.06); // A5
    gain.gain.setValueAtTime(0.15, ctx.currentTime);
    gain.gain.linearRampToValueAtTime(0.01, ctx.currentTime + 0.15);
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.start();
    osc.stop(ctx.currentTime + 0.16);
  }

  // 4. Trotro Departure Rev
  playDepart() {
    if (this.isMuted) return;
    const ctx = this.getContext();
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.type = 'sawtooth';
    osc.frequency.setValueAtTime(130, ctx.currentTime);
    osc.frequency.exponentialRampToValueAtTime(260, ctx.currentTime + 0.28);
    gain.gain.setValueAtTime(0.12, ctx.currentTime);
    gain.gain.linearRampToValueAtTime(0.01, ctx.currentTime + 0.3);
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.start();
    osc.stop(ctx.currentTime + 0.31);
  }

  // 5. 'Trotro Arrived!' Victory Fanfare
  playWin() {
    if (this.isMuted) return;
    const ctx = this.getContext();
    const notes = [523.25, 659.25, 783.99, 1046.50]; // C5, E5, G5, C6
    notes.forEach((freq, idx) => {
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.type = 'triangle';
      osc.frequency.setValueAtTime(freq, ctx.currentTime + idx * 0.1);
      gain.gain.setValueAtTime(0.2, ctx.currentTime + idx * 0.1);
      gain.gain.linearRampToValueAtTime(0.01, ctx.currentTime + idx * 0.1 + 0.25);
      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.start(ctx.currentTime + idx * 0.1);
      osc.stop(ctx.currentTime + idx * 0.1 + 0.26);
    });
  }
}
```

---

## 6. Breadth-First Solver (BFS) Specification

Implement the BFS solver in TypeScript to power CI solvability validation and runtime hints:

```typescript
interface SolverState {
  vehicles: Vehicle[];
  slots: (Vehicle | null)[];
  queueHead: number;
}

export function solveBFS(initialState: SolverState, maxDepth = 25): string[] | null {
  const queue: { state: SolverState; path: string[] }[] = [];
  const visited = new Set<string>();

  const hash = (s: SolverState) =>
    s.vehicles.map(v => `${v.id}:${v.col},${v.row}`).join('|') +
    '#' + s.slots.map(sl => sl ? sl.id : '_').join(',') +
    '#' + s.queueHead;

  queue.push({ state: initialState, path: [] });
  visited.add(hash(initialState));

  while (queue.length > 0) {
    const { state, path } = queue.shift()!;

    if (state.queueHead >= TOTAL_PASSENGERS && state.slots.every(s => s === null)) {
      return path; // Win state found!
    }

    if (path.length >= maxDepth) continue;

    for (const v of state.vehicles) {
      if (canExitYard(v, state.vehicles)) {
        const nextState = applyMove(state, v.id);
        const h = hash(nextState);
        if (!visited.has(h)) {
          visited.add(h);
          queue.push({ state: nextState, path: [...path, v.id] });
        }
      }
    }
  }
  return null; // Deadlock or unresolvable
}
```

---

## 7. 40-Station Transit Corridor Catalog

Stations are organized across 3 continuous corridors strictly preserving topological progression:

### Corridor A: N4 Liberation Road (Levels L001 – L015)
1. **L001: Tema Station / CMB** (Accra Central Hub · Par 3)
2. **L002: Ridge / Cedi House** (Financial District · Par 4)
3. **L003: Police Headquarters / Danquah Link** (Ring Road East · Par 4)
4. **L004: 37 Military Hospital** (Transit Hub Interchange · Par 5)
5. **L005: Spanner Junction / Tetteh Quarshie** (Commercial Node · Par 6)
6. **L006: Shiashie Under-Bridge** (Airport Bypass Stage · Par 6)
7. **L007: Legon Hall Gate / University of Ghana** (Academic Stage · Par 7)
8. **L008: Okponglo / Haatso Turnoff** (Major Arterial Junction · Par 7)
9. **L009: Presec Gate** (Secondary Stage · Par 8)
10. **L010: Atomic Junction** (Haatso/Atomic Link · Par 8)
11. **L011: Madina Zongo Junction** (Vibrant Market Stage · Par 9)
12. **L012: Madina Station Central** (Bustling Municipal Terminal · Par 10)
13. **L013: Ritz Junction** (Adenta Feeder Stage · Par 10)
14. **L014: Redco Flats** (Residential Arterial Node · Par 11)
15. **L015: Adenta Barrier / Housing** (Expressway Outer Terminal · Par 12)

### Corridor B: N6 Nsawam / Achimota Corridor (Levels L016 – L028)
16. **L016: Kwame Nkrumah Circle (Odorna Terminal)** (Grand Central Hub · Par 5)
17. **L017: Caprice / Cocobod** (Commercial Stage · Par 6)
18. **L018: Avenor Junction** (Industrial Link · Par 6)
19. **L019: Alajo Junction** (High-Density Residential Stage · Par 7)
20. **L020: Abeka Lapaz (Bambolino Stage)** (Bustling Junction Interchange · Par 8)
21. **L021: Flat Top / Nyamekye Link** (Feeder Stage · Par 8)
22. **L022: Chantan / Tabora Junction** (Residential Link · Par 9)
23. **L023: Achimota New Station (Neoplan)** (Interstate Lorry Park · Par 9)
24. **L024: Mile 7 / Achimota ABC** (Suburban Stage · Par 10)
25. **L025: St. John's Grammar School** (Academic Stage · Par 10)
26. **L026: Dome Crossing / Railway Stage** (Commercial Node · Par 11)
27. **L027: Taifa Junction** (Residential Stage · Par 11)
28. **L028: Pokuase Interchange (ACP Yard)** (Major Multi-tier Interchange · Par 12)

### Corridor C: Winneba Road / Kaneshie Arterial (Levels L029 – L040)
29. **L029: Kaneshie Complex (Main Market Terminal)** (Multi-tier Terminal Complex · Par 6)
30. **L030: First Light / Kaneshie West** (Intersection Stage · Par 6)
31. **L031: Atico Junction** (Commercial Node · Par 7)
32. **L032: Bubuashie / Cable & Wireless** (Feeder Stage · Par 8)
33. **L033: Darkuman Junction** (Commercial Stage · Par 8)
34. **L034: Odorkor Official Town** (Arterial Interchange · Par 9)
35. **L035: Sakaman Junction** (Feeder Stage · Par 9)
36. **L036: Mallam Junction / Under-Bridge** (Arterial Crossroads · Par 10)
37. **L037: Gbawe Zero Point** (Feeder Stage · Par 10)
38. **L038: McCarthy Hill Foot** (Topographical Landmark Stage · Par 11)
39. **L039: Tetegu Junction / Weija Barrier** (Waterworks Wetland Link · Par 12)
40. **L040: Kasoa Toll Booth (Old Barrier)** (Inter-Regional Border Terminal · Par 14)

---

## 8. Web Delivery & Build Checklist

1. **Vite + React Setup**:
   ```bash
   npm create vite@latest trotro-rush -- --template react-ts
   npm install -D tailwindcss postcss autoprefixer
   npx tailwindcss init -p
   npm install lucide-react canvas-confetti
   ```

2. **Tailwind Config Extensions**:
   - `colors.trotro`: Laterite Ochre (`#9A3412`), Golden Yellow (`#F59E0B`), Deep Slate (`#0A0F1D`).
   - `animation`: Bumper bounce (`bounce-short`), Confetti burst, Dust drift.

3. **Accessibility**:
   - Every vehicle button must have `aria-label` detailing: *"[Colour] [Type], [Seats] seats, moving [Direction], Row [R] Column [C]"*.
   - Keyboard navigation via Tab / Enter / Arrow keys.
   - Live region (`aria-live="polite"`) announcing boarding cascades and station clearances.

---

## 9. Verification & Quality Gate

Prior to production deployment:
1. Verify that **all 40 levels** pass the automated BFS solver check (`solveBFS(level.initialState) !== null`).
2. Verify sound synthesizer plays smoothly across all major browsers (Chrome, Safari, Firefox, Edge) without requiring user gesture errors (resume AudioContext on first tap).
3. Confirm 100% responsive display on mobile viewport (`360px` to `430px` width) and desktop widescreen (`1920px`).
4. Validate that 'Trotro Arrived!' victory animation triggers with vibrant confetti and performance rating badge upon clearing any level.

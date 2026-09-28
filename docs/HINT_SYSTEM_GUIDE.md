# TECHBRIDGE UNIVERSITY COLLEGE (TUC)
## Faculty of Computing and Information Systems
### Directorate of ICT Services — Oyibi Campus, Accra, Ghana

---

```
Document Ref : TUC-ICT-SOP-2026-033
Subject      : CI Solver Hint System Architecture & Implementation SOP
System       : Trotro Rush Mobile Transit Puzzle Game
Owner        : Daniel Twum, Head of ICT
Standard     : IEEE 830 / ISO/IEC 25010
Date         : September 2026
```

---

## 1. Overview & Objective

The **CI Solver Hint System** provides real-time, deterministic move guidance for players encountering challenging traffic gridlock scenarios across Greater Accra's transit corridors. Built directly upon the Continuous Integration (CI) Breadth-First-Search (`Solver.kt`) engine, the system evaluates the player's exact in-progress game state, computes the minimum-move trajectory to `GameStatus.WON`, highlights the next optimal trotro on the board, and delivers authentic Ghanaian transit commentary.

---

## 2. Core Architecture

```
               ┌───────────────────────────────┐
               │         PlayScreen UI         │
               │  [💡 Hint Button / TopAppBar] │
               └───────────────┬───────────────┘
                               │ (Dispatchers.Default)
                               ▼
               ┌───────────────────────────────┐
               │          HintSystem           │
               └───────────────┬───────────────┘
                               │
         ┌─────────────────────┴─────────────────────┐
         ▼                                           ▼
┌─────────────────────────┐               ┌─────────────────────────┐
│ Fast-Path Deadlock Check│               │    Solver (BFS Engine)   │
│  - Bays Full vs Queue   │               │ - solveFromState()      │
│  - State Space Analysis │               │ - Transposition Cache   │
└─────────────────────────┘               └─────────────┬───────────┘
                                                        │
                                                        ▼
                                          ┌───────────────────────────┐
                                          │      HintResult.Success   │
                                          │  - nextVehicleId          │
                                          │  - winningSequence        │
                                          │  - reason & movesToWin    │
                                          └───────────────────────────┘
```

### 2.1 State-Agnostic BFS Resolution (`solveFromState`)
While initial level validation resolves puzzles starting from `RulesEngine.createState(level)`, the gameplay hint system resolves dynamically from `currentState: EngineState` — accommodating partially boarded vehicles, depleted slots, and historical moves.

```kotlin
fun solveFromState(
    currentState: EngineState,
    maxStates: Int = 35_000,
    timeLimitMs: Long = 5_000
): SolveResult
```

### 2.2 Fast-Path Deadlock & Unsolvable State Detection
If all parking bays are occupied and none match the color of the waiting passenger at the head of the queue, the system instantly catches the deadlock without burning CPU cycles:
```kotlin
if (currentState.freeSlotsCount == 0 && currentState.queue.isNotEmpty()) {
    val nextPassenger = currentState.queue.first()
    val anySlotMatches = currentState.slots.any { it != null && it.colour == nextPassenger }
    if (!anySlotMatches) {
        return HintResult.Deadlock(
            title = "Parking Bays Locked",
            message = "All parking bays are occupied, and none match the waiting ${nextPassenger.displayName} passenger at the gate. Tap Undo to back up!",
            canUndo = true,
            isSlotsFull = true,
            statesExplored = 0
        )
    }
}
```

---

## 3. UI/UX & Sensory Affordances

1. **Board Highlighting (`VehicleView.kt`)**:
   - **Pulsing Radiant Amber Outline**: Border animates smoothly between `0.45` and `1.0` alpha at `600ms` cycle time.
   - **Tactile Scaling Breathing**: Subtle scale breathing between `1.0` and `1.04` to attract focus.
   - **Visual Pill Badge**: `💡 NEXT` badge pinned to the top-right corner of the recommended trotro.
2. **Contextual Action Banner (`HintBanner`)**:
   - Displays move count remaining to clear the level.
   - Culturally authentic mate instructions (e.g. *"Direct Match! Drive Yellow trotro East into a bay to board the waiting Yellow passenger"*).
   - Instant action: **[Execute Move ➔]** taps the trotro directly for the player.
3. **Assistance Suggestion Chip**:
   - Automatically surfaces when a player triggers 3 consecutive blocked moves: *"Traffic jammed? Tap for CI Solver Hint!"*
4. **Assistive Accessibility (TalkBack)**:
   - Publishes announcements to Android screen readers via `liveRegion = LiveRegionMode.Polite`:
     *"Hint: Move Blue Trotro heading east (Optimal next move recommended)."*

---

## 4. Automated Verification Matrix

| Test Method | Target Scenario | Assertion | Result |
| :--- | :--- | :--- | :---: |
| `testHintOnInitialStateL001` | Fresh Level 1 | Returns `v1` (Red Car), 3 moves to win | PASSED ✅ |
| `testHintOnMidGameStateL001` | Mid-game after `v1` | Returns `v2` (Blue Minibus), 2 moves to win | PASSED ✅ |
| `testHintOnNearWinStateL001` | Near-win after `v2` | Returns `v3` (Yellow Car), 1 move to win | PASSED ✅ |
| `testHintOnWonStateReturnsAlreadyWon` | Completed Level | Returns `HintResult.AlreadyWon` | PASSED ✅ |
| `testHintOnDeadlockDetectsGridlock` | Full slots with mismatched queue | Returns `HintResult.Deadlock`, `canUndo = true` | PASSED ✅ |

All automated JVM unit tests pass in **15 seconds** (`gradle :app:testDebugUnitTest`).

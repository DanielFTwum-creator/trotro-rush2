# Trotro Rush: Software Requirements Specification
**Document ID:** TUC-ICT-SRS-2026-030  
**Revision:** 1.0 · 26 September 2026 · Conformant with IEEE/ISO/IEC 29148-2018 (IEEE 830 heritage)  
**Institution:** Techbridge University College (TUC), Oyibi, Greater Accra, Ghana  
**Owner:** Daniel Frempong Twum, Head of ICT & Special Adviser to the Founder  
**Tech Stack:** Kotlin, Jetpack Compose, Material Design 3, Room SQLite, Coroutines, StateFlow

---

## 1. Executive Summary & Purpose
Trotro Rush is an authentic, Ghanaian-themed colour-matching traffic-jam puzzle game designed to run smoothly on Android and cross-platform mobile environments without requiring network access, advertisements, or data collection. Players navigate a grid-based car park filled with jammed vehicles (cars, trotros, buses) adorned with Ghanaian slogans ("Nyame Bekyere", "Sea Never Dry", "Slow But Sure"). Releasing vehicles in their long-axis arrow direction moves them to a limited row of parking slots, where waiting queue passengers of matching colours board automatically.

## 2. System Architecture & Diagrams
The application strictly separates:
- **Pure Deterministic Rules Engine**: Immutable state transitions with zero I/O side-effects.
- **Exhaustive BFS Solver**: Pre-validates every level to prove solvability and determine shortest-path par.
- **Jetpack Compose UI Shell & Accessibility Layer**: Full TalkBack live region announcements, keyboard shortcuts, and triple-theme support (Light, Dark, High-Contrast).
- **Local Persistence**: Room SQLite database preserving progress, star ratings, and administrator audit logs.

## 3. Verified Functional Requirements
- **REQ-PLAY-001 to 012**: Vehicle selection, clear exit path verification, bump animations on blocked path, no-free-slot warnings, unlimited undo stack, and restart mechanics.
- **REQ-QUEUE-001 to 005**: Ordered queue, 10 accessible colour-symbol pairings, automatic cascading boarding.
- **REQ-SLOT-001 to 005**: 4 to 7 parking bays, leftmost insertion, immediate departure on full capacity.
- **REQ-LVL-001 to 013**: 40 solvable hand-checked levels, colour-passenger seat balance invariant.
- **REQ-A11Y-001 to 012**: Dual colour-symbol coding (Circle, Square, Triangle, Diamond, Star, Hexagon, Heart, Cross, Crescent, Ring), WCAG 2.2 AA/AAA contrast conformance.

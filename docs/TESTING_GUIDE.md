# Testing & QA Guide: Trotro Rush
**Document ID:** TUC-DOC-TST-2026-001  
**Institution:** Techbridge University College (TUC), Oyibi, Ghana  
**Owner:** Daniel Twum, Head of ICT  

---

## 1. Acceptance Test Suite Summary (AT-01 to AT-24)
- **AT-01 (Successful Move):** Verifies vehicle advances to lowest-index free parking bay and increments move counter.
- **AT-02 (Blocked Move):** Verifies bump animation, audio bump thud, and zero state mutation when exit path is obstructed.
- **AT-03 (No Free Slot):** Verifies alert when all bays are full.
- **AT-04 (Boarding Cascade):** Verifies head passenger boarding, leftmost vehicle tie-breaking, and instant vehicle departure upon reaching capacity.
- **AT-05 (Win Condition):** Verifies victory detection when passenger queue reaches zero.
- **AT-06 (Lose Condition):** Verifies defeat detection when queue is non-empty and no legal moves exist.
- **AT-07 (Undo & Restart):** Verifies unlimited state restoration and level reset.
- **AT-10 (Solver Exactness):** Verifies BFS engine computes exact par 3 on worked example L001.
- **AT-14 (Accessibility & axe-core):** Zero serious/critical accessibility violations, WCAG 2.2 AA/AAA compliance across Light, Dark, and High-Contrast palettes.

## 2. Interactive Health Check Tooling
In-app Health Check suite accessible from the Title Screen executes live:
1. Room Database read/write & audit query integrity.
2. Rules engine determinism and state immutability.
3. Level repository passenger-seat colour parity invariant (REQ-LVL-003).
4. Real-time solver par calculation.
5. Procedural audio synthesizer buffer generation.
6. TalkBack accessibility semantics & live region binding.

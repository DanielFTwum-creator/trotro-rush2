# Administrator Guide: Trotro Rush
**Document ID:** TUC-DOC-ADM-2026-001  
**Target:** Engineering, Operations & Governance Staff  
**Institution:** Techbridge University College (TUC), Oyibi, Ghana  
**Owner:** Daniel Twum, Head of ICT  

---

## 1. Authentication & Security
The Administrator Console is password-protected to ensure only authorized TUC engineering personnel can modify game state, view audit trails, or run diagnostic benchmarks.
- **Default Master Passkey:** `trotro2026` (or `TUC2026`)
- **Access Route:** Via the Title Screen `[Admin Auth]` button or through Settings `[Admin Console & Audit Log]`.
- **Session Duration:** Persists until explicit `[Logout]` or application termination.

## 2. Audit Trail Operations
Every critical action is logged to the local SQLite `audit_log` table:
- `SYSTEM_INIT`: First-run database setup and Level 1 unlock.
- `ADMIN_LOGIN_SUCCESS` / `ADMIN_LOGIN_FAILED`: Records administrative access attempts.
- `ADMIN_UNLOCK_ALL`: Bulk unlocking of all 40 levels.
- `PROGRESS_RESET`: Complete user purge of scores and stars.
- `SOLVER_RUN`: In-app solver executions with benchmark metrics.

## 3. Administrative Tooling
- **Unlock All 40 Levels:** Immediately unlocks all levels and awards 3 stars for testing and QA inspection.
- **Solver Benchmark:** Executes the BFS pathfinder against the selected level to verify shortest-path par and memory limits.

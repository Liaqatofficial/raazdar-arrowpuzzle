# Raazdar: Arrow Puzzle Game — rebuild app

Independent, offline-first reimplementation. **VERIFIED & shipped as an installable APK.**

| Item | Value |
|---|---|
| applicationId | `com.raazdar.arrowpuzzlegame` (verified installable; no conflicting package on test device) |
| Display name | Arrow Puzzle |
| Brand | Raazdar (About screen, website, publisher identity) |
| Version | 1.0.0 (versionCode 1), minSdk 26 / targetSdk 34 |
| Permissions | `VIBRATE` only. No INTERNET, no storage, no tracking. |
| APK | `rebuild/dist/raazdar-arrowpuzzle-1.0.0.apk` (33 KB, signed with `~/.android/raazdar-debug.keystore`) |

## Build (no Gradle required)
```
powershell -ExecutionPolicy Bypass -File rebuild/build.ps1
```
Requires: JDK 17, Android build-tools 35.0.0, android-36 platform (all present in this environment).

## Source layout
- `app/src/main/java/com/raazdar/arrowpuzzlegame/Game.java` — pure-JVM puzzle model (grid, clear-path ray test, hearts, undo, hint, reset, revive, deterministic solvable level generator).
- `GameView.java` — board rendering, escape animations, blocked shake, hint highlight, accessibility announcements + live board content description.
- `MainActivity.java` — HUD, Undo/Hint/Restart/Settings, win/fail dialogs, Raazdar About, persistence (SharedPreferences), haptics.
- `app/tests/HostTest.java` — model test suite (runnable on host JVM).
- `app/tests/PrintLevel.java` — prints a level's layout + guaranteed solution (used for deterministic device tests).

## Implemented (verified) mechanics — taken from the audited spec only
- 4-direction arrows; tap escapes if path to edge is clear, blocked otherwise.
- Hearts pool = 3; blocked tap costs 1; win/lose states; Try-again revive keeps cleared arrows.
- Unlimited Undo (restores exact origin), Restart, Hint (highlights an arrow with a clear path).
- Levels 1..999 deterministic, guaranteed solvable by construction, offline.
- Haptics toggle persisted; content descriptions on all controls.

## Deliberately NOT implemented (EVIDENCE_BASED, per audit)
- No coins/IAP SKUs, no ads, no daily-challenge backend, no leaderboards — original economy/backend behavior was UNVERIFIED or server-side; fake implementations and mock rewards were not added. "Offline" claims are supported by zero network permissions.

## Test evidence
- Host: `PASS: all host model tests` (300 levels solvable+bounded, determinism, tap-driven solve of 100 levels, undo/reset/hint, out-of-bounds tap safety).
- Device (API 35 emulator): launch OK (UI dump confirms HUD), Level 1 board state matches host model exactly (`3x4|^^>/^>>/.<v/>.v`), full solution played via adb taps → "Level complete!" dialog → Next Level loads Level 2 (state matches model), 3 blocked taps → "Out of hearts" dialog, TRY AGAIN restored ♥ 3 while keeping cleared arrows, Settings shows Raazdar brand + persisted haptics toggle, Undo & Hint exercised with no crash. Screenshots: `rebuild/store-listing/screenshots/*.png`.

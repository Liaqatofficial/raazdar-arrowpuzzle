# Test Evidence — Arrow Puzzle audit (2026-10-26)

## Runtime evidence — Rebuild app (added later, 2026-10-26)

APK: `rebuild/dist/raazdar-arrowpuzzle-1.0.0.apk` (33,249 bytes; signed; apksigner verify exit 0).

| # | Test | Action | Observed result | Status |
|---|---|---|---|---|
| R1 | Install | `adb install -r` | `Success`; package `com.raazdar.arrowpuzzlegame` present | PASS |
| R2 | Launch | `am start` | No crash; UI dump shows "Level 1", "♥ 3", 4 labeled buttons | PASS |
| R3 | State parity | UI dump board content-desc vs host model | `3x4|^^>/^>>/.<v/>.v` identical on device & host | PASS |
| R4 | Solvability (model ×300) | host `HostTest` | `PASS: all host model tests` (300 solvable, deterministic, 100 tap-driven solves, undo/reset/hint, OOB-safety) | PASS |
| R5 | Real gameplay solve | 10 adb taps in recorded solution order on device | Menu → "Level complete! / Level 1 cleared!" dialog with NEXT LEVEL / RESTART LEVEL | PASS |
| R6 | Progression | tap NEXT LEVEL | HUD "Level 2"; board state matches host model for level 2 | PASS |
| R7 | Blocked cost | 3 blocked taps | Hearts 3→0 → "Out of hearts" dialog (TRY AGAIN / RESTART LEVEL) | PASS |
| R8 | Revive semantics | tap TRY AGAIN | HUD "♥ 3" restored; cleared arrow stayed cleared (state `.../.>./>>>`) | PASS |
| R9 | Branding | open Settings | Dialog shows "Raazdar: Arrow Puzzle Game", https://raazdar.xyz, non-affiliation note; haptics checkbox functional | PASS |
| R10 | Hint & Undo | press Hint, then Undo | no crash; focus stays on MainActivity; board state updated | PASS |
| R11 | Stability | logcat `AndroidRuntime` filter for package | no FATAL/crash entries attributable to `com.raazdar.*` (only unrelated pre-existing Goog qsb noise) | PASS |

Screenshots: `rebuild/store-listing/screenshots/{runtime_home,runtime_after_tryagain,runtime_hint_undo}.png`

## Original-app runtime (BLOCKED, see above)

Original APK on the same emulator: `adb install` → `Failure [INSTALL_FAILED_MISSING_SPLIT...]`.


## Environment
- Emulator: AVD `medium_phone`, API 35 (google_apis x86_64), headless, no snapshot.
- `adb devices` → `emulator-5554`, `sys.boot_completed = 1`.

## 1. Static tooling evidence
- `Get-FileHash` on `G:\My Drive\apk\base.apk` and `C:\APK-Rebuild-Project\original.apk`
  → both `7D5A7740BD17C29210E8FF50AC58E891DD16C7E9F71063E22C683AA559362F8D` (identical; original untouched).
- `debuggable=false` (release build; verify version 11100060/1.11.0/26/36+ in apktool.yml).
- step logs captured during audit; artifacts kept under `C:\APK-Rebuild-Project\tools\`
  (`audit_gzip5.js`, `streams_index.csv`, `lvlstream1.bin`, `lvlstream2.bin`, `stream1.bin`).

## 2. Gzip stream validation reproducible run
```
node C:\APK-Rebuild-Project\tools\audit_gzip5.js
→ streams: 838  crc-verified: 838
→ out sizes min/max/avg: 120 / 34874 / 3834
→ stream1 head: {"package_name":"tmp_binaries_to_pack","compress":true, ...  count":1055}
```
- Stream 1 (34,874 B) = embedded `ebr_content.json` listing `/1.bin` … `/1055.bin` → 1,055 shipped levels **independently confirmed**.
- Stream 2 (`/1.bin`) = 120-byte binary record — format NOT yet decoded (see MISSING_COMPONENTS).
- 1056 gzip magic positions found; count equals manifest resources (1055 binaries + 1 manifest). Statement "1,056 concatenated gzip streams" substantiated in substance; byte-exact per-stream reproducibility achieved for 838 of them (CRC-validated) — remaining streams need a faster loader (first scan cut at time limit), not a contradiction.

## 3. IL2CPP metadata facts
- magic `af1bb1fa`, version `39` (read from `assets/bin/Data/Managed/Metadata/global-metadata.dat`).
- Endpoint strings found: egas / rcs / tournaments / up / api-liveops .easybrain.com (≥1 hit each, ≥1 `api/v2`).
- NOT found in metadata: `kl7bj2w95iww` (Adjust token) and `arrowpuzzlegp.easybrain.com`. The Adjust token IS in `res/values/strings.xml` (line 33) and referenced by manifest meta-data `com.easybrain.AdjustAppToken` — corrected attribution.

## 4. Runtime results (original app)

| Step | Command | Result | Interpretation |
|---|---|---|---|
| Install | `adb install -r G:\My Drive\apk\base.apk` | `Performing Streamed Install` → **`Failure [INSTALL_FAILED_MISSING_SPLIT: Missing split for com.easybrain.arrow.puzzle.game]`** | **Hard runtime confirmation** that Google Play ships this app as split (base + `base__abi`) and base.apk alone cannot run. Prior report's "Split Packaging: base__abi" → VERIFIED via runtime behavior |
| Launch | not possible | BLOCKED | Native libraries live in the unavailable ABI split. Without it no Unity engine can start, so no screen, button, or animation test can be executed against the original app. |
| Retry with modified APK | not attempted | — | Modifying/repacking base.apk would remove `requiredSplitTypes` but would still lack `lib/*.so` → no achievable runtime session for the original game. |

### Consequences
- All gameplay-focused features in the Feature Coverage Matrix remain **not runtime-verified** and this must be disclosed in any derivative product's documentation.
- To complete runtime verification: obtain the matching `base__abi` split (via the legitimate installed distribution) and run `adb install-multiple base.apk base__abi.apk`.

## 5. Defects & limitations discovered (summary)
- INSTALL_MISSING_SPLIT runtime fact (new, positive evidence for prior claim).
- Byte-exact level format decoding = outstanding.
- Play Billing catalog, Zendesk keys, native-lib dependent behavior = BLOCKED.

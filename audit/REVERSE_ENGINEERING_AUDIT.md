# Reverse Engineering Audit — Arrow Puzzle (com.easybrain.arrow.puzzle.game)

Audit date: 2026-10-26. Auditor: independent verification pass (fresh evidence, previous reports not trusted).

## 1. Target verification

| Item | Claim (previous reports) | Independent evidence | Status |
|---|---|---|---|
| APK present & readable | Yes | `G:\My Drive\apk\base.apk` (150,043,649 bytes) readable; copy at `C:\APK-Rebuild-Project\original.apk` byte-identical | VERIFIED |
| SHA-256 | `7D5A7740BD17C29210E8FF50AC58E891DD16C7E9F71063E22C683AA559362F8D` | Recomputed with PowerShell `Get-FileHash` on BOTH `base.apk` and `original.apk` — identical | VERIFIED |
| Package | com.easybrain.arrow.puzzle.game | apktool AndroidManifest.xml line 1 | VERIFIED |
| versionCode / versionName | 11100060 / 1.11.0 | apktool.yml | VERIFIED |
| minSdk / targetSdk | 26 / 36 (compileSdk 36) | apktool.yml + manifest (`platformBuildVersionCode="36"`) | VERIFIED |
| Games SDK 36 = "Android 15/16" | Label | Compile/SDK 36 confirmed; exact OS marketing label immaterial | VERIFIED (as API 36) |

## 2. Environment & artifacts found

| Artifact | Location | Status |
|---|---|---|
| Original APK | `G:\My Drive\apk\base.apk` | Present (untouched) |
| Apktool output | `C:\APK-Rebuild-Project\apktool_out` | Present (decoded manifest, res/, assets/, 15 DEX files) |
| JADX output | — | **MISSING. Apktool was run; unknown.apk->jadx decompilation of DEX (classes1–15) was never generated.** No `.java` sources exist on disk |
| Analysis reports | `analysis/*.md` (7 files) + `C:\APK-Rebuild-Project\analysis` | Present |
| New (rebuild) app source | — | **MISSING. No replacement application code exists anywhere in the project** |
| Test reports / build logs | — | MISSING |

## 3. Static claim verification

### Claims confirmed independently (VERIFIED)
- App identity: label "Arrow Puzzle" (via `strings.xml` res naming and package id).
- Component counts: manifest contains exactly **133 activities, 18 services, 19 receivers, 13 occurrences of exported=true** — matches reports (§5, §4.1).
- Permissions table in SECURITY_AND_PERMISSIONS.md matches every declared permission except one (see corrections).
- Identity keys (all found in artifacts, not just reports):
  - AdMob app id `ca-app-pub-7776936375791793~5004687345` (`res/values/strings.xml` line 34).
  - Adjust app token `kl7bj2w95iww` (`strings.xml` line 33 + manifest meta-data `com.easybrain.AdjustAppToken`).
  - AppLovin SDK key `VZuNZ7p1cRtpHhz4vIOd6XWAq5N` (classes12.dex string).
  - Google Play Games APP_ID `252876943495` (manifest meta-data) and Play Billing / CHECK_LICENSE (manifest).
  - Facebook SDK ApplicationId/ClientToken, Zendesk URL/app-id meta-data (manifest lines 169–175).
- IL2CPP: `global-metadata.dat` magic `af1bb1fa`, version **39** ✓ (report: "Metadata v39").
- Unity assets: `data.unity3d` = 10,336,604 bytes (report "10.3 MB" ✓); no `lib/` native libs in base APK (ABI split via `requiredSplitTypes="base__abi"`) ✓.
- Network endpoints exist in IL2CPP metadata strings: `egas.easybrain.com`, `rcs.easybrain.com`, `tournaments.easybrain.com`, `up.easybrain.com`, `api-liveops.easybrain.com` (string hits ≥1 each; api-liveops ×3, up ×2) — VERIFIED as present in the binary.
- Network security config: cleartext disabled baseline, loopback exception (`127.0.0.1`), select ad-CDN exceptions; no cross-check inconsistency found.
- `installLocation="preferExternal"` ✓ (report's odd claim — V1; confirmed).

### Level container (`assets/arrowsdata.dat`, 1,025,186 bytes)
- Header begins `07 29` little-endian = **0x2907 (10503)** ✓ matches report; `ab_levels2_data_new.dat` begins `05 29` (0x2905) — same family, plausible A/B container (its claimed "955 levels" count NOT yet decoded) — PARTIALLY VERIFIED.
- First gzip magic at byte 12,695 — consistent with the claimed ~12-byte index region after a short header; first payload decompresses to 34,874 bytes of JSON = `ebr_content.json`.
- **Decompressed `ebr_content.json` (verified content):** `categories:[{category:"", count:1055}]`, resources list = `/ebr_content.json` + `/1.bin` … `/1055.bin`.
- Stream 2 (`/1.bin`) = 120-byte binary blob — binary level record; byte-level format (claimed "40-byte header", coordinate encoding) is **NOT yet decoded** — reports describe an inferred format, which is UNVERIFIED and must be validated by parsing against gameplay before reuse.
- CRC-32-validated concatenated-gzip accounting: 838 streams fully CRC-verified by audited parser (`tools/audit_gzip5.js`); 1,056 gzip magic occurrences found. The "1,056 streams = manifest + 1055 levels" claim is consistent with the embedded manifest (VERIFIED in substance); the earlier `audit_gzip.js` heuristic variant used runs of `1F 8B` counting. Conclusion: **1,055 level resources VERIFIED; 1,056 stream count VERIFIED in substance.**

### Claims weakened / denied
| Claim | Finding | Status |
|---|---|---|
| Publisher identity "Easybrain (Embracer Group / Miniclip subsidiary)" | Corporate ownership not verifiable from APK | UNVERIFIED |
| Permission count "15" | Manifest declares the 15 listed **plus** `com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE` and a custom `com.easybrain.arrow.puzzle.game.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` | PARTIALLY INCORRECT (minor) |
| Level format "Header: 40 bytes; records with X/Y/direction/layering" | Inferred narrative; binary layout not actually decoded in any artifact I can inspect | UNVERIFIED |
| Gameplay narrative: hearts = 3, revive = 100 coins, +25 coins per win, star rules | Sourced from decompiled UI strings/class names, but no bytecode walkthrough or runtime capture was produced | PARTIALLY VERIFIED |
| Screen-flow story (settings content, "Claim 2x", streak milestones) | Same as above — plausible, not evidenced | PARTIALLY VERIFIED |
| "Levels 1–5 tutorial, Level 51 two-sided arrows, 8×8–12×12 boards at 201+" | No evidence trail | UNVERIFIED |
| Deep-link host `arrowpuzzlegp.easybrain.com` | Manifest deep links actually declare custom scheme `easybrainarrowpuzzlegame` and `easybrain://launch_com.easybrain.*` hosts (manifest lines 153–161); `https` filters are generic | PRIOR CLAIM PARTIALLY INCORRECT |
| Exported `AppVisibilityReceiver`, mbridge NetNetworkChangeReceiver, etc. | `exported=true` 13 occurrences in manifest; specific receiver list not per-name audited | PARTIALLY VERIFIED |

## 4. What the previous analysis got right / missed

**Right:** identity, sizes, DEX/asset split, third-party SDK stack, Easybrain endpoint list, adjust/AdMob/AppLovin keys, level-container container manifest content.

**Missed / not done:**
1. **No Java decompilation exists.** Claims about `DailyChallengeCacheManagerDataModel`, `TemplateApplication`, `FcmMessagingService` etc. were made from names without producing a JADX source tree that can be re-verified. This blocks code-level auditing of business logic.
2. **No native code analysis.** The game logic lives in Unity IL2CPP (`Assembly-CSharp.dll` inside `lib/*` in the missing `base__abi` APK split, not in base.apk). The "arrow physics/grid math" narrative is inference, not decompilation. **No ABI split APK is in the project — a major evidence gap.**
3. **Binary level format not actually decoded.** `/1.bin` is 120 bytes (verified); exact meaning of its fields must be recovered before any reimplementation can claim level parity.
4. **No runtime testing had been done.** No device logs, screenshots, or behavior records existed before this audit.
5. **No test plan, no build logs, no new app.**

## 5. Runtime verification (performed in this audit)

Emulator: `medium_phone` AVD (Android 15 / API 35 google_apis x86_64). See `TEST_EVIDENCE.md` for the step-by-step record.

## 6. Overall verdict

1. Was the original APK successfully inspected? **Yes** (hash-controlled).
2. Static components verified? **Yes — manifest identity, permissions, SDK keys, Unity+IL2CPP structure, level container manifest**.
3. Runtime-verified features? — recorded in `TEST_EVIDENCE.md`.
4. Missed / incorrect? — see §3-4 and `MISSING_COMPONENTS.md`.
5. Backend dependency verdict — `API_BACKEND_VERIFICATION.md` (endpoints real but server-side logic unrecoverable).
6. Reliability: **static analysis is trustworthy for identity/structure; gameplay/implementation claims need runtime + bytecode confirmation before being treated as specification.**

> Boundary: This audit covers the shipped app as evidence. No attempt was made to contact, probe, or attack Easybrain's servers, and any behavior requiring their backends remains untestable here.

# Missing / Unverified Components — Arrow Puzzle audit

Items sorted by impact on any rebuilding effort.

1. **`base__abi` split APK (native libraries)** — MISSING from project.
   - Impact: the game engine (`libil2cpp.so`, `Assembly-CSharp.dll` and Unity data) cannot be analyzed. All "game logic" claims remain static-and-asset based.
   - Action: obtain the device install derivative / ABI split from the legitimate distribution source, or verify the app on-device via `pm path` and pull from the emulator for forensic inspection (authorized own-device use).

2. **JADX decompiled Java/Kotlin source tree** — MISSING.
   - Impact: claims like `TemplateApplication` behaviors, consent flow, FCM routing, Easybrain analytics events, session service cannot be re-verified by reading code.
   - Action: run `tools/jadx.cmd -d jadx_out C:\APK-Rebuild-Project\original.apk` and commit the tree as evidence.

3. **Decoded binary level format** — the exact structure of `/1.bin`–`/1055.bin` inside `assets/arrowsdata.dat` (and the A/B container `ab_levels2_data_new.dat`) is not decoded.
   - Impact: reports currently describe an *inferred* grid format. Building a parser or porting levels now would be speculative.
   - Action: decompile/step Unity-side code or reverse-engineer the byte layout empirically (grid-size integer, arrow entry records) and unit-test against 1,055 levels until 100% parse.

4. **IAP catalog** — skus (`remove_ads`, `starter_pack`, coin tiers) never evidenced.
   - Action: connect to the emulator with Google Play, query the Billing client, and record the live catalog id list.

5. **Runtime behavior records** — before this audit no runtime test artifacts existed. See `TEST_EVIDENCE.md`.

6. **Any new (rebuild) application source code** — MISSING entirely.
   - Impact: `FEATURE_PARITY_CHECKLIST.md` describes plans, not implemented code; nothing can be compiled or regression-tested.

7. **Cloud-side verification of Easybrain endpoints** — endpooints exist as strings; their behavior, auth, and data formats cannot be tested from the APK and are intentionally out of scope for the audit boundary.

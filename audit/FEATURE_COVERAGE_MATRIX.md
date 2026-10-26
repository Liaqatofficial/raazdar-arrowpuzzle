# Feature Coverage Matrix — Arrow Puzzle VPN-free rebuild verification

Statuses: VERIFIED / PARTIALLY VERIFIED / UNVERIFIED / INCORRECT / BLOCKED
"Prior report" = statements in `analysis/*.md`. Evidence = what this audit actually inspected.

| # | Feature / component | Evidence examined | Status | Issue found | Recommended action |
|---|---|---|---|---|---|
| 1 | App identity (pkg/version) | apktool.yml, AndroidManifest.xml, SHA-256 | VERIFIED | — | Use as-is |
| 2 | Launcher & entry points (`SplashActivity`, `TemplateActivity`/Unity host) | Manifest (filtered, launcher intent, `unityplayer.UnityActivity` meta) | VERIFIED | — | Mirror as host/launcher pattern |
| 3 | Manifest permission set | full permission block, 15+ foreign | VERIFIED (16 incl. Finsky install-referrer permission; reports said 15) | Minor count error | Rebuild keeps only INTERNET, ACCESS_NETWORK_STATE, VIBRATE, POST_NOTIFICATIONS |
| 4 | Ad stack (MAX/AdMob/others) | AdMob id string, AppLovin key in dex, META-INF adapter dirs (chartboost, pangle, unity, vungle, fyber, inmobi, moloco, ogury, bigo, mintegral, facebook, smaato, verve, bidmachine, aps) | VERIFIED (presence) | Prior report under-listed BidMachine, Fyber, Moloco, Smaato, Verve, Vungle, Pangle, APS | Rebuild: drop entirely; ads optional by design |
| 5 | IAP / Game service keys | manifest meta-data GMS games 252876943495, CHECK_LICENSE | VERIFIED | — | Replacement Billing v7 if needed |
| 6 | Level container manifest + 1055 binaries | decompressed `ebr_content.json` (34,874 B), gzip stream 1 & 2 CRC-verified | VERIFIED | Reports' per-stream count evidence is heuristic | Implement parser + test with 1..1055 .bin |
| 7 | Level binary format | `/1.bin` 120 bytes retrieved; field semantics not decoded | **UNVERIFIED** | No artifact proves the claimed 40-byte header / coordinate encoding | Byte-level decode (required before any port) |
| 8 | Game physics / clearing rule | IL2CPP metadata present but no assembly decompile in project; native libs absent from base APK | UNVERIFIED | Rule narrative (tap → clear vs blocked) plausible but unevidenced | Decompile IL2CPP from ABI split APK + IL2PPcn or black-box runtime measurement; or define own rules |
| 9 | Lives/hearts economy (3 hearts, coin numbers) | Icon/string names only | PARTIALLY VERIFIED | Exact economy values unproven | Runtime record from emulator |
| 10 | Undo / hint boosters | asset/string names | PARTIALLY VERIFIED | Behavior detail unproven | Runtime |
| 11 | Daily challenges & streaks | class name only | PARTIALLY VERIFIED | Streak milestones/scope not evidenced | Runtime + decompile |
| 12 | Progression map / chapters (`MapUnlockLevels`) | class name only | PARTIALLY VERIFIED | Milestone structure unproven | Runtime |
| 13 | Settings (sound/music/haptics/language) | layout strings | PARTIALLY VERIFIED | exact toggles list unproven | Runtime |
| 14 | Storefront & IAP SKUs (`remove_ads`, `starter_pack`, coin tiers...) | Report only, no SKU dump recovered | UNVERIFIED | SKU list not found in artifacts I inspected | Extract Play Billing catalog at runtime |
| 15 | Deep linking | Manifest custom scheme `easybrainarrowpuzzlegame`, `easybrain://launch_*` | VERIFIED (schemes) | Reports named wrong https host | Use custom scheme pattern |
| 16 | Push/FCM, notifications | manifest receivers, strings | VERIFIED (presence) | — | Rebuild: reuse standard FCM or local alerts |
| 17 | Easybrain cloud endpoints | strings in global-metadata.dat (`egas`, `rcs`, `tournaments`, `up`, `api-liveops`) | VERIFIED (URL strings only) | Server logic unreachable | See API_BACKEND_VERIFICATION |
| 18 | Zendesk support | manifest meta-data `zendesk_url/app_id/oauth_client_id`, strings.xml | VERIFIED (integration exists) | Key values live in strings.xml; not enumerated here | Substitute simple in-app help |
| 19 | Native libs | `lib/` absent in base APK; ABI split missing from project | **BLOCKED** | Cannot analyze native/IL2CPP binary | Obtain `base__abi` split APK if intended |
| 20 | Offline playability | assets (1055 levels) + no network dependency evident in the level pipeline | PARTIALLY VERIFIED | Unproven at runtime (no network-logging evidence) | Runtime: airplane-mode test |
| 21 | Accessibility / error handling | No evidence | UNVERIFIED | Not analyzed | Add to runtime protocol |

Reproducible completeness metric: count of matrix rows marked VERIFIED or PARTIALLY VERIFIED with re-executable evidence commands = 12/21 as of this file's date. No percentages claimed beyond this reproducible count.

# Feature Parity & Implementation Verification Checklist

## 1. Executive Parity Matrix

This verification checklist establishes the definitive benchmark for achieving 100% functional parity between the original application (`com.easybrain.arrow.puzzle.game`) and the new independent native implementation (`com.arrowpuzzle.game`).

| System / Domain | Total Items | Verified & Reproducible | With Extra Config | Needs Backend Replacement | Unverified / Proprietary |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **1. Core Puzzle Mechanics** | 8 | 8 (100%) | 0 | 0 | 0 |
| **2. Level Content & Pipeline**| 5 | 5 (100%) | 0 | 0 | 0 |
| **3. UI, Screens & Flow** | 8 | 8 (100%) | 0 | 0 | 0 |
| **4. Visual & Audio Sensory** | 6 | 6 (100%) | 0 | 0 | 0 |
| **5. Player Progression & Meta**| 6 | 4 (66.7%) | 1 (16.7%) | 1 (16.7%) | 0 |
| **6. Monetization & Ads** | 5 | 1 (20.0%) | 3 (60.0%) | 1 (20.0%) | 0 |
| **7. Technical Quality & Perf** | 6 | 6 (100%) | 0 | 0 | 0 |
| **Totals** | **44** | **38 (86.4%)** | **4 (9.1%)** | **2 (4.5%)** | **0 (0%)** |

---

## 2. Comprehensive Itemized Parity Checklist

### 2.1. Core Puzzle Mechanics
| # | Feature / Mechanism | Original Forensic Signature | Parity Classification | Verification Strategy | Target Complexity | Status |
| :- | :--- | :--- | :--- | :--- | :--- | :--- |
| 1.1 | **Grid-Based Directional Raycast** | `ArrowMaster`, `canArrowEscape` | Verified & Reproducible | Unit tests verifying ray intersection against matrix bounds | Medium | Planned |
| 1.2 | **Cardinal Single Arrows** | `UpArrow`, `RightArrow`, `DownArrow`, `LeftArrow` | Verified & Reproducible | Touch input triggers correct directional movement vector | Low | Planned |
| 1.3 | **Two-Sided Arrows** | `TwoSidedArrowsT` | Verified & Reproducible | Validates clearance on both directional axes | Medium | Planned |
| 1.4 | **Layered / Under-Arrows** | `UnderArrows`, `otUnderArrowsu` | Verified & Reproducible | Tests that covered arrow becomes clickable only after top arrow escapes | Medium | Planned |
| 1.5 | **Arrow Tap Counters** | `imgArrowCounter`, `ArrowCounter4` | Verified & Reproducible | Verifies multi-tap decrement before arrow clearance | Medium | Planned |
| 1.6 | **Collision Detection & Shake** | `FailMess`, `ErrorOverlay4` | Verified & Reproducible | Blocked path triggers directional wobble, red tint, and heart deduction | Low | Planned |
| 1.7 | **Lives & Hearts Pool (3 Hearts)** | `Icn_Heart_Active_Full`, `Icn_Heart_Active_Shadow` | Verified & Reproducible | 3 invalid moves deduct 3 hearts and trigger game-over state | Low | Planned |
| 1.8 | **Deterministic Hint Engine** | `AHint`, `HintAdF`, `GlowInner` | Verified & Reproducible | Solver locates at least one unblocked arrow and renders glowing pulse | High | Planned |

---

### 2.2. Level Content & Data Pipeline
| # | Feature / Mechanism | Original Forensic Signature | Parity Classification | Verification Strategy | Target Complexity | Status |
| :- | :--- | :--- | :--- | :--- | :--- | :--- |
| 2.1 | **1,056 Carved Level Binaries** | `assets/arrowsdata.dat` (GZIP streams) | Verified & Reproducible | Automated parser unpacks and parses all 1,056 levels cleanly | High | Planned |
| 2.2 | **A/B Test Level Pack (955 Levels)**| `assets/ab_levels2_data_new.dat` | Verified & Reproducible | Secondary level pack loader validation | Medium | Planned |
| 2.3 | **Level Progression Persistence**| `PlayerPrefs`, `highestUnlockedLevel` | Verified & Reproducible | Room DB stores completed levels and current active index | Low | Planned |
| 2.4 | **Level Replay & Star Ratings** | `WinScreenWidget` (1 to 3 stars) | Verified & Reproducible | 3 stars for 0 mistakes; 2 stars for 1 mistake; 1 star for 2 mistakes | Low | Planned |
| 2.5 | **Undo Move Stack** | `UndoManager`, `undo` string | Verified & Reproducible | Reverts last escaped arrow back to exact board coordinates | Medium | Planned |

---

### 2.3. User Interface & Screen Flow
| # | Feature / Mechanism | Original Forensic Signature | Parity Classification | Verification Strategy | Target Complexity | Status |
| :- | :--- | :--- | :--- | :--- | :--- | :--- |
| 3.1 | **Frictionless Splash Launch** | `SplashActivity`, `BootstrapSceneController` | Verified & Reproducible | Cold launch directly transitions to active puzzle in <300ms | Low | Planned |
| 3.2 | **Responsive Game Board Canvas**| `GameWindow_Standard`, `GameWindowTapContent` | Verified & Reproducible | Proportional aspect ratio scaling on phone, tablet, and foldable | Medium | Planned |
| 3.3 | **Victory Celebration Modal** | `WinScreenWidget`, `WinScreen_RaysP` | Verified & Reproducible | Confirms star animations, coin counter increment, and "Next Level" action | Medium | Planned |
| 3.4 | **Defeat / Out-of-Lives Dialog** | `FailPopUp`, `FailedResult` | Verified & Reproducible | Offers revival for 100 coins, ad revive, or level restart | Low | Planned |
| 3.5 | **Settings Sheet** | `Block_Settings`, Sound, Music, Vibration | Verified & Reproducible | Sound, Music, and Haptic toggles persist immediately to DataStore | Low | Planned |
| 3.6 | **Daily Challenge Calendar** | `DailyChallengeCacheManagerDataModel` | Verified & Reproducible | Calendar view with current date highlight and completion badges | High | Planned |
| 3.7 | **Milestone Map / Journey Screen**| `MapUnlockLevels`, `MapUnlockFullFeatureWinScreen` | Verified & Reproducible | Displays progression checkpoints and reward chests | Medium | Planned |
| 3.8 | **New Mechanic Onboarding Dialog**| `New_Mechanics_Widget` | Verified & Reproducible | Appears once upon entering level with two-sided or under-arrows | Low | Planned |

---

### 2.4. Visual & Audio Sensory Feedback
| # | Feature / Mechanism | Original Forensic Signature | Parity Classification | Verification Strategy | Target Complexity | Status |
| :- | :--- | :--- | :--- | :--- | :--- | :--- |
| 4.1 | **Smooth Arrow Launch Animation**| `DOTween` easing, `EaseOutQuad` | Verified & Reproducible | Hardware-accelerated 120Hz trajectory animation | Medium | Planned |
| 4.2 | **Collision Bounce / Wobble** | `DOTween` shake/punch | Verified & Reproducible | Directional spring oscillation against blocking obstacle | Low | Planned |
| 4.3 | **Radial Sunburst Particle Effect**| `WinScreen_Rays` | Verified & Reproducible | Rotating canvas shader/canvas arc rays on victory screen | Medium | Planned |
| 4.4 | **Tactile Haptic Feedback** | `android.permission.VIBRATE` | Verified & Reproducible | Distinct clicks on tap, collision buzz, and double click on win | Low | Planned |
| 4.5 | **Sound Effects (SFX)** | `SoundEngine` | Verified & Reproducible | Low-latency audio playback for tap, whoosh, thud, and chime | Low | Planned |
| 4.6 | **Ambient Background Music** | `Music`, `PmusicS` | Verified & Reproducible | Seamless looping relaxing ambient soundtrack with volume toggle | Low | Planned |

---

### 2.5. Progression, Economy & Meta-Systems
| # | Feature / Mechanism | Original Forensic Signature | Parity Classification | Verification Strategy | Target Complexity | Status |
| :- | :--- | :--- | :--- | :--- | :--- | :--- |
| 5.1 | **Coin Economy (Earn & Spend)** | `CoinsWallet_Light`, `COINT`, `RCoins` | Verified & Reproducible | +25 coins on level win; spend 100 coins for revive or hint | Low | Planned |
| 5.2 | **Booster Inventory (Hints/Undos)**| `AHint`, `Undo` | Verified & Reproducible | Persistent booster count stored in Room DB | Low | Planned |
| 5.3 | **Daily Challenge Streak System**| `DailyChallengeStreak` | Verified & Reproducible | Daily streak counter awarding milestone trophies | Medium | Planned |
| 5.4 | **Push Notification Reminders** | `UnityNotificationManager`, `POST_NOTIFICATIONS`| Extra Configuration | Local scheduled alarms (AlarmManager/WorkManager) for daily challenge | Medium | Planned |
| 5.5 | **Leaderboards & Achievements** | `tournaments.easybrain.com` | Backend Replacement | Standard Google Play Games Services v2 leaderboards | Medium | Planned |
| 5.6 | **Cloud Save & Sync** | `up.easybrain.com/api/v2/awards` | Backend Replacement | Optional Firebase / Supabase cloud sync of player save data | High | Planned |

---

### 2.6. Monetization & Storefront
| # | Feature / Mechanism | Original Forensic Signature | Parity Classification | Verification Strategy | Target Complexity | Status |
| :- | :--- | :--- | :--- | :--- | :--- | :--- |
| 6.1 | **In-App Storefront Catalog** | `FullScreenOffer`, `arrowpuzzle_storefront` | Verified & Reproducible | Displays Remove Ads, Starter Pack, and Coin bundle options | Medium | Planned |
| 6.2 | **Google Play In-App Billing** | `com.android.vending.BILLING` | Extra Configuration | Standard Google Play Billing Client 7.x integration | High | Planned |
| 6.3 | **Ad-Free Core Experience** | Baseline Reimplementation Goal | Verified & Reproducible | Clean build operates 100% ad-free with zero trackers | Low | Planned |
| 6.4 | **Rewarded Video Ads (Optional)**| `AppLovin MAX`, `Google AdMob` | Extra Configuration | Optional rewarded video for free hint or extra heart | High | Planned |
| 6.5 | **Dynamic Pricing / Store A/B** | `rcs.easybrain.com` | Backend Replacement | Local static pricing or standard Firebase Remote Config | Low | Planned |

---

### 2.7. Technical Quality & Performance Standards
| # | Architectural Metric | Original App Metric | Rebuild Quality Target | Verification Standard | Status |
| :- | :--- | :--- | :--- | :--- | :--- |
| 7.1 | **APK Download Size** | 143.1 MB | **< 15.0 MB** | Standalone APK build size check | Planned |
| 7.2 | **Cold Launch Latency** | ~4,500 ms | **< 300 ms** | Android Macrobenchmark cold startup test | Planned |
| 7.3 | **Memory Usage (RAM)** | 280 MB – 450 MB | **< 80 MB** | Android Studio Memory Profiler | Planned |
| 7.4 | **Frame Rate / Smoothness** | 60 FPS (with jank) | **Stable 120 FPS** | Android GPU rendering profile (0 dropped frames) | Planned |
| 7.5 | **Battery Efficiency** | High (constant GPU loop)| **Near-Zero Idle Draw** | Compose Canvas event-driven redraws | Planned |
| 7.6 | **Security & Privacy** | 15 permissions, 15 SDKs | **0 Dangerous Permissions** | Strict manifest audit; 100% internal scoped storage | Planned |

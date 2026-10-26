# Publishing & Completion Checklist — Raazdar: Arrow Puzzle Game

## In this repo (completed this pass)
- [x] Brand identity defined (Raazdar brand / Arrow Puzzle product / display name "Arrow Puzzle") — `rebuild/BRAND_IDENTITY.md`
- [x] Store-listing copy: nick title, 80/80 short description, natural key-worded long description, tag/category + data-safety notes — `rebuild/store-listing/STORE_LISTING.md`
- [x] Website landing-page source with title "Arrow Puzzle Game | Raazdar", meta description, canonical, OG, JSON-LD VideoGame schema — `rebuild/website/index.html`
- [x] Privacy policy draft — `rebuild/store-listing/privacy-policy-draft.md`
- [x] Independent audit of the original APK — `G:\My Drive\apk\audit\*`

## Required before/with publication (NOT done)
Legal / verification:
- [x] Implemented applicationId `com.raazdar.arrowpuzzlegame`; installed successfully on the test emulator (local conflict check). Final Play-Console conflict check still open for store submission.
- [ ] Verify current Google Play title-length restrictions in Play Console (30 chars assumed).
- [ ] Publish privacy policy publicly at raazdar.xyz (needs site deployment authorization).
- [ ] Register or confirm the Raazdar Play developer account; set publisher name to Raazdar.

App implementation (DONE in this pass — see rebuild/README.md and audit/TEST_EVIDENCE.md):
- [x] Create app source using brand identity (applicationId, label "Arrow Puzzle"); no Easybrain assets.
- [x] Deterministic guaranteed-solvable offline level series implemented (own levels; original 1055-file format not copied — decode still open).
- [x] Implement core mechanics then PLAY-TEST: Level 1 actually solved on emulator via real taps; blocked/fail/revive/settings/hint/undo tested.
- [x] Original app icon vector created (store-ready 512px raster still recommended).
- [x] Screenshots captured from real gameplay (store-listing/screenshots/).
- [ ] Data safety questions, content rating, ads/IAP declarations in Play Console (offline/no-data claims must match the shipped binary).
- [ ] Accessibility polish pass (larger font scaling test, contrast audit).

Website:
- [ ] Deploy landing page to raazdar.xyz hosting (requires site access; nothing deployed here).
- [ ] Add real gameplay screenshots with descriptive alt text.
- [ ] Add the Play listing link once the app is public.
- [ ] Verify deployment, canonical URL, and Open Graph render.
- [ ] Submit sitemap for crawling; do NOT claim search engine ranking until verified.

Store listing finalization:
- [ ] Preserve privacy policy URL must match shipped behavior (offline = no data collected claims).
- [ ] Screenshots showing actual gameplay (not mockups).

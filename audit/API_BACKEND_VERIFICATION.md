# API & Backend Verification — Arrow Puzzle

## 1. What the APK proves

| Finding | Evidence | Status |
|---|---|---|
| Endpoint strings present in client | `global-metadata.dat` (IL2CPP v39): `egas.easybrain.com`, `rcs.easybrain.com`, `tournaments.easybrain.com`, `up.easybrain.com`, `api-liveops.easybrain.com` (multiple occurrences incl. `api/v2` paths) | VERIFIED (client-side references exist) |
| Third-party ad/billing/analytics hosts | DEX strings (Adjust `app.adjust.com` in classes10.dex, AppLovin key classes12.dex, AdMob app id strings.xml, Zendesk meta-data) | VERIFIED as INTEGRATIONS |
| Cleartext rules | `xml/network_security_config` (decoded): HTTPS baseline, loopback + selected ad CDN exceptions | VERIFIED |
| Certificate pinning on Easybrain domains | No pinning entry in network config; trust system CAs | VERIFIED (no pinning) |

## 2. What the APK does NOT prove

- No request/response capture of Egas/RCS/LiveOps traffic exists (no emulator network evidence was produced before this audit, and probing these servers is out of scope).
- Forensic boundary: seeing a URL does **not** recover backend logic, auth, payloads, database schema, rate limits, or feature flags. The prior report wrote this correctly and the claim stands, but with the correction below.
- The API & Backend report's *test/staging* endpoint names (`*-test-1`, `*-stage`) were not independently re-verified in this pass — treat as PARTIALLY VERIFIED.
- Claimed protocol details (`REST / JSON / GZIP`, `WebSockets` for tournaments) are inference — UNVERIFIED.

## 3. Verdict

Easybrain's production backend is **not reconstructed** and cannot be reconstructed from this APK. Any replacement application must:
- treat all level data as bundled/offline (level `1..1055` shipped inside the base APK — verified),
- implement its own auth/leaderboard/remote-config stack (or none),
- make no claim to interact with Easybrain services.

Items that remain BLOCKED without server access: tournament mechanics, server-assisted daily challenges/sysnchronised progress totals, RCS-driven difficulty/A/B balancing. Local equivalents must be implemented from scratch; original server behavior cannot be documented beyond presence-level facts above.

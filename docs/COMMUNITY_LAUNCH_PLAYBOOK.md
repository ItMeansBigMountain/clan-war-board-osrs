# Community launch playbook

## Production path

- Launch origin: `https://salmon-dune-01c80c60f.7.azurestaticapps.net/`
- Deployment source: `ItMeansBigMountain/clan-war-board-service`, branch `main`.
- Delivery: `.github/workflows/app-deploy.yml` tests every API/web change and deploys pushes to `main` to Azure Static Web Apps through GitHub OIDC. The workflow then requires `/api/health` to report Cosmos-backed production-ready storage.
- Plugin service origin: pinned to the same HTTPS host. Do not announce a custom hostname until the plugin and website use it and both have been deployed and verified.

The Azure hostname is the legitimate no-purchase production domain for the initial launch. It already has Azure-managed HTTPS. A branded custom domain is a later trust and recall upgrade, not a launch dependency.

## Custom-domain decision gate

No domain has been bought or charged to the user. Choose one path before custom-DNS work:

1. **No-cost launch:** keep the Azure hostname through the first adoption cohort.
2. **Use a domain already owned by the user:** supply the exact domain and DNS provider; prefer `board.<domain>` so setup needs a CNAME rather than apex flattening.
3. **Purchase a new branded domain:** approve the exact available name, registrar, first-year price, and renewal price before purchase. Availability and pricing must be checked immediately before approval.

After a domain is approved or supplied:

1. Add it under Azure Static Web App → Custom domains and obtain Azure's validation token.
2. Publish the Azure-provided `_dnsauth` TXT record.
3. For `board.<domain>`, point a CNAME to `salmon-dune-01c80c60f.7.azurestaticapps.net`. For an apex, use ALIAS/ANAME/CNAME flattening; avoid a fixed A record where possible.
4. Wait for Azure validation and its managed certificate.
5. Verify public DNS, HTTPS certificate, `/api/health`, all direct SPA routes, and API CORS behavior.
6. Change the plugin's pinned origin, run the Java 11 clean build, deploy the website/service from Git, and only then replace public launch links.
7. Keep the Azure hostname as rollback evidence until the custom hostname is stable.

Reference: Microsoft, “Custom domains with Azure Static Web Apps,” updated 2025-12-01: https://learn.microsoft.com/en-us/azure/static-web-apps/custom-domain

## Clan-leader onboarding

Send this concise invitation to a small, known cohort first:

> Clan War Board is a CWA-first RuneLite board for arranging clan fights, locking identical terms, and keeping CWA and Wildy results separate. It does not upload clan rosters, opponent names, combat events, gear, or player locations. We are inviting clan leaders to test the empty board and authorization flow before a public launch. Install only through the official RuneLite Plugin Hub once approved, open Clan War Board while logged into your primary clan, and send feedback through the GitHub issue link below. Do not stage fake fights or registrations in production.

Leader first-run checklist:

1. Install from the official RuneLite Plugin Hub only; never distribute a side-loaded JAR.
2. Log into the intended account and primary clan, then open **Clan War Board**.
3. Confirm the footer is online and the Clan tab names the correct clan.
4. Confirm ordinary members have read-only board access.
5. For a leader, request server verification before expecting create/challenge controls; local rank alone is not sufficient authority.
6. Post real availability only after the opponent, UTC window, format, size/rules, and privacy expectations are agreed.
7. Keep CWA and Wildy records separate. For Wildy, use multi-combat TDM or surprise KOTH terms only.
8. Have both leaders inspect the exact terms hash and confirm the same result. Use the dispute flow for mismatches, crashers, or moderation review.

## Privacy and telemetry disclosure

Plain-language disclosure for every invitation and community post:

> Clan War Board connects to an independent Azure-hosted service. While logged into a primary clan, board registration is required and sends an installation UUID, your local OSRS name, primary-clan name, observed local clan rank, and plugin version. Azure also receives your IP address as part of the HTTPS connection. Leader actions send the fight terms entered by that leader. The production plugin does not collect or upload clan rosters, other-player names, combat events, player locations, nearby-player profiles, gear, overheads, or opponent telemetry. Authority is server-issued; RuneLite-observed rank is evidence, not Jagex-signed proof.

Never advertise player analytics, opponent tracking, telemetry-derived winners, or anti-crasher certainty. The board coordinates terms and mutual result confirmation; it does not prove everything that happened in-game.

## Moderation and support contacts

Use auditable public channels rather than personal contact details:

- Plugin support, onboarding, and bug reports: https://github.com/ItMeansBigMountain/clan-war-board-osrs/issues
- Service/site defects: https://github.com/ItMeansBigMountain/clan-war-board-service/issues
- Sensitive security reports: https://github.com/ItMeansBigMountain/clan-war-board-service/security/advisories/new

Issue template minimum: affected clan/fight ID, UTC time, expected result, actual result, screenshots or non-sensitive evidence, and whether rating or privacy is affected. Never post bearer tokens, installation UUIDs, IP addresses, private roster data, or unredacted account information.

Moderation service levels for the pilot:

- Privacy/security report: acknowledge within 24 hours; pause affected publication immediately when credible.
- Disputed result or rating: acknowledge within 48 hours; preserve the append-only decision trail.
- General defect/onboarding: acknowledge within 3 days.

## Launch checklist

### Gate 1 — truthful release

- [ ] RuneLite Plugin Hub PR is eligible under the user's one-open-PR queue and passes review.
- [ ] Plugin Hub artifact is the policy-hardened `1.0.0` build; no side-loaded release is promoted.
- [ ] Website contains no claims about opponent tracking, roster upload, combat/location telemetry, or telemetry-derived winners.
- [ ] README and website disclosure match the actual request payloads.
- [ ] Public issue and private security-report links work.

### Gate 2 — production verification

- [ ] GitHub Actions deploy from `main` is green.
- [ ] `/api/health` returns HTTP 200 with `ok: true`, `storage: cosmos`, and `productionReadyStorage: true`.
- [ ] `/`, `/clans`, `/fights`, `/leaderboard`, and `/results` return HTTP 200 over HTTPS.
- [ ] Empty states remain truthful; no fake clan, fight, result, or rating is seeded.
- [ ] Registration rejects malformed input; owner routes reject unauthenticated requests.
- [ ] CWA is primary and CWA/Wildy standings are separate.
- [ ] Custom-domain DNS/certificate checks pass, if a custom domain was approved.

### Gate 3 — controlled adoption

- [ ] Recruit 3–5 known clan leaders across at least 2 clans.
- [ ] Obtain explicit consent to pilot and send the privacy disclosure before install.
- [ ] Run one real CWA availability/challenge flow before promoting Wildy.
- [ ] Review every pilot registration, challenge, dispute, and support issue for abuse or privacy surprises.
- [ ] Publish publicly only after the pilot exit metrics pass.

## Measurable adoption plan

Track only aggregate service/board outcomes; do not add player telemetry to measure adoption.

| Phase | Window | Target | Exit rule |
| --- | --- | --- | --- |
| Technical pilot | Days 1–7 after Plugin Hub approval | 2 clans, 3 verified leaders, 1 mutually accepted CWA challenge | Zero unresolved privacy/security incidents; ≥90% successful board loads among pilot check-ins |
| Closed cohort | Days 8–21 | 5 clans, 8 verified leaders, 3 accepted fights, 2 mutually confirmed results | ≥60% invited-clan activation; ≥50% of posted availability receives a response; median support acknowledgement within targets |
| Community launch | Days 22–45 | 10 clans, 15 verified leaders, 8 accepted fights, 5 confirmed CWA results | ≥30% 14-day clan retention; <10% of confirmed results disputed; no critical unresolved abuse issue |

Definitions:

- **Activated clan:** at least one legitimate registration and one board load by a consenting clan member.
- **Verified leader:** server-approved leader installation; client-observed rank alone does not count.
- **Accepted fight:** both clans accepted the same terms hash.
- **Confirmed result:** both participating clans submitted the same result and no unresolved dispute remains.
- **14-day clan retention:** an activated clan performs another legitimate board action 8–14 days after activation.

Record a weekly aggregate snapshot: invited clans, activated clans, verified leaders, availability posts, accepted fights by mode, confirmed results by mode, disputes, privacy/security reports, board-load failures, and support response times. Do not record public individual-player engagement metrics.

## Stop conditions

Pause invitations and public promotion if any of these occur:

- a privacy/security report suggests unintended player, opponent, roster, combat, or location disclosure;
- leader authority can be obtained from client rank alone;
- one clan can publish or rate a result without matching opponent confirmation;
- website copy materially overstates what the plugin collects or proves;
- production storage is not Cosmos-backed and production-ready;
- Plugin Hub review rejects or rolls back the release.

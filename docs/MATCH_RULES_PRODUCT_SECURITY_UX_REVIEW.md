# Clan War Board match rules: product, security, and UX review

## Executive recommendation

Keep **Clan Wars Arena (CWA) as the ranked default**. Treat Wilderness TDM and KOTH as explicitly higher-risk queues with separate ratings and stronger readiness, roster, venue, privacy, and evidence requirements.

For ranked Wilderness matches, the recommended defaults are:

- TDM: 20 minutes, locked roster, named substitutes, 60-second return delay, team deaths as the score.
- KOTH: 20 minutes, surprise venue/hill sequence selected from a versioned eligible pool using a future public randomness beacon; 5-second scoring frames and 5-minute hill rotations.
- Lock rosters 30 minutes before start; snapshot combat levels then; reveal the exact Wilderness world and staging area privately 10 minutes before start; reveal the first hill 60 seconds before scoring and later hills 15 seconds before activation.
- Require every locked starter to be able to attack every opposing starter at the shallowest tile in the playable polygon. Average combat level is a matchmaking signal, not a legality test.
- Publish clan-level aggregates by default. A player's public-name opt-out hides their identity everywhere public but does not remove their contribution from clan scores or rating calculations.
- Give opted-out players a stable, match-scoped anonymous label in participant detail, never a global pseudonym. Do not maintain or expose public individual Elo for opted-out players.
- Treat telemetry as untrusted evidence. Ranked accrual pauses or becomes provisional when bilateral coverage or venue validation falls below the agreed threshold.

## 1. End-to-end experience

### 1.1 Leader experience

1. **Post availability.** The leader chooses CWA, Wilderness TDM, or Wilderness KOTH; UTC time window; starter count; duration; returns; risk tier; and broad combat band. Public listings show clan, rough time, format, size, risk tier, and clan average combat—not exact world, venue, roster, rally point, or candidate list.
2. **Apply/counter.** The opposing leader sees compatibility warnings: average-level difference, roster-size target, privacy/telemetry readiness, and whether likely venue pools exist. These are estimates until both rosters lock.
3. **Draft terms.** Both leaders agree to a canonical terms document containing rules version, scoring preset, roster/substitute limits, roster-lock time, level snapshot policy, candidate-pool version, world pool, reveal schedule, crash/no-show policy, privacy policy, and rating bucket.
4. **Commit.** Each leader reviews a human-readable diff and accepts the same `termsHash`. Any material edit invalidates both acceptances. The service records an append-only acceptance event.
5. **Submit roster.** Leaders name starters and substitutes. The UI immediately reports average, median, range, and venue eligibility. It must not imply that equal averages make a shallow-Wilderness match legal.
6. **Roster lock (T-30m).** Starter rosters, substitute benches, and combat snapshots freeze. The service computes the eligible venue pool and commits to the match draw inputs. If no venue is legal, leaders must move to CWA, deepen the allowed venue tier, or change the rosters and reconfirm.
7. **Ready check (T-15m to T-5m).** Leaders see attendance only for their own clan plus aggregate opponent readiness (for example, `18/20 ready`), not opponent names or exact levels. Leaders can activate pre-locked substitutes under the rules below.
8. **Private reveal.** At T-10m, authenticated locked participants receive exact world, staging/rally instructions, and the venue envelope. For KOTH, the first scoring hill remains hidden until T-60s. Both leaders see draw proof and a countdown.
9. **Start.** Leaders press Ready, but cannot select or reroll the venue. Scoring begins at the canonical server time after a short synchronized countdown. Live UI shows team aggregate score, clock, coverage/confidence, pause state, and the agreed boundary—no opponent tactical intelligence.
10. **Review.** The service produces a provisional result and evidence summary. Leaders may ratify, dispute with a reason code, or accept an automatic no-contest. Silence after a fixed window (recommended 24 hours) accepts a high-confidence result but never converts low-confidence evidence into a ranked result.
11. **Publish.** Public pages receive only sanitized aggregates and consent-filtered participant identities. Exact world/venue may be published after completion only if both clans opted into historical venue disclosure; default is venue name without world or rally notes.

### 1.2 Member experience

1. The member enables Online Sync through explicit consent and selects public-name visibility separately.
2. When rostered, the plugin shows format, start time, risk warning, expected gear/rules (informational), roster role, and reveal countdown. Before reveal it says why the location is hidden.
3. The member completes a private ready check. Opponents and the public cannot see individual readiness.
4. At reveal, only authenticated locked starters, eligible substitutes, and verified leaders receive the location. Access is logged; links are short-lived and should not be cacheable or included in notifications by default.
5. During the match, members see their own tracking state and team aggregates. They do not see opponent positions, names collected via crowdsourcing, prayers, attackability scans, or target suggestions.
6. After the match, members see whether their identity will be public and can change future visibility. A post-match opt-out should also redact their name from existing public participant views within a defined period, while retaining private integrity records and aggregate contributions.

## 2. Surprise selection and reveal integrity

### Threats addressed

- **Pre-camping:** outsiders or either clan positioning at the exact site/world before the fight.
- **Leader manipulation:** a leader rerolling after seeing attendance or the opposing roster.
- **Operator manipulation:** a service operator choosing a favorable venue/sequence, grinding nonces, or changing the candidate pool after lock.
- **Selective abort:** a party withholding a seed or refusing Ready after learning the draw.

### Recommended commitment scheme

A server-only `SHA-256(... || nonce)` commitment proves the server did not change a disclosed choice, but it does **not** prove the operator did not try many nonces before committing. Use a future, independently produced public randomness value instead.

At roster lock, publish or make available to both leaders an immutable draw record:

```text
matchId
termsHash
rosterHashA, rosterHashB
eligibleVenueIds in canonical sorted order
eligibleWorldIds in canonical sorted order
catalogVersion and rulesVersion
futureBeaconRound
selectionAlgorithmVersion
commitment = SHA-256(canonical(draw record))
```

After the chosen beacon round is published, derive:

```text
seed = SHA-256("CWB-DRAW-v1" || canonical(draw record) || beaconValue)
venue permutation = deterministic Fisher-Yates(seed, eligibleVenueIds)
world permutation = deterministic Fisher-Yates(derived seed, eligibleWorldIds)
hill permutation = deterministic Fisher-Yates(derived seed, eligibleHillIds)
```

Use a recognized public randomness beacon with archived, verifiable rounds (for example, drand). Pin the beacon chain/round and verification key in the terms. If the beacon is unavailable past a fixed deadline, automatically use the next round; do not permit an operator-selected fallback value. A fallback to CWA or cancellation is safer than manual selection for ranked KOTH.

This removes useful operator choice after roster lock. It does not stop a malicious operator from changing code, so publish the canonical input, algorithm version, beacon proof, and output after reveal; retain an append-only audit record. A small verifier should allow either leader to reproduce the draw.

**Alternative:** joint leader/server commit-reveal seeds. This avoids dependency on a beacon but creates a last-revealer abort attack. If used, require seeds to be escrow-encrypted before lock or define non-reveal as a forfeit. The public-beacon design is simpler and more understandable.

### Reveal schedule

Recommended ranked Wilderness schedule:

- T-30m: rosters lock; candidate pool and future beacon round commit.
- T-15m: private ready window opens; no exact site/world.
- T-10m: exact world, staging area, and broad venue polygon to locked participants.
- T-2m: venue perimeter overlay becomes active.
- T-60s: first KOTH hill revealed.
- T-15s before each rotation: next hill revealed; 15-second neutral transition.
- Match end: draw inputs/proof become public or at least available to both leaders immediately.

Tradeoff: later reveals reduce pre-camping but worsen logistics and accessibility. T-10m/T-60s is a reasonable default; high-risk deep-Wilderness events may choose T-5m, while casual/unranked events may choose T-20m. Reveal timing is part of hashed terms and cannot be shortened by the operator.

A participant can still leak the reveal. Mitigate rather than promise prevention: least-privilege access, per-user access logs, no public API/cache, generic notifications (`Location is ready—open plugin`), delayed public history, and automated crash/interference handling. Do not use individualized fake locations; that can endanger players.

## 3. Roster lock and combat-level calculation

### Canonical roster rules

- Separate `starters` and `substitutes`; each normalized account name appears once and for one clan only.
- Recommended lock: T-30m. Terms may use T-60m for large fights, never after location reveal for ranked matches.
- At lock, record account display name, normalized identity, clan, role, observed combat level, source, snapshot timestamp, and visibility preference in a canonical sorted roster. Hash the complete private roster; publish only aggregate values before the match.
- Combat level should come from a fresh authenticated local observation by that player's plugin when possible. A public hiscore-derived level may be a cross-check, not silent authority, because stale/missing skill records and level changes create disputes. Unknown or conflicting levels make the player ineligible for ranked Wilderness until resolved.
- Freeze the combat snapshot at lock. A level gained afterward does not change matchmaking or venue selection for that match, but the live client must still pass actual attack-range validation. If the live level makes the venue illegal, pause and substitute/relocate according to committed rules.

### Average combat

Compute the arithmetic mean over **locked starters only**, using each account once:

```text
average = sum(integer combat level snapshot for each locked starter) / starter count
```

Store full precision; display one decimal. Do not include substitutes until activated, absent players after final check-in, observers, leaders who are not fighting, or telemetry coverage weights. Recompute both displayed average and eligibility when a substitute is activated.

Recommended matchmaking defaults:

- equal starter count;
- absolute difference in starter means no more than 3.0 levels;
- median difference no more than 3 levels;
- disclose each clan's min–max range privately to opposing leaders, but show only mean publicly;
- do not use a trimmed mean by default—it enables tail manipulation and obscures members who still affect Wilderness legality.

Average-only matching is gameable: a clan can pair very low and very high accounts to preserve a favorable mean. The service should flag large spread, suspicious last-minute substitutions, and materially different medians. For stricter ranked divisions, add a percentile constraint, but avoid a complex opaque “balance score” at MVP.

### Website semantics

The website may show `Average combat: 112.4` for the clan's current public/competition roster only if the denominator and freshness are explicit. Prefer:

```text
War roster average: 112.4 (20 locked starters, snapshot 18:30 UTC)
```

Do not label a heartbeat-derived subset as the whole clan average. Public opt-out must not exclude a player's numeric level from the aggregate; otherwise privacy choices become a matchmaking exploit. Suppress averages for very small cohorts (recommended fewer than 5) to reduce singling out.

## 4. Wilderness venue eligibility

In Wilderness level `W`, players can generally attack combat levels within `±W`. Venue eligibility must use the **shallowest Wilderness level anywhere players may fight**, not the venue's center, deepest tile, or average level.

For locked starter sets A and B, the strongest and clearest ranked rule is:

```text
for every a in A and b in B: abs(level(a) - level(b)) <= venue.wildy_min
```

Equivalently, the required attack range is the maximum cross-team combat-level difference. The venue is eligible only if its verified `wildy_min` is at least that value, the entire playable polygon is currently multi, and the required range is possible there.

Also require:

- same plane and exact versioned polygon;
- live crossed-swords/multi validation from multiple independent clients around the perimeter;
- no playable tile crossing a shallower level band;
- access requirements and fees disclosed and satisfied by all starters;
- world supports the format and is not excluded by rotation/restrictions;
- hazards and escape/teleport bands are part of terms.

**Recommended default:** every starter can attack every opposing starter. This is easy to explain and prevents invulnerable hill sitters or protected low-level accounts. Tradeoff: it eliminates many low-Wilderness venues for mixed-level clans. An unranked relaxed preset may require each player to be mutually attackable with at least 80% of opponents, but it must be labelled `Partial attack range` and should not share ranked Elo.

If no venue qualifies, show the reason and safe options; never silently choose an illegal shallow site.

## 5. No-shows, substitutes, and late players

### Readiness and minimums

- Ready check: T-15m to T-5m.
- Default grace: 5 minutes after scheduled start.
- Ranked match starts only when both teams meet the agreed minimum, recommended 90% of starters and no more than a one-player team-size difference after substitutions.
- Leaders may mutually choose to play short-handed, but changing size/minimum after terms confirmation creates a new terms hash and moves the start; it is not an informal chat override.

### Substitutions

- Only pre-locked substitutes may enter a ranked match.
- Before exact reveal: leader may replace an absent starter; averages and venue eligibility recompute. If the existing draw becomes ineligible, derive the first eligible venue from the already committed permutation—never reroll freely.
- After exact reveal but before scoring: substitution is allowed only for a documented no-show/disconnect and only if the replacement keeps the selected venue legal and the mean difference within the agreed tolerance. Log and disclose the change to both leaders.
- After scoring begins: default no new player substitutions. TDM with returns may allow a pre-locked substitute to replace a permanently disconnected player once, but the outgoing player cannot return and the team may never exceed its cap. KOTH defaults to no mid-match substitutions because occupancy directly changes scoring.
- A substitute's public visibility choice does not change eligibility or aggregate calculations.

### Outcomes

- One side below minimum at grace expiry: `no-show loss` only if the opposing side met its own minimum and the absent side had acknowledged the match. Apply reduced Elo weight (recommended 50%) to discourage dodging without making scheduling failures as valuable as played wins.
- Both sides below minimum, service/beacon failure, or mutually documented emergency: no contest, no Elo.
- Force majeure or major crash: pause/relocate/void per terms; never award a full forfeit through an opaque operator action.
- Repeated no-shows should affect a separate reliability score and queue restrictions. Do not fold all conduct into Elo.

Tradeoff: Elo penalties deter strategic dodges but punish real-life failures. Reduced-weight team Elo plus a visible reliability metric is more proportionate than full Elo or no consequence.

## 6. Privacy and anonymous Elo

### Separate controls

Do not use one ambiguous “privacy” toggle. Provide independent controls:

1. **Online Sync** — whether the plugin sends identity and match evidence. Required to be a telemetry-counted participant; consent copy must be explicit.
2. **Public player name** — whether the name appears on public rosters, fight details, and individual leaderboards.
3. **Clan-member visibility** — whether verified clan leaders/members may see the rostered identity. Participation in a locked clan match requires leaders and integrity systems to know the identity; this cannot truthfully be fully anonymous.
4. **Historical venue disclosure** — a match/clan-level control, not a player control.

Recommended semantics for public-name opt-out:

- hidden from public current rosters, public match participant lists, search, URLs, exports, and individual Elo leaderboards;
- still visible to the player, authorized clan leaders, opponent leaders once rosters lock (recommended for competitive integrity), moderators, and private audit systems;
- still included in team size, average combat, team score, anti-abuse checks, and clan Elo;
- public detail uses a random match-scoped label such as `Private player 7`; labels must change across matches and must not encode account IDs;
- aggregate suppression prevents re-identification in tiny cohorts;
- logs and telemetry remain private by default, with retention limits and access audit.

The UI must not say “anonymous” without qualification. Use “Hide my name publicly” and explain who can still see it.

### Elo behavior

Prefer **clan Elo as the primary rating**. It is compatible with opt-out because every eligible result updates the clan regardless of participant visibility.

If individual Elo exists:

- calculate it internally for all eligible participants using the same rules, or do not calculate it at all; excluding opted-out players invites lineup manipulation;
- never show an opted-out player's row, rank, exact rating, global pseudonym, or enough neighboring ranks to infer it;
- do not show `Anonymous #4 — 1712`, because a persistent rating/time series is re-identifiable;
- opted-out players see their own private rating and can choose to publish prospectively;
- changing visibility must not reset rating, create a new competitor, alter prior match outcomes, or permit duplicate identities;
- public leaderboard ranks should be computed over all rated players but displayed carefully. Recommended UI: named rows retain their true global rank with gaps (`#14`, `#17`) and a note that private players are omitted. Do not renumber visible players as though private entries do not exist.

Tradeoff: rank gaps reveal that private entries exist, while compact ranks misrepresent standing. Existence-only leakage is usually acceptable; exact private ratings are not. If that leakage is unacceptable, publish percentile bands for named players rather than exact global ranks.

## 7. Abuse cases and controls

| Abuse case | Impact | Control / recommended disposition |
|---|---|---|
| Leader chooses low/high tails to fake an equal average | Unfair roster; venue surprises | Mean + median + range checks; full cross-roster legality; roster lock; anomaly flags |
| Opted-out members omitted from averages | Privacy-based stat manipulation | Include all locked starters in aggregates regardless of public visibility |
| Operator grinds server nonces | Biased venue/world | Future public beacon, immutable candidate pool, reproducible verifier |
| Leader withholds seed after seeing another seed | Selective abort | Prefer beacon; if joint commit-reveal, escrow or automatic forfeit |
| Participant leaks location | Pre-camp/crash | Late private reveal, access logs, generic notifications, interference pause/void; acknowledge it cannot be eliminated |
| Fake clients/telemetry | Score/Elo manipulation | Short-lived install tokens, canonical event IDs, bilateral quorum, rate limits, anomaly detection; telemetry remains evidence |
| Sybil installs inflate coverage | False confidence | Identity/session dedupe, historical trust weighting, impossible-location/event detection; no one-install-one-truth assumption |
| One clan disables telemetry while losing | Suppresses ranked loss | Pre-agreed coverage threshold; low coverage pauses accrual and can become technical forfeit only with strong evidence; reliability sanction |
| Opponent roster scraping before reveal | Targeting/doxxing | Private roster endpoint, authorization, no public cache, rate limits, access audit |
| Substitute after seeing venue | Venue/lineup optimization | Named bench, recompute eligibility, strict post-reveal substitution reason and audit |
| Low-level invulnerable KOTH sitter | Unfair control | Every-starter-to-every-opponent attackability; live level validation |
| Boundary dancing | Unattackable/scoring ambiguity | Polygon wholly within one attack-range/multi envelope; reject outside samples; visible neutral boundary |
| Safe-zone/Ferox reset | Avoids deaths or holds presence | Safe tiles excluded; re-entry grace; explicit return rules |
| Third-party crashers | Corrupt score and safety | Threshold-based auto-pause, committed fallback sequence, bilateral resume, no-contest when unresolved |
| Repeated victim farming | Inflated TDM | Return cooldown, grace, repeated-victim anomaly rules; count team deaths, not claimed killer credit |
| Correlated private labels | Re-identification | Match-scoped random labels; no stable pseudonyms; cohort suppression |
| Clan forks/renames to reset Elo or no-show history | Reputation laundering | Verified clan identity lineage and moderator review; rating transfer policy |
| Leader account compromise | Terms/roster sabotage | Strong leader verification, short sessions, action confirmation, append-only audit, revoke flow |
| API/cache leaks exact venue | Player safety risk | Separate private DTOs/endpoints, `no-store`, no analytics payloads, secret scanning, authorization tests |

## 8. Understandable UI copy

### Availability card

```text
Wilderness KOTH • 20v20 • High risk
Sunday, 20:00–20:30 UTC
War roster average: 112.4
Exact world and location stay private until the ready window.
```

### Terms confirmation

```text
Confirm these match terms

Your roster, scoring rules, privacy rules, candidate venue pool, and reveal times will be locked. Any later change requires both leaders to confirm again.

[Review changes]  [Confirm terms]
```

### Roster balance

```text
Roster check
Your starters: 20 • Average 112.4 • Range 98–126
Opponent starters: 20 • Average 111.7 • Range 101–124
Average difference: 0.7

Average combat measures roster balance. Wilderness eligibility is checked separately for every player pair.
```

### No legal venue

```text
No eligible Wilderness venue
At least one starter cannot attack every opposing starter at the shallowest level of any allowed venue.

Change the roster, allow a deeper venue tier, or switch to Clan Wars Arena.
```

### Draw integrity

```text
Surprise draw locked
The eligible venue list is frozen. The result will use public randomness that does not exist yet, so neither clan nor the service can choose the location after seeing attendance.

Draw proof: Available after reveal
```

### Pre-reveal member state

```text
You are rostered as a starter
Exact world and staging instructions unlock in 08:42.
Keeping them hidden reduces pre-camping. Wilderness items may be lost.

[I'm ready]
```

### Reveal

```text
Location revealed — private match information
World: 330
Staging: [instructions]
First hill unlocks in 00:54.

Share only with your locked roster. Leaving the marked area may stop scoring.
```

### Tracking/confidence

```text
Ranked scoring active
Coverage: Good
Only team totals are shown live. Telemetry is untrusted evidence and is checked across multiple participants.
```

```text
Ranked scoring paused
Participant coverage or venue validation is too low. The clock is paused while both leaders resolve the issue.
```

### Substitute

```text
Activate substitute?
Replacing Player A with Player B will change your roster average from 112.4 to 112.8. The selected venue remains eligible.

Player A cannot re-enter this match after scoring begins.
[Cancel]  [Activate and notify both leaders]
```

### Public-name privacy

```text
Hide my name publicly
Your name will not appear on public rosters, match pages, search, or individual leaderboards. Match-scoped labels such as “Private player 7” may appear instead.

Clan leaders, opponent leaders after roster lock, moderators, and integrity systems can still see your identity. Your combat level and results still count in team averages, clan scores, and ratings.
```

### Anonymous rating

```text
Your rating is private
Your matches still update your rating and your clan's rating, but your name, rating, and leaderboard row are hidden from the public. Turning visibility on later does not reset your rating.
```

### No-show result

```text
Provisional no-show loss
Your clan did not meet the agreed minimum roster within the 5-minute grace period. The opponent met its minimum. Clan Elo impact is reduced; reliability history will be updated.

[Review evidence]  [Dispute]
```

## 9. Product tradeoffs and final defaults

| Decision | Tradeoff | Recommended default |
|---|---|---|
| CWA vs Wilderness | CWA is fairer/safer; Wilderness is culturally authentic but crashable and risky | CWA primary ranked queue; separate Wilderness TDM/KOTH ratings |
| Surprise vs preparation | Later reveal reduces camping but increases logistics failures | World/site T-10m; first hill T-60s; rotations T-15s |
| Randomness design | Beacon adds external dependency; server nonce is easier but operator-gameable | Future public beacon with reproducible draw |
| Average metric | Simple and understandable but gameable | Display mean plus private median/range checks; never use mean for attack legality |
| Venue legality | Full cross-roster attackability reduces venue choice | Require full mutual cross-team eligibility for ranked play |
| Roster lock | Earlier is fairer; later handles attendance | T-30m with pre-locked substitutes |
| Mid-match substitutes | Helps disconnects but changes competitive state | None in ranked KOTH; one permanent replacement in TDM only if pre-agreed |
| No-show Elo | Full penalty is harsh; none enables dodging | 50% clan Elo weight plus separate reliability score |
| Public roster privacy | Transparency aids integrity; names increase harassment/doxxing risk | Names opt-in publicly; identities visible to authorized leaders/integrity systems |
| Anonymous Elo | Public pseudonyms enable re-identification; omission creates rank gaps | Private internal rating, no public anonymous row; true ranks with gaps or percentiles |
| Telemetry confidence | Strict quorum causes pauses; permissive scoring is exploitable | Bilateral coverage thresholds; provisional/no-contest rather than invented certainty |

These defaults should be encoded in a versioned rules preset, not scattered across UI and backend logic. The canonical terms document and draw verifier should be treated as product features, not implementation details.
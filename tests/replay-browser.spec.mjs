import { test, expect } from '@playwright/test';

const json = (body) => ({
  status: 200,
  contentType: 'application/json',
  body: JSON.stringify(body),
});

const baseApis = {
  '/api/clans': { clans: [] },
  '/api/public/availability': { availability: [], scheduled: [], history: [] },
  '/api/public/battles': { battles: [] },
  '/api/fight-setup/schema': { requiredFields: [] },
  '/api/theme/assets': { images: [] },
  '/api/leaderboard': { standings: [], modeLabel: 'Clan Wars Arena' },
  '/api/leaderboard?mode=cwa': { standings: [], modeLabel: 'Clan Wars Arena' },
  '/api/leaderboard?mode=wildy': { standings: [], modeLabel: 'Wilderness' },
  '/api/challenge-system': { leaderActions: [] },
  '/api/judging-system': { winnerSignals: [] },
};

const fightSummary = (events, overrides = {}) => ({
  fight: {
    creatorClanId: 'trapistan',
    opponentClanId: 'rivals',
    terms: {
      world: 330,
      location: 'Clan Wars Arena',
      startsAt: '2026-08-20T20:00:00Z',
      durationMinutes: 30,
      combatMin: 70,
      combatMax: 126,
      rules: 'Matched opts',
      mode: 'cwa',
      returnsAllowed: false,
      ...overrides.terms,
    },
  },
  analytics: {
    totals: { damageInflicted: 42, eventsTracked: events.length },
    dimensions: { confidence: { high: events.length } },
    byClan: {},
    byPlayer: {},
    byOpponent: {},
    locationHotspots: events.filter((event) => event.location).map((event) => ({
      regionId: event.location.regionId,
      x: event.location.x,
      y: event.location.y,
      plane: event.location.plane,
      samples: 1,
    })),
    events,
  },
});

async function stubApis(page, summary) {
  await page.route('**/api/**', async (route) => {
    const url = new URL(route.request().url());
    const key = url.pathname + url.search;
    if (url.pathname === '/api/public/fights/regression-fight/summary') {
      await route.fulfill(json(summary));
      return;
    }
    await route.fulfill(json(baseApis[key] ?? baseApis[url.pathname] ?? {}));
  });
}

test('completed fight replay supports play, pause, and scrub controls', async ({ page }) => {
  await stubApis(page, fightSummary([
    {
      timestamp: 1000,
      observedAt: '2026-08-20T20:00:01Z',
      tick: 10,
      type: 'damage_dealt',
      clanId: 'trapistan',
      player: 'Oyama',
      opponentName: 'Rival',
      amount: 42,
      evidence: 'local_player_hitsplat',
      confidence: 'high',
      relation: 'non_own_clan',
      world: 330,
      location: { regionId: 12850, x: 3200, y: 3600, plane: 0 },
    },
    {
      timestamp: 1500,
      observedAt: '2026-08-20T20:00:02Z',
      tick: 11,
      type: 'death',
      clanId: 'rivals',
      player: 'Private rival',
      opponentName: 'Oyama',
      amount: 1,
      evidence: 'target_death_with_recent_local_damage',
      confidence: 'high',
      relation: 'outsider_or_unverified',
      world: 330,
      location: { regionId: 12850, x: 3202, y: 3601, plane: 0 },
    },
  ]));

  await page.goto('https://salmon-dune-01c80c60f.7.azurestaticapps.net/results?fight=regression-fight');
  await expect(page.locator('#replay-clock')).toContainText('1/2 · tick 10 · damage dealt');
  await expect(page.locator('#replay-scrubber')).toHaveAttribute('max', '1');

  await page.locator('#replay-play').click();
  await expect(page.locator('#replay-play')).toHaveText('Pause');
  await page.locator('#replay-play').click();
  await expect(page.locator('#replay-play')).toHaveText('Play');

  await page.locator('#replay-scrubber').fill('1');
  await expect(page.locator('#replay-clock')).toContainText('2/2 · tick 11 · death');
  await expect(page.locator('#replay-legend')).toContainText('Trail = observed positions');
});

test('completed fight replay renders no-position events and mode-specific terms honestly', async ({ page }) => {
  await stubApis(page, fightSummary([
    {
      timestamp: 2000,
      observedAt: '2026-08-20T20:00:03Z',
      tick: 20,
      type: 'return',
      clanId: 'trapistan',
      player: 'Private player',
      opponentName: 'Rival',
      amount: 1,
      evidence: 'first_combat_event_after_local_death',
      confidence: 'high',
      relation: 'non_own_clan',
      world: 330,
    },
  ], {
    terms: {
      location: 'Ghorrock',
      rules: 'Wildy returns allowed; no single-spell pile fallback',
      mode: 'wildy',
      returnsAllowed: true,
    },
  }));

  await page.goto('https://salmon-dune-01c80c60f.7.azurestaticapps.net/results?fight=regression-fight');
  await expect(page.locator('#analytics-terms')).toContainText('Ghorrock');
  await expect(page.locator('#analytics-terms')).toContainText('Wildy returns allowed');
  await expect(page.locator('#analytics-locations')).toContainText('No location samples');
  await expect(page.locator('#analytics-events')).toContainText('W330 R0');
});

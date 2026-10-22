import test from 'node:test';
import assert from 'node:assert/strict';
import {
  classifyCapabilityOutcome,
  parseGridInventory,
  recoverOwnedSession,
  sanitizePublicSummary,
  signalOwnedProcessGroup,
  validatePrivateGridUrl,
  validatePrivateReceipt,
} from './grid-recovery-drill.mjs';

const nonce = 'a'.repeat(32);
const sourceSha = 'b'.repeat(40);
const ownedId = 'owned:session-1';
const origin = 'http://127.0.0.1:4444';
const grid = { origin, endpoint: `${origin}/wd/hub` };

function statusPayload(sessions = [], browserName = 'chrome', ready = true) {
  return {
    value: {
      ready,
      nodes: [{
        id: 'node-test',
        slots: [{
          id: 'slot-test',
          stereotype: { browserName },
          session: sessions.length ? {
            sessionId: sessions[0].sessionId,
            capabilities: sessions[0].capabilities ?? {
              browserName: 'chrome',
              browserVersion: '145.0',
              platformName: 'linux',
            },
          } : null,
        }],
      }],
    },
  };
}

function response(body, status = 200) {
  return { ok: status >= 200 && status < 300, status, json: async () => body };
}

function receipt(sessionId = ownedId, createdAt = new Date(10_000).toISOString()) {
  return {
    schemaVersion: 1,
    runNonce: nonce,
    sourceSha,
    gridOrigin: origin,
    sessionId,
    createdAt,
    requestedCapabilities: { browserName: 'chrome', 'se:name': `grid-recovery:${nonce}` },
    observedCapabilities: { browserName: 'chrome', browserVersion: '145.0', platformName: 'linux' },
  };
}

test('capability classification follows the validated slot inventory and structured response', () => {
  const inventory = parseGridInventory(statusPayload());
  assert.equal(classifyCapabilityOutcome({
    inventory,
    requestedBrowser: 'quality-drill-unsupported',
    response: { status: 500, body: { value: { error: 'session not created' } } },
  }), 'CAPABILITY_UNAVAILABLE');
  assert.equal(classifyCapabilityOutcome({
    inventory,
    requestedBrowser: 'chrome',
    response: { status: 500, body: { value: { error: 'session not created' } } },
  }), 'UNKNOWN');
  assert.equal(classifyCapabilityOutcome({
    inventory,
    requestedBrowser: 'quality-drill-unsupported',
    response: { status: 500, body: { value: { error: 'unknown error' } } },
  }), 'UNKNOWN');
  assert.equal(classifyCapabilityOutcome({
    inventory,
    requestedBrowser: 'quality-drill-unsupported',
    response: null,
    reachable: false,
  }), 'GRID_UNREACHABLE');
});

test('missing or changed Grid inventory schemas fail closed', () => {
  assert.throws(() => parseGridInventory({ value: { ready: true } }), /GRID_INVENTORY_SCHEMA_UNAVAILABLE/);
  assert.throws(() => parseGridInventory({ value: { ready: true, nodes: [{ slots: [{}] }] } }), /GRID_INVENTORY_SCHEMA_UNAVAILABLE/);
  assert.throws(() => parseGridInventory({ value: { nodes: [] } }), /GRID_INVENTORY_SCHEMA_UNAVAILABLE/);
});

test('a full single-slot Grid can be not ready and still expose its live session', () => {
  const inventory = parseGridInventory(
    statusPayload([{ sessionId: ownedId }], 'chrome', false),
  );

  assert.equal(inventory.ready, false);
  assert.equal(inventory.sessions.length, 1);
  assert.equal(inventory.sessions[0].sessionId, ownedId);
});

test('private Grid URL rejects remote hosts, credentials, query strings and fragments', () => {
  assert.equal(validatePrivateGridUrl(`${origin}/wd/hub`).origin, origin);
  for (const value of [
    'https://grid.example.test/wd/hub',
    'http://user:pass@127.0.0.1:4444/wd/hub',
    'http://127.0.0.1:4444/wd/hub?token=x',
    'http://127.0.0.1:4444/wd/hub#fragment',
    'http://127.0.0.2:4444/wd/hub',
  ]) {
    assert.throws(() => validatePrivateGridUrl(value), /GRID_URL_/);
  }
});

test('receipt age, origin, nonce, source revision and in-memory allowlist all gate deletion', () => {
  const context = {
    origin,
    nonce,
    sourceSha,
    now: 30_000,
    ownedSessionIds: new Set([ownedId]),
  };
  assert.equal(validatePrivateReceipt(receipt(), context).sessionId, ownedId);
  assert.throws(() => validatePrivateReceipt(receipt(ownedId, new Date(-100_001).toISOString()), context), /OWNERSHIP_RECEIPT_/);
  assert.throws(() => validatePrivateReceipt({ ...receipt(), gridOrigin: 'http://127.0.0.1:5555' }, context), /OWNERSHIP_RECEIPT_/);
  assert.throws(() => validatePrivateReceipt({ ...receipt(), runNonce: 'c'.repeat(32) }, context), /OWNERSHIP_RECEIPT_/);
  assert.throws(() => validatePrivateReceipt({ ...receipt(), sourceSha: 'd'.repeat(40) }, context), /OWNERSHIP_RECEIPT_/);
  assert.throws(() => validatePrivateReceipt(receipt('foreign-session'), context), /OWNERSHIP_RECEIPT_/);
});

test('recovery deletes one encoded owned session, polls it out, and is idempotent', async () => {
  let active = [{ sessionId: ownedId }];
  let clock = 30_000;
  let deleteCount = 0;
  const deletePaths = [];
  const fetchImpl = async (url, options = {}) => {
    if (url === `${origin}/status`) return response(statusPayload(active));
    if (options.method === 'DELETE') {
      deleteCount += 1;
      deletePaths.push(url);
      active = [];
      return response({ value: null });
    }
    throw new Error('unexpected fixture request');
  };
  const recoveredSessions = new Set();
  const args = {
    grid,
    receipt: receipt(ownedId, new Date(29_999).toISOString()),
    context: { origin, nonce, sourceSha, ownedSessionIds: new Set([ownedId]) },
    fetchImpl,
    recoveredSessions,
    now: () => clock,
    sleep: async (milliseconds) => { clock += milliseconds; },
  };

  const result = await recoverOwnedSession(args);
  const repeated = await recoverOwnedSession(args);
  assert.equal(result.outcome, 'deleted');
  assert.equal(repeated.outcome, 'already-recovered');
  assert.deepEqual(deletePaths, [`${origin}/wd/hub/session/owned%3Asession-1`]);
  assert.equal(deleteCount, 1);
  assert.equal(active.length, 0);
});

test('foreign session is denied before any HTTP deletion or inventory lookup', async () => {
  let calls = 0;
  await assert.rejects(recoverOwnedSession({
    grid,
    receipt: receipt('foreign-session'),
    context: { origin, nonce, sourceSha, ownedSessionIds: new Set([ownedId]) },
    fetchImpl: async () => { calls += 1; return response(statusPayload()); },
    now: () => 30_000,
  }), /OWNERSHIP_RECEIPT_NOT_AUTHORIZED/);
  assert.equal(calls, 0);
});

test('an unsuccessful or malformed deletion never becomes a cleanup success', async () => {
  const fetchImpl = async (url, options = {}) => {
    if (url === `${origin}/status`) return response(statusPayload([{ sessionId: ownedId }]));
    if (options.method === 'DELETE') return response({ unexpected: true });
    throw new Error('unexpected fixture request');
  };
  await assert.rejects(recoverOwnedSession({
    grid,
    receipt: receipt(ownedId, new Date(29_999).toISOString()),
    context: { origin, nonce, sourceSha, ownedSessionIds: new Set([ownedId]) },
    fetchImpl,
    now: () => 30_000,
  }), /OWNED_SESSION_DELETE_MALFORMED/);
});

test('process interruption targets only the explicitly owned detached process group', () => {
  const signals = [];
  signalOwnedProcessGroup({ pid: 1234, detached: true }, 'SIGKILL', (pid, signal) => {
    signals.push({ pid, signal });
  });
  assert.deepEqual(signals, [{ pid: -1234, signal: 'SIGKILL' }]);
  assert.throws(() => signalOwnedProcessGroup({ pid: 1234, detached: false }, 'SIGKILL', () => {}), /CHILD_PROCESS_GROUP_NOT_OWNED/);
});

test('public output omits session IDs, receipts, raw logs and screenshot canaries', () => {
  const canary = 'MASKED_SCREENSHOT_CANARY_8af6d1';
  const publicSummary = sanitizePublicSummary({
    status: 'passed',
    sourceSha,
    runId: '123456',
    runAttempt: 1,
    projectName: 'grid-recovery-123456-1',
    gridOrigin: origin,
    observedCapabilities: { browserName: 'chrome', browserVersion: '145.0', platformName: 'linux' },
    sessionId: `raw-${canary}`,
    privateReceipt: { sessionId: canary },
    rawLogs: canary,
    screenshotFallbackBytes: canary,
    screenshotCanary: canary,
    sessionCounts: { starting: 0, orphanObserved: 1, afterRecovery: 0, afterControl: 0, successfulSessions: 2 },
    recovery: { outcome: 'deleted', sessionIdHash: 'a'.repeat(20) },
    control: { outcome: 'passed' },
  });
  const rendered = JSON.stringify(publicSummary);
  assert.equal(rendered.includes(canary), false);
  assert.equal(rendered.includes('rawSession'), false);
  assert.equal(rendered.includes('privateReceipt'), false);
  assert.equal(rendered.includes('screenshotFallbackBytes'), false);
  assert.match(rendered, /"sessionIdSha256Prefix":"a{20}"/);
});

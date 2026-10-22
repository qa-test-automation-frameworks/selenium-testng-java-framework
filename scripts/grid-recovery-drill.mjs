#!/usr/bin/env node

import { spawn } from 'node:child_process';
import { createHash, randomUUID } from 'node:crypto';
import { chmod, mkdir, readFile, rename, rm, writeFile } from 'node:fs/promises';
import { setTimeout as delay } from 'node:timers/promises';
import path from 'node:path';
import { pathToFileURL } from 'node:url';

export const FAILURE_CATEGORIES = Object.freeze([
  'EXPECTED_WORKER_INTERRUPTION',
  'CAPABILITY_UNAVAILABLE',
  'GRID_UNREACHABLE',
  'SESSION_LOST',
  'APPLICATION_ASSERTION',
  'UNKNOWN',
]);

export class DrillFailure extends Error {
  constructor(code, category = 'UNKNOWN', phase = 'unknown') {
    super(code);
    this.name = 'DrillFailure';
    this.code = code;
    this.category = FAILURE_CATEGORIES.includes(category) ? category : 'UNKNOWN';
    this.phase = phase;
  }
}

const failure = (code, category, phase) => new DrillFailure(code, category, phase);
const sha256 = (value) => createHash('sha256').update(String(value)).digest('hex');
const delayMs = (milliseconds) => delay(milliseconds);

export function validatePrivateGridUrl(value) {
  let url;
  try {
    url = new URL(value);
  } catch {
    throw failure('GRID_URL_INVALID', 'UNKNOWN', 'arguments');
  }
  const loopback = ['127.0.0.1', 'localhost', '[::1]'].includes(url.hostname);
  if (
    url.protocol !== 'http:' ||
    !loopback ||
    url.username ||
    url.password ||
    url.search ||
    url.hash ||
    !['/wd/hub', '/'].includes(url.pathname.replace(/\/$/, '') || '/')
  ) {
    throw failure('GRID_URL_NOT_PRIVATE_LOOPBACK', 'UNKNOWN', 'arguments');
  }
  return Object.freeze({ url, origin: url.origin, endpoint: url.href.replace(/\/$/, '') });
}

export function parseGridInventory(payload) {
  const value = payload?.value;
  if (!value || value.ready !== true || !Array.isArray(value.nodes)) {
    throw failure('GRID_INVENTORY_SCHEMA_UNAVAILABLE', 'UNKNOWN', 'inventory');
  }
  const sessions = [];
  const stereotypes = [];
  for (const node of value.nodes) {
    if (!node || !Array.isArray(node.slots)) {
      throw failure('GRID_INVENTORY_SCHEMA_UNAVAILABLE', 'UNKNOWN', 'inventory');
    }
    for (const slot of node.slots) {
      if (!slot || typeof slot.stereotype !== 'object' || slot.stereotype === null) {
        throw failure('GRID_INVENTORY_SCHEMA_UNAVAILABLE', 'UNKNOWN', 'inventory');
      }
      const browserName = slot.stereotype.browserName;
      if (typeof browserName !== 'string' || browserName.length === 0) {
        throw failure('GRID_INVENTORY_SCHEMA_UNAVAILABLE', 'UNKNOWN', 'inventory');
      }
      stereotypes.push(browserName);
      if (slot.session === null) continue;
      if (
        !slot.session ||
        typeof slot.session.sessionId !== 'string' ||
        slot.session.sessionId.length === 0 ||
        typeof slot.session.capabilities !== 'object' ||
        slot.session.capabilities === null
      ) {
        throw failure('GRID_INVENTORY_SCHEMA_UNAVAILABLE', 'UNKNOWN', 'inventory');
      }
      sessions.push({
        sessionId: slot.session.sessionId,
        capabilities: slot.session.capabilities,
        stereotype: slot.stereotype,
      });
    }
  }
  return Object.freeze({
    ready: true,
    nodeCount: value.nodes.length,
    sessions: Object.freeze(sessions),
    stereotypes: Object.freeze(stereotypes),
  });
}

export function classifyCapabilityOutcome({ inventory, requestedBrowser, response, reachable = true }) {
  if (!reachable) return 'GRID_UNREACHABLE';
  if (!inventory || !Array.isArray(inventory.stereotypes)) return 'UNKNOWN';
  const browserIsAbsent = !inventory.stereotypes.includes(requestedBrowser);
  if (
    browserIsAbsent &&
    response?.status >= 400 &&
    response?.body?.value?.error === 'session not created'
  ) {
    return 'CAPABILITY_UNAVAILABLE';
  }
  return 'UNKNOWN';
}

function safeSessionId(sessionId) {
  return typeof sessionId === 'string' && /^[A-Za-z0-9._:-]{1,256}$/.test(sessionId);
}

export function validatePrivateReceipt(receipt, context) {
  if (!receipt || receipt.schemaVersion !== 1 || !safeSessionId(receipt.sessionId)) {
    throw failure('OWNERSHIP_RECEIPT_INVALID', 'UNKNOWN', 'ownership');
  }
  const createdAt = Date.parse(receipt.createdAt);
  if (
    receipt.runNonce !== context.nonce ||
    receipt.sourceSha !== context.sourceSha ||
    receipt.gridOrigin !== context.origin ||
    receipt.requestedCapabilities?.['se:name'] !== `grid-recovery:${context.nonce}` ||
    !Number.isFinite(createdAt) ||
    context.now - createdAt < 0 ||
    context.now - createdAt > 120_000 ||
    receipt.requestedCapabilities?.browserName !== 'chrome' ||
    typeof receipt.observedCapabilities?.browserName !== 'string' ||
    !context.ownedSessionIds?.has(receipt.sessionId)
  ) {
    throw failure('OWNERSHIP_RECEIPT_NOT_AUTHORIZED', 'UNKNOWN', 'ownership');
  }
  return Object.freeze({ sessionId: receipt.sessionId, createdAt });
}

async function responseJson(response, code, category, phase) {
  try {
    return await response.json();
  } catch {
    throw failure(code, category, phase);
  }
}

export async function readGridInventory(grid, fetchImpl = fetch, timeoutMs = 1500) {
  let response;
  try {
    response = await fetchImpl(`${grid.origin}/status`, {
      signal: AbortSignal.timeout(timeoutMs),
    });
  } catch {
    throw failure('GRID_STATUS_UNREACHABLE', 'GRID_UNREACHABLE', 'inventory');
  }
  if (!response.ok) throw failure('GRID_STATUS_UNAVAILABLE', 'GRID_UNREACHABLE', 'inventory');
  return parseGridInventory(
    await responseJson(response, 'GRID_INVENTORY_SCHEMA_UNAVAILABLE', 'UNKNOWN', 'inventory'),
  );
}

export async function recoverOwnedSession({
  grid,
  receipt,
  context,
  fetchImpl = fetch,
  recoveredSessions = new Set(),
  waitMs = 10_000,
  sleep = delayMs,
  now = Date.now,
}) {
  const authorized = validatePrivateReceipt(receipt, { ...context, now: now() });
  const recoveryKey = `${context.nonce}:${sha256(authorized.sessionId)}`;
  if (recoveredSessions.has(recoveryKey)) {
    return Object.freeze({ outcome: 'already-recovered', sessionIdHash: sha256(authorized.sessionId).slice(0, 20) });
  }

  let inventory = await readGridInventory(grid, fetchImpl);
  if (!inventory.sessions.some((session) => session.sessionId === authorized.sessionId)) {
    throw failure('OWNED_SESSION_NOT_VISIBLE', 'SESSION_LOST', 'recovery');
  }

  let response;
  try {
    response = await fetchImpl(
      `${grid.endpoint}/session/${encodeURIComponent(authorized.sessionId)}`,
      { method: 'DELETE', signal: AbortSignal.timeout(1500) },
    );
  } catch {
    throw failure('OWNED_SESSION_DELETE_UNREACHABLE', 'GRID_UNREACHABLE', 'recovery');
  }
  if (!response.ok) throw failure('OWNED_SESSION_DELETE_REJECTED', 'UNKNOWN', 'recovery');
  const body = await responseJson(response, 'OWNED_SESSION_DELETE_MALFORMED', 'UNKNOWN', 'recovery');
  if (!body || !Object.hasOwn(body, 'value')) {
    throw failure('OWNED_SESSION_DELETE_MALFORMED', 'UNKNOWN', 'recovery');
  }

  const deadline = now() + waitMs;
  do {
    const remaining = Math.max(1, deadline - now());
    inventory = await readGridInventory(grid, fetchImpl, Math.min(1500, remaining));
    if (!inventory.sessions.some((session) => session.sessionId === authorized.sessionId)) {
      recoveredSessions.add(recoveryKey);
      return Object.freeze({ outcome: 'deleted', sessionIdHash: sha256(authorized.sessionId).slice(0, 20) });
    }
    if (now() >= deadline) break;
    await sleep(Math.min(250, Math.max(1, deadline - now())));
  } while (now() <= deadline);
  throw failure('OWNED_SESSION_REMAINS_AFTER_DELETE', 'UNKNOWN', 'recovery');
}

export function sanitizePublicSummary(input) {
  const observed = input.observedCapabilities ?? {};
  const allowedCategories = new Set(FAILURE_CATEGORIES);
  const output = {
    schemaVersion: 1,
    status: input.status === 'passed' ? 'passed' : 'failed',
    failureCode: /^[A-Z0-9_]{1,64}$/.test(input.failureCode ?? '') ? input.failureCode : '',
    failurePhase: /^[a-z-]{1,32}$/.test(input.failurePhase ?? '') ? input.failurePhase : '',
    sourceSha: String(input.sourceSha ?? '').slice(0, 40),
    runId: String(input.runId ?? '').slice(0, 32),
    runAttempt: Number.isInteger(input.runAttempt) ? input.runAttempt : 0,
    projectName: String(input.projectName ?? '').slice(0, 64),
    gridOrigin: String(input.gridOrigin ?? '').slice(0, 128),
    images: {
      hub: String(input.images?.hub ?? '').slice(0, 256),
      chrome: String(input.images?.chrome ?? '').slice(0, 256),
      hubImageId: String(input.images?.hubImageId ?? '').slice(0, 128),
      chromeImageId: String(input.images?.chromeImageId ?? '').slice(0, 128),
    },
    limits: {
      memoryBytes: Number(input.limits?.memoryBytes ?? 0),
      sharedMemoryBytes: Number(input.limits?.sharedMemoryBytes ?? 0),
    },
    requestedBrowser: 'chrome',
    observedCapabilities: {
      browserName: String(observed.browserName ?? '').slice(0, 32),
      browserVersion: String(observed.browserVersion ?? '').slice(0, 32),
      platformName: String(observed.platformName ?? '').slice(0, 32),
    },
    bidi: input.bidi === 'active' || input.bidi === 'unavailable' ? input.bidi : 'not-requested',
    timestamps: {
      gridReady: String(input.timestamps?.gridReady ?? ''),
      unsupportedCapability: String(input.timestamps?.unsupportedCapability ?? ''),
      sessionReady: String(input.timestamps?.sessionReady ?? ''),
      workerKilled: String(input.timestamps?.workerKilled ?? ''),
      recoveryCompleted: String(input.timestamps?.recoveryCompleted ?? ''),
      controlCompleted: String(input.timestamps?.controlCompleted ?? ''),
    },
    unsupportedCapability: {
      requestedBrowser: 'quality-drill-unsupported',
      disposition: input.unsupportedCapability?.disposition === 'CAPABILITY_UNAVAILABLE'
        ? 'CAPABILITY_UNAVAILABLE'
        : 'UNKNOWN',
      elapsedMs: Number(input.unsupportedCapability?.elapsedMs ?? 0),
    },
    interruption: {
      category: allowedCategories.has(input.interruption?.category)
        ? input.interruption.category
        : 'UNKNOWN',
      childExitCode: input.interruption?.childExitCode ?? null,
      childSignal: String(input.interruption?.childSignal ?? '').slice(0, 16),
      nativeDisposition: input.interruption?.category === 'EXPECTED_WORKER_INTERRUPTION'
        ? 'incomplete_expected_process_interruption'
        : 'not-established-before-worker-interruption',
    },
    ownershipProof: input.ownershipProof === 'nonce-and-observed-capability'
      ? 'nonce-and-observed-capability'
      : 'isolated-origin-session-allowlist',
    recovery: {
      action: 'delete-one-owned-session',
      outcome: input.recovery?.outcome === 'deleted' ? 'deleted' : 'failed',
      sessionIdSha256Prefix: /^[a-f0-9]{20}$/.test(input.recovery?.sessionIdHash ?? '')
        ? input.recovery.sessionIdHash
        : '',
    },
    sessionCounts: {
      starting: Number(input.sessionCounts?.starting ?? 0),
      orphanObserved: Number(input.sessionCounts?.orphanObserved ?? 0),
      afterRecovery: Number(input.sessionCounts?.afterRecovery ?? 0),
      afterControl: Number(input.sessionCounts?.afterControl ?? 0),
      successfulSessions: Number(input.sessionCounts?.successfulSessions ?? 0),
    },
    control: {
      outcome: input.control?.outcome === 'passed' ? 'passed' : 'failed',
      nativeDisposition: String(input.control?.nativeDisposition ?? 'not-run').slice(0, 64),
    },
    diagnosticAvailability: String(input.diagnosticAvailability ?? 'available').slice(0, 32),
    limitations: [
      'Isolated pinned Grid only; this does not prove safety on shared Grids or arbitrary network partitions.',
      'BiDi was not requested; no Grid video or raw session diagnostics were collected.',
    ],
  };
  return output;
}

export function signalOwnedProcessGroup(owner, signal = 'SIGKILL', killProcess = process.kill) {
  if (!owner || owner.detached !== true || !Number.isInteger(owner.pid) || owner.pid <= 1) {
    throw failure('CHILD_PROCESS_GROUP_NOT_OWNED', 'UNKNOWN', 'interruption');
  }
  killProcess(-owner.pid, signal);
}

function parseArgs(argv) {
  const result = { flags: new Set(), values: {} };
  for (let index = 0; index < argv.length; index += 1) {
    const item = argv[index];
    if (item === '--private-grid') {
      result.flags.add(item);
    } else if (item.startsWith('--') && argv[index + 1] && !argv[index + 1].startsWith('--')) {
      result.values[item.slice(2)] = argv[index + 1];
      index += 1;
    } else {
      throw failure('ARGUMENTS_INVALID', 'UNKNOWN', 'arguments');
    }
  }
  return result;
}

function assertRunBoundary(args) {
  if (!args.flags.has('--private-grid') || process.env.GITHUB_ACTIONS !== 'true') {
    throw failure('PRIVATE_HOSTED_RUN_REQUIRED', 'UNKNOWN', 'arguments');
  }
  const required = ['grid-url', 'nonce', 'source-sha', 'run-id', 'run-attempt', 'project-name', 'receipt-dir', 'summary'];
  if (required.some((name) => !args.values[name])) {
    throw failure('ARGUMENTS_INCOMPLETE', 'UNKNOWN', 'arguments');
  }
  if (!/^[a-f0-9]{32}$/i.test(args.values.nonce)) {
    throw failure('RUN_NONCE_INVALID', 'UNKNOWN', 'arguments');
  }
  const expectedProject = `grid-recovery-${args.values['run-id']}-${args.values['run-attempt']}`;
  if (
    args.values['project-name'] !== expectedProject ||
    process.env.COMPOSE_PROJECT_NAME !== expectedProject
  ) {
    throw failure('COMPOSE_PROJECT_NOT_OWNED', 'UNKNOWN', 'arguments');
  }
  const tempRoot = path.resolve(process.env.RUNNER_TEMP ?? '');
  const receiptDir = path.resolve(args.values['receipt-dir']);
  if (!process.env.RUNNER_TEMP || path.dirname(receiptDir) !== tempRoot) {
    throw failure('PRIVATE_RECEIPT_PATH_INVALID', 'UNKNOWN', 'arguments');
  }
  return {
    grid: validatePrivateGridUrl(args.values['grid-url']),
    nonce: args.values.nonce,
    sourceSha: args.values['source-sha'],
    runId: args.values['run-id'],
    runAttempt: Number(args.values['run-attempt']),
    projectName: args.values['project-name'],
    receiptDir,
    summaryPath: path.resolve(args.values.summary),
    receiptPath: path.join(receiptDir, 'owned-session.json'),
    controlPath: path.join(path.dirname(path.resolve(args.values.summary)), 'grid-recovery-control.json'),
  };
}

async function waitForGrid(grid, fetchImpl = fetch) {
  const deadline = Date.now() + 120_000;
  let lastFailure;
  while (Date.now() < deadline) {
    try {
      const inventory = await readGridInventory(grid, fetchImpl);
      if (
        inventory.nodeCount > 0 &&
        inventory.stereotypes.includes('chrome')
      ) {
        return inventory;
      }
      lastFailure = failure('GRID_CHROME_NODE_NOT_REGISTERED', 'GRID_UNREACHABLE', 'inventory');
    } catch (error) {
      lastFailure = error;
      if (error.code === 'GRID_INVENTORY_SCHEMA_UNAVAILABLE') throw error;
    }
    await delayMs(1000);
  }
  throw lastFailure ?? failure('GRID_STARTUP_TIMEOUT', 'GRID_UNREACHABLE', 'inventory');
}

async function requestUnsupportedCapability(grid, inventory) {
  const requestedBrowser = 'quality-drill-unsupported';
  const startedAt = Date.now();
  let response;
  let body;
  try {
    response = await fetch(`${grid.endpoint}/session`, {
      method: 'POST',
      headers: { 'content-type': 'application/json' },
      body: JSON.stringify({
        capabilities: { alwaysMatch: { browserName: requestedBrowser }, firstMatch: [{}] },
      }),
      signal: AbortSignal.timeout(6500),
    });
    body = await responseJson(response, 'UNSUPPORTED_CAPABILITY_RESPONSE_INVALID', 'UNKNOWN', 'capability');
  } catch (error) {
    if (error instanceof DrillFailure) throw error;
    throw failure('UNSUPPORTED_CAPABILITY_GRID_UNREACHABLE', 'GRID_UNREACHABLE', 'capability');
  }
  const elapsedMs = Date.now() - startedAt;
  const disposition = classifyCapabilityOutcome({
    inventory,
    requestedBrowser,
    response: { status: response.status, body },
  });
  if (
    disposition !== 'CAPABILITY_UNAVAILABLE' ||
    elapsedMs > 7000 ||
    inventory.sessions.length !== 0
  ) {
    throw failure('UNSUPPORTED_CAPABILITY_CONTROL_INVALID', disposition, 'capability');
  }
  return { disposition, elapsedMs };
}

function workerArgs(config, mode) {
  const remoteCapabilities = JSON.stringify({ 'se:name': `grid-recovery:${config.nonce}` });
  return [
    '-B', '-ntp',
    '-Dtestng.suite.file=testng-grid-recovery.xml',
    '-Dexecution.type=remote',
    `-Dremote.url=${config.grid.endpoint}`,
    '-Dbrowser=CHROME',
    '-Dheadless=true',
    '-Dthread.count=1',
    `-Dremote.capabilities=${remoteCapabilities}`,
    `-Dgrid.recovery.mode=${mode}`,
    `-Dgrid.recovery.nonce=${config.nonce}`,
    `-Dgrid.recovery.source.sha=${config.sourceSha}`,
    `-Dgrid.recovery.receipt=${mode === 'interrupted' ? config.receiptPath : ''}`,
    `-Dgrid.recovery.control=${mode === 'control' ? config.controlPath : ''}`,
    'test',
  ];
}

function startWorker(config, mode) {
  const child = spawn('./mvnw', workerArgs(config, mode), {
    cwd: process.cwd(),
    env: process.env,
    detached: true,
    stdio: ['ignore', 'pipe', 'pipe'],
  });
  let outputTail = '';
  let readyResolve;
  let readyReject;
  let readySettled = false;
  let closed = false;
  const ready = new Promise((resolve, reject) => {
    readyResolve = resolve;
    readyReject = reject;
  });
  let lineBuffer = '';
  const observe = (chunk) => {
    const text = chunk.toString('utf8');
    lineBuffer += text;
    if (lineBuffer.length > 2048) lineBuffer = lineBuffer.slice(-2048);
    outputTail = `${outputTail}${text}`.slice(-4096);
    const lines = lineBuffer.split(/\r?\n/);
    lineBuffer = lines.pop() ?? '';
    if (mode === 'interrupted' && lines.includes('GRID_RECOVERY_READY') && !readySettled) {
      readySettled = true;
      readyResolve();
    }
  };
  child.stdout.on('data', observe);
  child.stderr.on('data', () => {});
  const close = new Promise((resolve) => {
    child.once('error', () => {
      closed = true;
      if (!readySettled) {
        readySettled = true;
        readyReject(failure('WORKER_SPAWN_FAILED', 'UNKNOWN', 'worker'));
      }
      resolve({ code: null, signal: null, error: true });
    });
    child.once('close', (code, signal) => {
      closed = true;
      if (!readySettled && mode === 'interrupted') {
        readySettled = true;
        readyReject(failure('WORKER_EXITED_BEFORE_SESSION_READY', 'UNKNOWN', 'worker'));
      }
      resolve({ code, signal, error: false });
    });
  });
  return { child, close, ready, get closed() { return closed; }, get outputTail() { return outputTail; } };
}

async function waitClose(worker, timeoutMs) {
  const timeout = new Promise((resolve) => setTimeout(() => resolve(null), timeoutMs));
  const result = await Promise.race([worker.close, timeout]);
  if (!result) throw failure('WORKER_EXIT_TIMEOUT', 'UNKNOWN', 'worker');
  return result;
}

async function waitForSession(inventoryFunction, sessionId, present, timeoutMs = 10_000) {
  const deadline = Date.now() + timeoutMs;
  let inventory;
  do {
    inventory = await inventoryFunction();
    const found = inventory.sessions.some((session) => session.sessionId === sessionId);
    if (found === present) return inventory;
    await delayMs(250);
  } while (Date.now() < deadline);
  return inventory;
}

async function writeJsonAtomic(filePath, value, mode = 0o600) {
  const temporaryPath = `${filePath}.${randomUUID()}.tmp`;
  await writeFile(temporaryPath, `${JSON.stringify(value, null, 2)}\n`, { mode, flag: 'wx' });
  await chmod(temporaryPath, mode);
  await rename(temporaryPath, filePath);
}

async function readJson(filePath) {
  return JSON.parse(await readFile(filePath, 'utf8'));
}

function timestamp() {
  return new Date().toISOString();
}

function isOwnedSession(inventory, receipt) {
  return inventory.sessions.find((session) => session.sessionId === receipt.sessionId);
}

async function runDrill(config) {
  const data = {
    status: 'failed',
    sourceSha: config.sourceSha,
    runId: config.runId,
    runAttempt: config.runAttempt,
    projectName: config.projectName,
    gridOrigin: config.grid.origin,
    images: {
      hub: process.env.GRID_HUB_IMAGE ?? '',
      chrome: process.env.GRID_CHROME_IMAGE ?? '',
      hubImageId: process.env.GRID_HUB_IMAGE_ID ?? '',
      chromeImageId: process.env.GRID_CHROME_IMAGE_ID ?? '',
    },
    limits: {
      memoryBytes: Number(process.env.GRID_MEMORY_LIMIT_BYTES ?? 0),
      sharedMemoryBytes: Number(process.env.GRID_SHARED_MEMORY_BYTES ?? 0),
    },
    observedCapabilities: {},
    bidi: 'not-requested',
    timestamps: {},
    unsupportedCapability: {},
    interruption: { category: 'UNKNOWN', childExitCode: null, childSignal: '' },
    recovery: { outcome: 'failed', sessionIdHash: '' },
    sessionCounts: { starting: 0, orphanObserved: 0, afterRecovery: 0, afterControl: 0, successfulSessions: 0 },
    control: { outcome: 'failed', nativeDisposition: 'not-run' },
    diagnosticAvailability: 'available',
    ownershipProof: 'isolated-origin-session-allowlist',
  };
  let activeWorker;
  let receipt;
  let inventory;
  let receiptDirCreated = false;
  const ownedSessionIds = new Set();
  const recoveredSessions = new Set();
  const receiptContext = { origin: config.grid.origin, nonce: config.nonce, sourceSha: config.sourceSha, ownedSessionIds };

  try {
    if (
      data.limits.memoryBytes < 2_147_483_648 ||
      data.limits.sharedMemoryBytes < 2_147_483_648
    ) {
      throw failure('GRID_RESOURCE_LIMIT_BELOW_TWO_GIB', 'UNKNOWN', 'startup');
    }
    await mkdir(config.receiptDir, { mode: 0o700 });
    receiptDirCreated = true;
    await chmod(config.receiptDir, 0o700);
    inventory = await waitForGrid(config.grid);
    data.timestamps.gridReady = timestamp();
    data.sessionCounts.starting = inventory.sessions.length;
    if (inventory.sessions.length !== 0) throw failure('GRID_DID_NOT_START_EMPTY', 'UNKNOWN', 'startup');

    const unsupported = await requestUnsupportedCapability(config.grid, inventory);
    data.unsupportedCapability = unsupported;
    data.timestamps.unsupportedCapability = timestamp();
    inventory = await readGridInventory(config.grid);
    if (inventory.sessions.length !== 0) throw failure('UNSUPPORTED_REQUEST_ALLOCATED_SESSION', 'UNKNOWN', 'capability');

    activeWorker = startWorker(config, 'interrupted');
    await Promise.race([
      activeWorker.ready,
      delayMs(90_000).then(() => { throw failure('WORKER_SESSION_READY_TIMEOUT', 'UNKNOWN', 'worker'); }),
    ]);
    data.timestamps.sessionReady = timestamp();
    receipt = await readJson(config.receiptPath);
    const parsedReceipt = validatePrivateReceipt(receipt, {
      ...receiptContext,
      now: Date.now(),
      ownedSessionIds: new Set([receipt.sessionId]),
    });
    inventory = await waitForSession(() => readGridInventory(config.grid), parsedReceipt.sessionId, true);
    const observed = isOwnedSession(inventory, receipt);
    if (!observed) throw failure('OWNED_SESSION_NOT_VISIBLE_BEFORE_INTERRUPT', 'SESSION_LOST', 'worker');
    ownedSessionIds.add(parsedReceipt.sessionId);
    data.observedCapabilities = {
      browserName: observed.capabilities.browserName,
      browserVersion: observed.capabilities.browserVersion ?? '',
      platformName: observed.capabilities.platformName ?? '',
    };
    if (observed.capabilities['se:name'] === `grid-recovery:${config.nonce}`) {
      data.ownershipProof = 'nonce-and-observed-capability';
    }

    if (activeWorker.closed) throw failure('WORKER_EXITED_BEFORE_CONTROLLED_KILL', 'UNKNOWN', 'interruption');
    signalOwnedProcessGroup({ pid: activeWorker.child.pid, detached: true });
    const childExit = await waitClose(activeWorker, 10_000);
    data.interruption.childExitCode = childExit.code;
    data.interruption.childSignal = childExit.signal ?? '';
    data.timestamps.workerKilled = timestamp();
    if (childExit.signal !== 'SIGKILL') {
      throw failure('WORKER_WAS_NOT_INTERRUPTED_AS_EXPECTED', 'UNKNOWN', 'interruption');
    }
    inventory = await waitForSession(() => readGridInventory(config.grid), receipt.sessionId, true, 3000);
    if (!isOwnedSession(inventory, receipt)) {
      data.interruption.category = 'SESSION_LOST';
      throw failure('ORPHAN_SESSION_NOT_OBSERVED', 'SESSION_LOST', 'interruption');
    }
    data.interruption.category = 'EXPECTED_WORKER_INTERRUPTION';
    data.sessionCounts.orphanObserved = inventory.sessions.length;
    if (inventory.sessions.length !== 1) {
      throw failure('UNEXPECTED_ACTIVE_SESSION_COUNT', 'UNKNOWN', 'interruption');
    }

    const recovered = await recoverOwnedSession({
      grid: config.grid,
      receipt,
      context: receiptContext,
      recoveredSessions,
    });
    data.recovery = recovered;
    data.timestamps.recoveryCompleted = timestamp();
    inventory = await readGridInventory(config.grid);
    data.sessionCounts.afterRecovery = inventory.sessions.length;
    if (inventory.sessions.length !== 0) throw failure('OWNED_SESSION_REMAINS_AFTER_RECOVERY', 'UNKNOWN', 'recovery');

    activeWorker = startWorker(config, 'control');
    const controlExit = await waitClose(activeWorker, 120_000);
    activeWorker = undefined;
    if (controlExit.code !== 0 || controlExit.signal) {
      throw failure('NORMAL_CONTROL_WORKER_FAILED', 'APPLICATION_ASSERTION', 'control');
    }
    const control = await readJson(config.controlPath);
    if (
      control.schemaVersion !== 1 ||
      control.status !== 'passed' ||
      control.frameworkCleanupCalled !== true ||
      control.observedCapabilities?.browserName !== 'chrome'
    ) {
      throw failure('NORMAL_CONTROL_RESULT_INVALID', 'APPLICATION_ASSERTION', 'control');
    }
    data.control = { outcome: 'passed', nativeDisposition: 'testng-control-passed-and-framework-cleanup-called' };
    data.sessionCounts.successfulSessions = 2;
    data.timestamps.controlCompleted = timestamp();
    inventory = await readGridInventory(config.grid);
    data.sessionCounts.afterControl = inventory.sessions.length;
    if (inventory.sessions.length !== 0) throw failure('OWNED_SESSION_REMAINS_AFTER_CONTROL', 'UNKNOWN', 'control');
    data.status = 'passed';
    return data;
  } catch (error) {
    if (error instanceof DrillFailure) {
      data.interruption.category = data.interruption.category === 'EXPECTED_WORKER_INTERRUPTION'
        ? data.interruption.category
        : error.category;
      data.failureCode = error.code;
      data.failurePhase = error.phase;
    } else {
      data.failureCode = 'UNEXPECTED_DRILL_FAILURE';
      data.failurePhase = 'unknown';
      if (data.interruption.category !== 'EXPECTED_WORKER_INTERRUPTION') {
        data.interruption.category = 'UNKNOWN';
      }
    }
    throw Object.assign(error, { publicData: data });
  } finally {
    if (activeWorker && !activeWorker.closed) {
      try {
        signalOwnedProcessGroup({ pid: activeWorker.child.pid, detached: true });
        await Promise.race([activeWorker.close, delayMs(5000)]);
      } catch {
        // Only the child process group created by this run is eligible for interruption.
      }
    }
    if (receipt) {
      try {
        const current = await readGridInventory(config.grid);
        const present = isOwnedSession(current, receipt);
        if (present && !ownedSessionIds.has(receipt.sessionId)) {
          validatePrivateReceipt(receipt, {
            origin: config.grid.origin,
            nonce: config.nonce,
            sourceSha: config.sourceSha,
            now: Date.now(),
            ownedSessionIds: new Set([receipt.sessionId]),
          });
          ownedSessionIds.add(receipt.sessionId);
        }
        if (present && ownedSessionIds.has(receipt.sessionId)) {
          await recoverOwnedSession({
            grid: config.grid,
            receipt,
            context: { ...receiptContext, now: Date.now() },
            recoveredSessions,
            waitMs: 10_000,
          });
        }
      } catch {
        data.diagnosticAvailability = 'scoped-cleanup-unavailable';
      }
    }
    if (receiptDirCreated) {
      try {
        await rm(config.receiptDir, { recursive: true, force: true });
      } catch {
        data.diagnosticAvailability = 'private-receipt-removal-failed';
      }
    }
    if (data.status !== 'passed' && !data.failureCode) {
      data.failureCode = 'DRILL_INCOMPLETE';
      data.failurePhase = 'unknown';
    }
    const publicData = sanitizePublicSummary(data);
    await writeJsonAtomic(config.summaryPath, publicData, 0o644);
    const nativePath = path.join(path.dirname(config.summaryPath), 'grid-recovery-native.json');
    await writeJsonAtomic(nativePath, {
      schemaVersion: 1,
      suite: 'testng-grid-recovery.xml',
      interruptedTestDisposition: publicData.interruption.nativeDisposition,
      workerSignal: publicData.interruption.childSignal,
      recoveryControl: publicData.control.outcome,
      frameworkCleanupCalled: publicData.control.outcome === 'passed',
      rawSessionIdentifiersUploaded: false,
      rawLogsUploaded: false,
    }, 0o644);
    if (process.env.GITHUB_STEP_SUMMARY) {
      const lines = [
        '## Owned-session Grid recovery drill',
        '',
        `- Result: ${publicData.status}`,
        `- Source: \`${publicData.sourceSha}\` (run ${publicData.runId}, attempt ${publicData.runAttempt})`,
        `- Unsupported capability: ${publicData.unsupportedCapability.disposition} in ${publicData.unsupportedCapability.elapsedMs} ms`,
        `- Worker outcome: ${publicData.interruption.category} (${publicData.interruption.childSignal || `exit ${publicData.interruption.childExitCode}`})`,
        `- Recovery: ${publicData.recovery.outcome}; owned sessions after recovery/control: ${publicData.sessionCounts.afterRecovery}/${publicData.sessionCounts.afterControl}`,
        `- Same-slot control: ${publicData.control.outcome}; successful real sessions: ${publicData.sessionCounts.successfulSessions}`,
        '',
      ].join('\n');
      await writeFile(process.env.GITHUB_STEP_SUMMARY, lines, { flag: 'a' });
    }
  }
}

async function main(argv) {
  const parsed = parseArgs(argv);
  const config = assertRunBoundary(parsed);
  const data = await runDrill(config).catch((error) => {
    process.stderr.write(`Grid recovery drill failed (${error.code ?? 'UNEXPECTED_DRILL_FAILURE'} at ${error.phase ?? 'unknown'}).\n`);
    if (error.publicData) {
      process.stderr.write(`Recorded category: ${error.publicData.interruption.category}.\n`);
    }
    throw error;
  });
  if (data.status !== 'passed') process.exitCode = 1;
}

if (process.argv[1] && import.meta.url === pathToFileURL(path.resolve(process.argv[1])).href) {
  main(process.argv.slice(2)).catch(() => {
    process.exitCode = 1;
  });
}

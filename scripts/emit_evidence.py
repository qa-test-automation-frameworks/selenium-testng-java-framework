#!/usr/bin/env python3
"""Emit a sanitized evidence-v3 record from Maven Surefire's JUnit XML."""
import hashlib
import json
import os
import sys
import xml.etree.ElementTree as ET
from datetime import datetime, timezone
from pathlib import Path


def utc(value):
    if not value:
        return None
    parsed = datetime.fromisoformat(value.replace("Z", "+00:00"))
    if parsed.tzinfo is None:
        raise ValueError("timestamps must include a timezone")
    return parsed.astimezone(timezone.utc).isoformat(timespec="seconds").replace("+00:00", "Z")


def build_record(env, report_dir, output):
    source = env.get("GITHUB_SHA", "")
    if len(source) != 40 or any(c not in "0123456789abcdef" for c in source):
        raise ValueError("GITHUB_SHA must be the exact 40-character tested checkout SHA")
    reports = sorted(Path(report_dir).glob("TEST-*.xml"))
    native = None
    integrity = "unavailable"
    selected = passed = failed = skipped = retried = None
    raw_retry_attempts = None
    limitations = ["Surefire XML contains aggregate outcomes only; testcase names, parameters, traces and environment values are intentionally omitted."]
    if env.get("TARGET_ID", "saucedemo-public-demo") == "saucedemo-public-demo":
        limitations.append("SauceDemo is vendor-managed; this run did not discover an application release or fixture revision.")
    if reports:
        digest = hashlib.sha256()
        sums = {key: 0 for key in ("tests", "failures", "errors", "skipped")}
        cases = 0
        retried_cases = 0
        raw_retries = 0
        try:
            for report in reports:
                raw = report.read_bytes()
                if len(raw) > 20_000_000 or b"<!DOCTYPE" in raw.upper() or b"<!ENTITY" in raw.upper():
                    raise ValueError("unsafe or oversized native XML")
                digest.update(report.name.encode() + b"\0" + raw)
                root = ET.fromstring(raw)
                if root.tag not in ("testsuite", "testsuites"):
                    raise ValueError("unexpected Surefire XML root")
                suites = [root] if root.tag == "testsuite" else list(root.findall("testsuite"))
                if not suites:
                    raise ValueError("empty Surefire XML")
                for suite in suites:
                    for key in sums:
                        sums[key] += int(suite.attrib[key])
                    testcases = suite.findall("testcase")
                    cases += len(testcases)
                    case_failures = sum(any(child.tag == "failure" for child in case) for case in testcases)
                    case_errors = sum(any(child.tag == "error" for child in case) for case in testcases)
                    case_skips = sum(any(child.tag == "skipped" for child in case) for case in testcases)
                    if (case_failures, case_errors, case_skips) != (
                        int(suite.attrib["failures"]), int(suite.attrib["errors"]), int(suite.attrib["skipped"])
                    ):
                        raise ValueError("native aggregate failure/error/skip counters do not match testcase outcomes")
                    retried_cases += sum(any(child.tag in ("flakyFailure", "flakyError") for child in case) for case in testcases)
                    raw_retries += sum(child.tag in ("flakyFailure", "flakyError") for case in testcases for child in case)
            selected = sums["tests"]
            failed = sums["failures"] + sums["errors"]
            skipped = sums["skipped"]
            passed = selected - failed - skipped
            retried = retried_cases
            raw_retry_attempts = raw_retries
            if selected != cases or min(passed, failed, skipped, retried) < 0 or selected <= 0:
                raise ValueError("native XML aggregate counters do not reconcile to substantive testcase entries")
            native = digest.hexdigest()
            integrity = "complete"
            limitations.append("Native Surefire XML SHA-256: " + native)
        except (ET.ParseError, OSError, ValueError, KeyError) as exc:
            integrity = "partial"
            limitations.append("Native Surefire report validation failed: " + str(exc)[:300])
            selected = passed = failed = skipped = retried = None
            raw_retry_attempts = None
    now = utc(env.get("COMPLETED_AT"))
    start = utc(env.get("STARTED_AT"))
    requested = env.get("TEST_OUTCOME", "failure").lower()
    if requested == "skipped" and env.get("SKIP_REASON"):
        disposition = "skipped"
        reason = env["SKIP_REASON"]
        start = now = None
        selected = passed = failed = skipped = retried = None
        raw_retry_attempts = None
        integrity = "unavailable"
    elif requested == "skipped":
        disposition = "unavailable"
        reason = "The test runner did not start; inspect earlier workflow step outcomes."
        start = now = None
        selected = passed = failed = skipped = retried = None
        raw_retry_attempts = None
        integrity = "unavailable"
    elif requested == "cancelled":
        disposition = "cancelled"
        reason = "The native runner was cancelled before the workflow completed."
        if integrity == "complete":
            integrity = "partial"
            limitations.append("Workflow cancellation means the native report may omit planned execution.")
    elif integrity == "unavailable":
        disposition = "unavailable"
        reason = "No current Surefire XML report was produced."
    elif integrity == "partial":
        disposition = "failed"
        reason = "Surefire XML was present but incomplete or inconsistent."
    else:
        disposition = "passed" if requested == "success" and failed == 0 and skipped < selected else "failed"
        reason = None if disposition == "passed" else "The native runner failed, reported failing cases, or selected no substantive cases."
    run_id = env.get("GITHUB_RUN_ID", "")
    attempt = int(env.get("GITHUB_RUN_ATTEMPT", "1"))
    repo = env.get("GITHUB_REPOSITORY", "qa-test-automation-frameworks/selenium-testng-java-framework")
    browser = env.get("BROWSER", "CHROME")
    scope = env.get("SCOPE_ID", "ui." + browser)
    tools = {"java": env.get("JAVA_VERSION", "unknown"), "maven": env.get("MAVEN_VERSION", "unknown"),
             "selenium": env.get("SELENIUM_VERSION", "4.39.0"), "testng": env.get("TESTNG_VERSION", "7.12.0")}
    if disposition == "skipped":
        tools = {}
    record = {
        "schemaVersion": 3, "repository": repo, "sourceSha": source,
        "limitations": limitations, "kind": "execution",
        "workflow": {"id": env.get("GITHUB_WORKFLOW_REF", "selenium-testng-java-framework/.github/workflows/ui-tests.yml"),
                     "name": env.get("GITHUB_WORKFLOW", "UI Tests"), "runId": run_id, "attempt": attempt,
                     "url": env.get("RUN_URL", "https://github.com/qa-test-automation-frameworks/selenium-testng-java-framework/actions/runs/1"),
                     "trigger": env.get("GITHUB_EVENT_NAME", "local"), "branch": env.get("GITHUB_REF_NAME", "local")},
        "scope": {"id": scope, "evidenceClass": env.get("EVIDENCE_CLASS", "controlled"),
                  "required": env.get("SCOPE_REQUIRED", "true").lower() == "true", "freshnessPolicy": env.get("FRESHNESS_POLICY", "quarterly_experiment_v1")},
        "target": {"identity": env.get("TARGET_ID", "saucedemo-public-demo"), "revision": env.get("TARGET_REVISION") or None,
                   "fixtureVersion": None, "environment": env.get("TARGET_ENVIRONMENT", "public-demo"),
                   "profile": env.get("TARGET_PROFILE") or browser, "tools": tools},
        "execution": {"disposition": disposition, "reason": reason, "startedAt": start, "completedAt": now,
                       "integrity": integrity,
                       "counts": {"unit": env.get("COUNT_UNIT", "parameter_case"), "selected": selected, "executed": selected - skipped if selected is not None else None,
                                  "passed": passed, "failed": failed, "skipped": skipped, "retried": retried,
                                  "semantics": "Final Surefire JUnit testcase invocations (each TestNG data-provider invocation is a testcase); selected includes skips; retried counts unique testcase entries with native flakyFailure/flakyError children."},
                       "measurements": ([{"name": "native_retry_attempts", "value": raw_retry_attempts,
                                           "unit": "attempt", "sampleCount": 1,
                                           "semantics": "Count of native Surefire flakyFailure/flakyError attempt elements across the current aggregate XML files."}]
                                        if raw_retry_attempts is not None else []),
                       "shards": {"expected": [scope], "received": [scope] if integrity == "complete" else []}},
        "publication": {"disposition": "pending", "reason": None, "publishedAt": None, "reportUrl": None, "artifacts": []},
        "generator": {"name": "selenium-surefire-evidence", "version": "1.0.0"}}
    Path(output).parent.mkdir(parents=True, exist_ok=True)
    Path(output).write_text(json.dumps(record, indent=2) + "\n", encoding="utf-8")
    return record


if __name__ == "__main__":
    try:
        result = build_record(os.environ, sys.argv[1] if len(sys.argv) > 1 else "target/surefire-reports",
                              sys.argv[2] if len(sys.argv) > 2 else "target/portfolio-evidence/evidence-v3.json")
        print("evidence disposition=" + result["execution"]["disposition"] + " integrity=" + result["execution"]["integrity"])
        # A failed test is valid evidence; the earlier native test step preserves its own failure.
        sys.exit(0 if result["execution"]["integrity"] == "complete" or result["execution"]["disposition"] == "skipped" else 1)
    except Exception as exc:
        print("evidence generation failed: " + str(exc), file=sys.stderr)
        sys.exit(2)

#!/usr/bin/env python3
"""Validate the two sanitized shared-app session-control records."""

import json
import re
import sys
from pathlib import Path

HASH_PATTERN = re.compile(r"^[0-9a-f]{64}$")


def verify(directory):
    records = []
    for path in sorted(Path(directory).glob("control-*.json")):
        record = json.loads(path.read_text(encoding="utf-8"))
        if not isinstance(record, dict):
            raise ValueError(f"control record must be a JSON object in {path.name}")
        required = {
            "scenarioId",
            "memberASessionSha256",
            "memberBSessionSha256",
            "claimIdSha256",
            "memberACleaned",
            "memberBCleaned",
        }
        if set(record) != required:
            raise ValueError(f"unexpected control record fields in {path.name}")
        if not isinstance(record["scenarioId"], str) or not record["scenarioId"]:
            raise ValueError(f"missing scenario identity in {path.name}")
        for field in ("memberASessionSha256", "memberBSessionSha256", "claimIdSha256"):
            if not isinstance(record[field], str) or not HASH_PATTERN.fullmatch(record[field]):
                raise ValueError(f"invalid sanitized hash in {path.name}: {field}")
        if record["memberACleaned"] is not True or record["memberBCleaned"] is not True:
            raise ValueError(f"driver cleanup did not pass in {path.name}")
        records.append(record)

    if len(records) != 2:
        raise ValueError(f"expected two fresh scenario invocations, found {len(records)}")
    if len({record["scenarioId"] for record in records}) != 2:
        raise ValueError("scenario invocation identities were reused")

    session_hashes = [
        record[field]
        for record in records
        for field in ("memberASessionSha256", "memberBSessionSha256")
    ]
    if len(set(session_hashes)) != 4:
        raise ValueError("expected four distinct remote sessions across two invocations")

    claim_hashes = [record["claimIdSha256"] for record in records]
    if len(set(claim_hashes)) != 2:
        raise ValueError("claim identity was reused across fresh invocations")

    return {"invocations": 2, "uniqueSessions": 4, "cleanedSessions": 4, "uniqueClaims": 2}


if __name__ == "__main__":
    try:
        summary = verify(sys.argv[1] if len(sys.argv) > 1 else "target/shared-app-controls")
        print(
            "shared-app control passed: "
            f"{summary['invocations']} invocations, "
            f"{summary['uniqueSessions']} unique sessions, "
            f"{summary['cleanedSessions']} cleanups, "
            f"{summary['uniqueClaims']} unique claims"
        )
    except (OSError, json.JSONDecodeError, ValueError) as failure:
        print(f"shared-app control failed: {failure}", file=sys.stderr)
        sys.exit(1)

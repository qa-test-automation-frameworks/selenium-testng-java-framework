"""Fail unless the intentional TestNG control failure was captured correctly."""
import hashlib
import json
import sys
import xml.etree.ElementTree as ET
from pathlib import Path


def verify(record_path, report_dir):
    record = json.loads(Path(record_path).read_text(encoding="utf-8"))
    execution = record["execution"]
    counts = execution["counts"]
    reports = sorted(Path(report_dir).glob("TEST-*.xml"))
    if len(reports) != 1:
        raise ValueError("expected exactly one current Surefire XML report")
    report = reports[0]
    digest = hashlib.sha256(report.name.encode() + b"\0" + report.read_bytes()).hexdigest()
    root = ET.parse(report).getroot()
    expected = ("failed", "complete", 1, 1, 0, 0)
    actual = (execution["disposition"], execution["integrity"], counts["selected"], counts["failed"], counts["passed"], counts["skipped"])
    if actual != expected:
        raise ValueError("native failure did not reconcile to exactly one failed testcase")
    if (int(root.attrib["tests"]), int(root.attrib["failures"]), int(root.attrib["errors"]), int(root.attrib["skipped"])) != (1, 1, 0, 0):
        raise ValueError("Surefire XML did not contain exactly one failure")
    if "Native Surefire XML SHA-256: " + digest not in record["limitations"]:
        raise ValueError("record digest does not match the native XML input")
    if record["workflow"]["runId"] != sys.argv[3] or record["sourceSha"] != sys.argv[4]:
        raise ValueError("record is not bound to the current Actions run and checkout")
    print("verified expected failed TestNG case against current Surefire XML and source identity")


if __name__ == "__main__":
    try:
        verify(sys.argv[1], sys.argv[2])
    except Exception as error:
        print("negative-evidence verification failed: " + str(error), file=sys.stderr)
        sys.exit(1)

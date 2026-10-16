import tempfile
import unittest
from pathlib import Path

from emit_evidence import build_record


class EvidenceEmitterTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.report = self.root / "reports"
        self.report.mkdir()
        self.env = {
            "GITHUB_SHA": "a" * 40, "GITHUB_RUN_ID": "42", "GITHUB_RUN_ATTEMPT": "1",
            "GITHUB_WORKFLOW_REF": "owner/repo/.github/workflows/ui-tests.yml@refs/heads/main",
            "GITHUB_EVENT_NAME": "push", "GITHUB_REF_NAME": "main", "TEST_OUTCOME": "success",
            "STARTED_AT": "2026-10-04T10:00:00Z", "COMPLETED_AT": "2026-10-04T10:01:00Z",
        }

    def test_native_success_and_redaction(self):
        (self.report / "TEST-suite.xml").write_text(
            '<testsuite tests="3" failures="0" errors="0" skipped="1">'
            '<testcase name="secret-user@example.test"/><testcase name="redacted"><skipped/></testcase>'
            '<testcase name="case"><flakyFailure message="sensitive"/></testcase></testsuite>')
        result = build_record(self.env, self.report, self.root / "out.json")
        counts = result["execution"]["counts"]
        self.assertEqual((counts["selected"], counts["executed"], counts["passed"], counts["skipped"], counts["retried"]), (3, 2, 2, 1, 1))
        self.assertEqual(result["execution"]["measurements"][0]["value"], 1)
        payload = (self.root / "out.json").read_text()
        self.assertNotIn("secret-user", payload)
        self.assertNotIn("sensitive", payload)

    def test_missing_and_inconsistent_reports_never_pass(self):
        self.assertEqual(build_record(self.env, self.report, self.root / "none.json")["execution"]["integrity"], "unavailable")
        (self.report / "TEST-suite.xml").write_text('<testsuite tests="2" failures="0" errors="0" skipped="0"><testcase/></testsuite>')
        result = build_record(self.env, self.report, self.root / "bad.json")
        self.assertEqual(result["execution"]["integrity"], "partial")
        self.assertEqual(result["execution"]["disposition"], "failed")

    def test_failed_runner_exit_is_preserved(self):
        (self.report / "TEST-suite.xml").write_text('<testsuite tests="1" failures="0" errors="0" skipped="0"><testcase/></testsuite>')
        env = dict(self.env, TEST_OUTCOME="failure")
        self.assertEqual(build_record(env, self.report, self.root / "failed.json")["execution"]["disposition"], "failed")

    def test_unconfigured_cloud_is_explicit_skip_without_fake_counts(self):
        env = dict(self.env, TEST_OUTCOME="skipped", SKIP_REASON="Credentials unavailable.")
        record = build_record(env, self.report, self.root / "skipped.json")
        self.assertEqual(record["execution"]["disposition"], "skipped")
        self.assertIsNone(record["execution"]["startedAt"])
        self.assertIsNone(record["execution"]["counts"]["selected"])
        self.assertEqual(record["execution"]["shards"]["received"], [])
        self.assertEqual(record["target"]["tools"], {})

    def test_previously_failed_step_does_not_masquerade_as_cloud_skip(self):
        env = dict(self.env, TEST_OUTCOME="skipped")
        record = build_record(env, self.report, self.root / "not-started.json")
        self.assertEqual(record["execution"]["disposition"], "unavailable")
        self.assertIn("did not start", record["execution"]["reason"])


if __name__ == "__main__":
    unittest.main()

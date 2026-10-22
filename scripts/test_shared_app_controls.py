import json
import tempfile
import unittest
from pathlib import Path

from verify_shared_app_controls import verify


class SharedAppControlsTests(unittest.TestCase):
    def setUp(self):
        self.temporary_directory = tempfile.TemporaryDirectory()
        self.directory = Path(self.temporary_directory.name)
        self.records = [self.record("1", "a"), self.record("2", "b")]
        self.write_records()

    def tearDown(self):
        self.temporary_directory.cleanup()

    @staticmethod
    def record(invocation, prefix):
        return {
            "scenarioId": invocation,
            "memberASessionSha256": prefix * 64,
            "memberBSessionSha256": ("c" if prefix == "a" else "d") * 64,
            "claimIdSha256": ("e" if prefix == "a" else "f") * 64,
            "memberACleaned": True,
            "memberBCleaned": True,
        }

    def write_records(self):
        for index, record in enumerate(self.records, start=1):
            (self.directory / f"control-{index}.json").write_text(
                json.dumps(record), encoding="utf-8"
            )

    def test_accepts_two_fresh_invocations_with_four_clean_sessions(self):
        self.assertEqual(
            verify(self.directory),
            {"invocations": 2, "uniqueSessions": 4, "cleanedSessions": 4, "uniqueClaims": 2},
        )

    def test_rejects_reused_session_or_claim_identity(self):
        self.records[1]["memberASessionSha256"] = self.records[0]["memberASessionSha256"]
        self.records[1]["claimIdSha256"] = self.records[0]["claimIdSha256"]
        self.write_records()

        with self.assertRaisesRegex(ValueError, "four distinct remote sessions"):
            verify(self.directory)

    def test_rejects_missing_invocation_or_failed_cleanup(self):
        (self.directory / "control-2.json").unlink()
        with self.assertRaisesRegex(ValueError, "expected two fresh"):
            verify(self.directory)

        self.records[1]["memberACleaned"] = False
        self.write_records()
        with self.assertRaisesRegex(ValueError, "cleanup did not pass"):
            verify(self.directory)


if __name__ == "__main__":
    unittest.main()

#!/usr/bin/env bash
set -u

TEST_STATUS=0
./mvnw test "$@" || TEST_STATUS=$?
./mvnw allure:report -DskipTests || {
  REPORT_STATUS=$?
  if [[ "$TEST_STATUS" -eq 0 ]]; then
    TEST_STATUS=$REPORT_STATUS
  fi
}
exit "$TEST_STATUS"

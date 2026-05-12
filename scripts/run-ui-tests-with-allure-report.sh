#!/usr/bin/env bash
set -u

TEST_STATUS=0
./mvnw test "$@" || TEST_STATUS=$?
./mvnw allure:report -DskipTests
exit "$TEST_STATUS"


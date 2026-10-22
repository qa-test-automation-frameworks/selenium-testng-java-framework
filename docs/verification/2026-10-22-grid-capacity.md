# Grid capacity sample — 22 October 2026

The bounded `grid-capacity-experiment` job completed in [workflow run 39709314143](https://github.com/qa-test-automation-frameworks/selenium-testng-java-framework/actions/runs/39709314143) against framework commit `7f583aa49c101224bd7aa1b5b7e0f886f5f1c3eb`. It opened 21 Chrome sessions in three repetition batches at each concurrency level (1, 2, and 4 workers), loaded and checked a local `data:` page, and quit each session. The uploaded report records 21 distinct session-ID digests, no failures, and zero active sessions after teardown.

The run used Java 21, Selenium 4.51.0, Chrome `147.0.7727.55`, and the pinned `selenium/node-chrome:4.43.0` image (`sha256:9e60c032f8f0950bb962868e73263fb503cb6fbf3f14fdd31e81c1f14b6f247b`). The node's actual memory and shared-memory limits were each 2 GiB. The complete sanitized source rows, provenance, and derived summaries are retained in [grid-capacity-run-154.json](evidence/grid-capacity-run-154.json).

## Results

Each level ran three coordinated batches. A batch at two or four workers produced two or four session-level measurements, respectively; `session samples` shows the denominator used for each median and range.

| Concurrent workers | Batches | Session samples | Request to session created, ms (median; min–max) | Full cycle, ms (median; min–max) |
|---:|---:|---:|---:|---:|
| 1 | 3 | 3 | 383.216; 377.052–1717.180 | 565.817; 550.895–2129.305 |
| 2 | 3 | 6 | 638.538; 366.428–948.361 | 816.526; 528.830–1133.197 |
| 4 | 3 | 12 | 1133.899; 353.882–1945.258 | 1293.008; 516.444–2161.392 |

The request-to-session-created timer covers both Grid queueing and browser startup; this client measurement cannot separate those parts. Full cycle includes that interval, loading and checking the data page, and quitting the session. The first one-worker batch had a notably slower observation, and the ranges are broad at four workers. With only three batches per level, these results support no capacity claim or configuration change. Keep the existing bounded sample and 2 GiB node limits; treat this as a descriptive local-Grid check, not product capacity or production throughput evidence.

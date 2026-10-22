# Grid capacity sample — 22 October 2026

The bounded `grid-capacity-experiment` job completed in [workflow run 39709314143](https://github.com/qa-test-automation-frameworks/selenium-testng-java-framework/actions/runs/39709314143) against framework commit `7f583aa49c101224bd7aa1b5b7e0f886f5f1c3eb`. It opened 21 Chrome sessions in three repetition batches at each concurrency level (1, 2, and 4 workers), loaded and checked a local `data:` page, and quit each session. The uploaded report records 21 distinct session-ID digests, no failures, and zero active sessions after teardown.

The run used Java 21, Selenium 4.51.0, Chrome `147.0.7727.55`, and the pinned `selenium/node-chrome:4.43.0` image (`sha256:9e60c032f8f0950bb962868e73263fb503cb6fbf3f14fdd31e81c1f14b6f247b`). The node's actual memory and shared-memory limits were each 2 GiB. The complete sanitized source rows, provenance, and derived summaries are retained in [grid-capacity-run-154.json](evidence/grid-capacity-run-154.json).

## Results

Each level ran three coordinated batches. For each batch, the value is the median across the concurrent sessions in that repetition. The table reports the median and min–max range of those three batch medians (`n=3` per level); the JSON retains the raw 3, 6, and 12 session rows as well as each batch median.

| Concurrent workers | Repetition batches | Request to session created, ms (median of batch medians; range) | Full cycle, ms (median of batch medians; range) |
|---:|---:|---:|---:|---:|
| 1 | 3 | 383.216; 377.052–1717.180 | 565.817; 550.895–2129.305 |
| 2 | 3 | 652.924; 617.823–678.110 | 819.434; 786.343–861.196 |
| 4 | 3 | 1129.824; 1126.485–1146.918 | 1292.964; 1290.594–1306.940 |

The request-to-session-created timer covers both Grid queueing and browser startup; this client measurement cannot separate those parts. Full cycle includes that interval, loading and checking the data page, and quitting the session. The first one-worker batch was much slower than the next two. The individual four-worker session rows also span 353.882–1945.258 ms for creation and 516.444–2161.392 ms for the full cycle, despite tighter batch medians. With only three batches per level, these results support no capacity claim or configuration change. Keep the existing bounded sample and 2 GiB node limits; treat this as a descriptive local-Grid check, not product capacity or production throughput evidence.

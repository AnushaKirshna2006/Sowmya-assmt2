# CSCU9E5 Assignment 2 — Traffic Light Statecharts

Statechart specifications, Mermaid state diagrams, and Kaocha test suites for three UK traffic light junction configurations implemented with `clj-statecharts`.

## Project Structure

* `src/e5/traffic_light_core.cljc` — Shared UK signal cycle (`:r -> :ra -> :g -> :a -> :r`) and state inspection utilities.
* `src/e5/one_way_without_ctrl.cljc` — Uncontrolled one-way signal conforming to **R1–R8**.
* `src/e5/one_way.cljc` — Controlled one-way signal with `:run` and graceful `:halt` conforming to **R1–R10**.
* `src/e5/three_way.cljc` — Coordinated three-way junction (`:w`, `:n`, `:s`) with mutual exclusion and round-robin scheduling conforming to **R1–R10**.
* `doc/*.md` — Mermaid `stateDiagram-v2` visualisations for each specification.
* `test/e5/*_test.cljc` — Automated Kaocha verification suites covering safety, liveness, cycle ordering, and graceful halting.

## Design Highlights

1. **Separation of Concerns & Code Reuse:** Common UK phase definitions and state helpers are isolated in `traffic_light_core.cljc`, while `three_way.cljc` parameterises single-direction turn states (`turn-states` and `halting-turn-states`) across `:w`, `:n`, and `:s`.
2. **Graceful Halt (R4, R6, R10):** Receiving `:halt` while any signal is `:ra`, `:g`, or `:a` transitions into a `:halting` phase rather than jumping straight to `:r`. Subsequent `:next` events step the active signal safely through `:a` to `:r` before entering `:halted`.
3. **Mutual Exclusion & Minimal Wait Time (R3, R5, R8):** In `three_way.cljc`, only one direction leaves `:r` per turn in strict round-robin order (`:w -> :n -> :s -> :w`) with zero idle all-red steps between turns.

## Running Tests

```bash
clj -M:test
```

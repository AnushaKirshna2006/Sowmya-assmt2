# Three-Way Coordinated Traffic Lights (`:w`, `:n`, `:s`)

```mermaid
stateDiagram-v2
    [*] --> halted
    halted --> running : run

    state running {
        [*] --> w_turn
        state "West Turn (:w)" as w_turn {
            w_r --> w_ra : next
            w_ra --> w_g : next
            w_g --> w_a : next
            w_a --> n_turn : next (:w=:r)
        }
        state "North Turn (:n)" as n_turn {
            n_r --> n_ra : next
            n_ra --> n_g : next
            n_g --> n_a : next
            n_a --> s_turn : next (:n=:r)
        }
        state "South Turn (:s)" as s_turn {
            s_r --> s_ra : next
            s_ra --> s_g : next
            s_g --> s_a : next
            s_a --> w_turn : next (:s=:r)
        }
    }

    running --> halted : halt (when all :r)
    running --> halting : halt (when active signal in :ra/:g/:a)

    state halting {
        [*] --> finish_active_signal
        finish_active_signal --> halted : next (until active signal reaches :r)
    }

    halting --> running : run
```

* **Mutual Exclusion & Safety (R2, R5):** Only one signal (`:w`, `:n`, or `:s`) is active in a turn at any time while the other two remain strictly locked at `:r`, preventing overlapping green or amber lights.
* **Equitable Interleaving & Minimal Wait (R3, R8):** Turns rotate in a strict round-robin (`:w` $\rightarrow$ `:n` $\rightarrow$ `:s` $\rightarrow$ `:w`) with zero idle all-red steps between turns (12 `:next` events for a complete 3-way cycle).
* **Graceful Halt (R10):** If `:halt` is received while any signal is `:ra`, `:g`, or `:a`, `halting` finishes only that signal's remaining UK sequence to `:r` before entering `halted` instead of starting the next signal's turn.
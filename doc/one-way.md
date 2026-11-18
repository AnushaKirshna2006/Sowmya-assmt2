# One-Way Traffic Light (Controlled)

```mermaid
stateDiagram-v2
    [*] --> halted
    state halted {
        [*] --> halted_r : :r
    }
    halted --> running : run

    state running {
        [*] --> run_r : :r
        run_r --> run_ra : next
        run_ra --> run_g : next
        run_g --> run_a : next
        run_a --> run_r : next
    }

    run_r --> halted : halt
    run_ra --> halt_ra : halt
    run_g --> halt_g : halt
    run_a --> halt_a : halt

    state halting {
        halt_ra --> halt_g : next
        halt_g --> halt_a : next
        halt_a --> halted : next
    }

    halt_ra --> run_ra : run
    halt_g --> run_g : run
    halt_a --> run_a : run
```

* **Graceful Halt (R10 & R4):** Sending `:halt` while active (`:ra`, `:g`, or `:a`) enters a `halting` mirror of the active colour rather than snapping directly to `:r`, ensuring `:g` always passes through `:a` on subsequent `:next` events (R4, R6).
* **Resumability (R9):** Sending `:run` at any point—whether in `halted` or mid-shutdown in `halting`—returns the controller to `running` without losing its current light phase.
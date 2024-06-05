## UCSC CHPL Spring 2024 - Weak Memory Models

### Description

Contributions during Spring 2024. Forked **Dartagnan**, i.e. a tool to check state reachability under weak memory models. Created new lock implementations including *bakery*, *cas*, *dekkers*, *exchange*, *filter*, and *petersons* in `benchmarks/locks/`.

Created a script `benchmarks/locks/locks.sh` that uses `benchmarks/locks/config.json` (as input) and runs `benchmarks/locks/locks.c` to conduct a series of Litmus tests with different locks and memory models.

Created Sense Reversal Barrier. In the middle of testing for weak memory patterns when relaxing different instructions, note that dartagnan does not allow yielding `benchmarks/locks/SROBarrier.c`.

### Contributor

Camille Gandotra  
cgandotr@ucsc.edu

### Usage

- *locks.sh*

Run in ``benchmarks/locks/` directory.

```
./locks.sh [OPTIONS]

[OPTIONS]
    --config=<json config file>      - Specify JSON file
    --help                           - Display help message
```

config.json format:

```
{
    "configurations": [
      {
        "lock": <LOCK>*,                           - Lock to test. Supports "bakery" "cas" "dekkers" "exchange" "filter" and "petersons". *Required*.
        "test": <LITMUS TEST>*,                    - Litmus Test. Supports "ls_litmus" "sl_litmus" and "ll_litmus". *Required*.
        "model": <MEMORY MODEL>,                   - Memory model (CAT file name). Supports "rc11" "tso" and "aarch64". Default: "r11" if left as empty string
        "relaxed_options": [<RELAXED OPTIONS>]      - Array where use can add: "acq2rx" "rel2rx"; Leave empty array if no relaxed options
      }, 
      {
        ...
      }
    ]
  }
  
```

config.json example:

```
{
    "configurations": [
      {
        "lock": "EXCHANGE",
        "test": "SL_LITMUS",
        "model": "aarch64",
        "relaxed_options": ["acq2rx", "rel2rx"]
      },
      {
        "lock": "EXCHANGE",
        "test": "LS_LITMUS",
        "model": "aarch64",
        "relaxed_options": ["acq2rx", "rel2rx"]
      }
      
    ]
  }
  
```

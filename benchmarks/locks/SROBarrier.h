#include <stdatomic.h>
#include <stdbool.h>
#include <stdlib.h>
#include <dat3m.h>
#include <sched.h>

// Optimized Sense Reversal Barrier

typedef struct srobarrier {
    _Atomic int counter;
    int total_threads;
    _Atomic bool sense;
    _Atomic bool* thread_sense;
} srobarrier;

void srobarrier_init(srobarrier* SROB, int num_threads) {
    atomic_init(&SROB->counter, 0);
    SROB->total_threads = num_threads;
    atomic_init(&SROB->sense, false);
    SROB->thread_sense = (_Atomic bool*)malloc(sizeof(_Atomic bool) * num_threads);
    for (int i = 0; i < num_threads; i += 1) {
        atomic_init(&SROB->thread_sense[i], true);
    }
}


void srobarrier_barrier(srobarrier* SROB, int tid) {
    int arrival_num = atomic_fetch_add_explicit(&SROB->counter, 1, memory_order_seq_cst);
    if (arrival_num == SROB->total_threads - 1) {
        atomic_store_explicit(&SROB->counter, 0, memory_order_seq_cst);
        bool temp = atomic_load_explicit(&SROB->thread_sense[tid], memory_order_seq_cst);
        atomic_store_explicit(&SROB->sense, temp, memory_order_seq_cst);
    }
    else {
        while(atomic_load_explicit(&SROB->sense, memory_order_seq_cst) != atomic_load_explicit(&SROB->thread_sense[tid], memory_order_seq_cst)) {
            await_while(atomic_load_explicit(&SROB->sense, memory_order_relaxed) != atomic_load_explicit(&SROB->thread_sense[tid], memory_order_relaxed)) {
                // sched_yield();
            }
        }
    }
    bool temp = atomic_load_explicit(&SROB->thread_sense[tid], memory_order_seq_cst);
    atomic_store_explicit(&SROB->thread_sense[tid], !temp, memory_order_seq_cst);    
}
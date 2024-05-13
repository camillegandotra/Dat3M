#include <stdatomic.h>
#include <stdbool.h>
#include <stdlib.h>

#define mo_lock memory_order_acquire
#define mo_unlock memory_order_release

typedef struct filterlock {
    _Atomic int* victim; 
    _Atomic int* level;
    int total_threads;
} filterlock;

void filterlock_init(filterlock* F, int num_threads) {
    F->victim = (_Atomic int*)malloc(sizeof(_Atomic int) * num_threads);
    F->level = (_Atomic int*)malloc(sizeof(_Atomic int) * num_threads);
    F->total_threads = num_threads;
    for (int i = 0; i < num_threads; i++) {
        atomic_init(&F->level[i], 0);
    }
}

void filterlock_acquire(filterlock* F, int thread_id) {
    for (int i = 1; i < F->total_threads; i += 1) {
        atomic_store_explicit(&F->level[thread_id], i, memory_order_seq_cst);
        atomic_store_explicit(&F->victim[i], thread_id, memory_order_seq_cst);
        for (int k = 0; k < F->total_threads; k += 1) {
            while (k != thread_id && (atomic_load_explicit(&F->level[k], memory_order_seq_cst) >= i)
                && (atomic_load_explicit(&F->victim[i], memory_order_seq_cst) == thread_id)) {}
        }
    }
}

void filterlock_release(filterlock* F, int thread_id) {
    atomic_store_explicit(&F->level[thread_id], 0, mo_unlock);
}

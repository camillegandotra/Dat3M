#include <stdatomic.h>
#include <stdbool.h>
#include <stdlib.h>

#ifdef ACQ2RX
#define mo_lock memory_order_relaxed
#else
#define mo_lock memory_order_acquire
#endif

#ifdef REL2RX
#define mo_unlock memory_order_relaxed
#else
#define mo_unlock memory_order_release
#endif

typedef struct petersonslock {
    _Atomic int victim; 
    _Atomic int* flag; 
} petersonslock;

void petersonslock_init(petersonslock* P, int num_threads) {
    P->flag = (_Atomic int*)malloc(sizeof(_Atomic int) * num_threads);
     for (int i = 0; i < num_threads; i++) {
        atomic_init(&P->flag[i], 0);
    }
    atomic_init(&P->victim, -1);
}

void petersonslock_acquire(petersonslock* P, int thread_id) {
    int other_thread = thread_id == 0 ? 1 : 0;
    atomic_store_explicit(&P->flag[thread_id], 1, memory_order_seq_cst);
    atomic_store_explicit(&P->victim, thread_id, memory_order_seq_cst);
    while ((atomic_load_explicit(&P->victim, mo_lock) == thread_id) &&
        (atomic_load_explicit(&P->flag[other_thread], memory_order_seq_cst) == 1)) {}
}

void petersonslock_release(petersonslock* P, int thread_id) {
    atomic_store_explicit(&P->flag[thread_id], 0, mo_unlock);
}
#include <stdatomic.h>
#include <stdbool.h>
#include <stdlib.h>
#include <dat3m.h>

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

typedef struct dekkerslock {
    _Atomic int turn; 
    _Atomic bool* wants_to_enter; 
} dekkerslock;

void dekkerslock_init(dekkerslock* D, int num_threads) {
    D->wants_to_enter = (_Atomic bool*)malloc(sizeof(_Atomic bool) * num_threads);
     for (int i = 0; i < num_threads; i++) {
        atomic_init(&D->wants_to_enter[i], false);
    }
    atomic_init(&D->turn, 0);
}

void dekkerslock_acquire(dekkerslock* D, int thread_id) {
    atomic_store_explicit(&D->wants_to_enter[thread_id], true, memory_order_seq_cst);
    await_while(atomic_load_explicit(&D->wants_to_enter[!thread_id], memory_order_seq_cst)) {
        if (atomic_load_explicit(&D->turn, mo_lock) != thread_id) {
            atomic_store_explicit(&D->wants_to_enter[thread_id], false, mo_lock);
            await_while(atomic_load_explicit(&D->turn, mo_lock) != thread_id) {}
        }
        atomic_store_explicit(&D->wants_to_enter[thread_id], true, mo_lock);
    }
}

void dekkerslock_release(dekkerslock* D, int thread_id) {
    atomic_store_explicit(&D->turn, !thread_id, mo_unlock);
    atomic_store_explicit(&D->wants_to_enter[thread_id], false, mo_unlock);
}
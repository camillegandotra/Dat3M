#include <stdatomic.h>
#include <stdbool.h>
#include <stdlib.h>

#define mo_lock memory_order_acquire
#define mo_unlock memory_order_release

typedef struct bakerylock {
    _Atomic bool* flag; 
    _Atomic int* label;
    int total_threads;
} bakerylock;

void bakerylock_init(bakerylock* B, int num_threads) {
    B->flag = (_Atomic bool*)malloc(sizeof(_Atomic bool) * num_threads);
    B->label = (_Atomic int*)malloc(sizeof(_Atomic int) * num_threads);
    B->total_threads = num_threads;
    for (int i = 0; i < num_threads; i++) {
        atomic_init(&B->flag[i], false);
        atomic_init(&B->label[i], 0);
    }
}

void bakerylock_acquire(bakerylock* B, int thread_id) {
    atomic_store_explicit(&B->flag[thread_id], true, memory_order_seq_cst);
    int current_max = atomic_load_explicit(&B->label[0], memory_order_seq_cst);
    for (int i = 1; i < B->total_threads; i++) {
        int label_value = atomic_load_explicit(&B->label[i], memory_order_seq_cst);
        if (label_value > current_max) {
            current_max = label_value;
        }
    }
    atomic_store_explicit(&B->label[thread_id], current_max + 1, memory_order_seq_cst);

    for (int k = 0; k < B->total_threads; k++) {
        while (k != thread_id && atomic_load_explicit(&B->flag[k], mo_lock) && 
               (atomic_load_explicit(&B->label[k], mo_lock) < atomic_load_explicit(&B->label[thread_id], mo_lock) ||
                (atomic_load_explicit(&B->label[k], mo_lock) == atomic_load_explicit(&B->label[thread_id], mo_lock) && k < thread_id))) {
        }
    }
}

void bakerylock_release(bakerylock* B, int thread_id) {
    atomic_store_explicit(&B->flag[thread_id], false, mo_unlock);
}
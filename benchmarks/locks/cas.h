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

typedef struct caslock {
    _Atomic bool flag; 
} caslock;

void caslock_init(caslock* C) {
    atomic_init(&C->flag, false);
}

void caslock_acquire(caslock* C) {
    bool expected = false;
    await_while(!atomic_compare_exchange_strong_explicit(&C->flag, &expected, true, 
                                        mo_lock,  
                                        mo_unlock)) {
        expected = false;
    }
}

void caslock_release(caslock* C) {
    atomic_store_explicit(&C->flag, false, mo_unlock);
}
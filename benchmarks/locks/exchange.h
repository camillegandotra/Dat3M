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

typedef struct exchangelock {
    _Atomic bool flag; 
} exchangelock;

void exchangelock_init(exchangelock* E) {
    atomic_init(&E->flag, false);
}

void exchangelock_acquire(exchangelock* E) {
    await_while(atomic_exchange_explicit(&E->flag, true, mo_lock)) {}
}

void exchangelock_release(exchangelock* E) {
    atomic_store_explicit(&E->flag, false, mo_unlock);
}


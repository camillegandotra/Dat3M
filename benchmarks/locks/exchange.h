#include <stdatomic.h>
#include <stdbool.h>
#include <stdlib.h>
#include <dat3m.h>

#define mo_lock memory_order_acquire
#define mo_unlock memory_order_release

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


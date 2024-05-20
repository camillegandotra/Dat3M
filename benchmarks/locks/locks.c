#include <pthread.h>
#include <assert.h>
#include <stdio.h>

#define ACQ2RX
#define REL2RX
#define SL_LITMUS
#define BAKERY

#include "bakery.h"
#include "cas.h"
#include "dekkers.h"
#include "exchange.h"
#include "filter.h"
#include "petersons.h"

typedef struct {
    void (*init)(void *, int);
    void (*acquire)(void *, int);
    void (*release)(void *, int);
    void *lock;
} lock_adt;


#ifdef BAKERY
typedef bakerylock lock_t;
#define lock_init(lock, nthreads) bakerylock_init((lock), (nthreads))
#define lock_acquire(lock, id) bakerylock_acquire((lock), (id))
#define lock_release(lock, id) bakerylock_release((lock), (id))
#elif defined(CAS)
typedef caslock lock_t;
#define lock_init(lock, nthreads) caslock_init((lock))
#define lock_acquire(lock, id) caslock_acquire((lock))
#define lock_release(lock, id) caslock_release((lock))
#elif defined(DEKKERS)
#define NTHREADS 2
typedef dekkerslock lock_t;
#define lock_init(lock, nthreads) dekkerslock_init((lock), (nthreads))
#define lock_acquire(lock, id) dekkerslock_acquire((lock), (id))
#define lock_release(lock, id) dekkerslock_release((lock), (id))
#elif defined(EXCHANGE)
typedef exchangelock lock_t;
#define lock_init(lock, nthreads) exchangelock_init((lock))
#define lock_acquire(lock, id) exhangelock_acquire((lock))
#define lock_release(lock, id) exchangelock_release((lock))
#elif defined(FILTER)
typedef filterlock lock_t;
#define lock_init(lock, nthreads) filterlock_init((lock), (nthreads))
#define lock_acquire(lock, id) filterlock_acquire((lock), (id))
#define lock_release(lock, id) filterlock_release((lock), (id))
#elif defined(PETERSONS)
#define NTHREADS 2
typedef petersonslock lock_t;
#define lock_init(lock, nthreads) petersonslock_init((lock), (nthreads))
#define lock_acquire(lock, id) petersonslock_acquire((lock), (id))
#define lock_release(lock, id) petersonslock_release((lock), (id))
#endif

#ifndef NTHREADS
#define NTHREADS 2
#endif

lock_t lock;
_Atomic int sum = 0;
_Atomic int shared = -1;

void *thread_n(void *arg)
{
    intptr_t index = ((intptr_t) arg);

    lock_acquire(&lock, index);
    
    #ifdef LS_LITMUS
        int temp = atomic_load(&sum);
        atomic_store(&sum, temp + 1);
    #elif defined(SL_LITMUS)
        atomic_store(&shared, index);
        int temp = atomic_load(&shared);
        assert(temp == index);
    #elif defined(LL_LITMUS)
        int temp1 = atomic_load(&shared);
        int temp2 = atomic_load(&shared);
        assert(temp1 == temp2);
        atomic_store(&shared, index);
    #endif

    lock_release(&lock, index);
    
    return NULL;
}

int main()
{
    pthread_t t[NTHREADS];

    lock_init(&lock, NTHREADS);

    for (int i = 0; i < NTHREADS; i++)
        pthread_create(&t[i], 0, thread_n, (void *)(size_t)i);

    for (int i = 0; i < NTHREADS; i++)
        pthread_join(t[i], 0);
    

    #ifdef LS_LITMUS
        assert(sum == NTHREADS);    
    #endif

    return 0;
}
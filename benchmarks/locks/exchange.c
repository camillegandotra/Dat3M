#include <pthread.h>
#include "exchange.h"
#include <assert.h>

#ifndef NTHREADS
#define NTHREADS 2
#endif

// int shared;
exchangelock lock;
_Atomic int sum = 0;
int temp;

void *thread_n(void *arg)
{
    intptr_t index = ((intptr_t) arg);

    exchangelock_acquire(&lock);
    // shared = index;
    // int r = shared;
    // assert(r == index);
    temp = atomic_load(&sum);
    atomic_store(&sum, temp + 1);
    exchangelock_release(&lock);
    return NULL;
}

int main()
{
    pthread_t t[NTHREADS];

    exchangelock_init(&lock);

    for (int i = 0; i < NTHREADS; i++)
        pthread_create(&t[i], 0, thread_n, (void *)(size_t)i);

    for (int i = 0; i < NTHREADS; i++)
        pthread_join(t[i], 0);

    assert(sum == NTHREADS);

    return 0;
}

#include <pthread.h>
#include <assert.h>

// #define ACQ2RX
// #define REL2RX
#include "cas.h"

#ifndef NTHREADS
#define NTHREADS 2
#endif

// int shared;
caslock lock;
_Atomic int sum = 0;
int temp;


void *thread_n(void *arg)
{
    intptr_t index = ((intptr_t) arg);

    caslock_acquire(&lock);
    // shared = index;
    // int r = shared;
    // assert(r == index);
    temp = atomic_load(&sum);
    atomic_store(&sum, temp + 1);
    caslock_release(&lock);
    return NULL;
}

int main()
{
    pthread_t t[NTHREADS];

    caslock_init(&lock);

    for (int i = 0; i < NTHREADS; i++)
        pthread_create(&t[i], 0, thread_n, (void *)(size_t)i);

    for (int i = 0; i < NTHREADS; i++)
        pthread_join(t[i], 0);

    assert(sum == NTHREADS);

    return 0;
}

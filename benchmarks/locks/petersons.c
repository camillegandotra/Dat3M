#include <pthread.h>
#include <assert.h>

// #define ACQ2RX
// #define REL2RX
#include "petersons.h"

#ifndef NTHREADS
#define NTHREADS 2
#endif

// int shared;
petersonslock lock;
_Atomic int sum = 0;
int temp;


void *thread_n(void *arg)
{
    intptr_t index = ((intptr_t) arg);

    petersonslock_acquire(&lock, index);
    // shared = index;
    // int r = shared;
    // assert(r == index);
    temp = atomic_load(&sum);
    atomic_store(&sum, temp + 1);
    petersonslock_release(&lock, index);
    return NULL;
}

int main()
{ 
    pthread_t t[NTHREADS];

    petersonslock_init(&lock, NTHREADS);

    for (int i = 0; i < NTHREADS; i++)
        pthread_create(&t[i], 0, thread_n, (void *)(size_t)i);

    for (int i = 0; i < NTHREADS; i++)
        pthread_join(t[i], 0);

    assert(sum == NTHREADS);

    return 0;
}

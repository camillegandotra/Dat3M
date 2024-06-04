#include <pthread.h>
#include <assert.h>

#ifndef NTHREADS
#define NTHREADS 5
#endif

#include "SROBarrier.h"

srobarrier SROB;
_Atomic int sum = 0;


void *thread_n(void *arg)
{
    intptr_t index = ((intptr_t) arg);
    
    atomic_fetch_add(&sum, 1);

    srobarrier_barrier(&SROB, index);

    assert(atomic_load(&sum) == NTHREADS);
}

int main()
{
    pthread_t t[NTHREADS];

    srobarrier_init(&SROB, NTHREADS);

    for (int i = 0; i < NTHREADS; i++)
        pthread_create(&t[i], 0, thread_n, (void *)(size_t)i);

    for (int i = 0; i < NTHREADS; i++)
        pthread_join(t[i], 0);

    assert(sum == NTHREADS);

    return 0;
}

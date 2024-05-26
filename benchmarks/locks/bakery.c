#include <pthread.h>
#include <assert.h>
#include <stdio.h>

// #define ACQ2RX
// #define REL2RX
#include "bakery.h"

#ifndef NTHREADS
#define NTHREADS 2
#endif

#define LS_LITMUS

// int shared;
bakerylock lock;
_Atomic int sum = 0;
_Atomic int shared = -1;

void *thread_n(void *arg)
{
    intptr_t index = ((intptr_t) arg);

    bakerylock_acquire(&lock, index);
    
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

    bakerylock_release(&lock, index);
    
    return NULL;
}

int main()
{
    pthread_t t[NTHREADS];

    bakerylock_init(&lock, NTHREADS);

    for (int i = 0; i < NTHREADS; i++)
        pthread_create(&t[i], 0, thread_n, (void *)(size_t)i);

    for (int i = 0; i < NTHREADS; i++)
        pthread_join(t[i], 0);
    

    #ifdef LS_LITMUS
        assert(sum == NTHREADS);    
    #endif

    return 0;
}
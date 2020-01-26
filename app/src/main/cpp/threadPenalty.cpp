/*
 *  Copyright (c) 2020 Dennis Lang(LanDen Labs) landenlabs@gmail.com
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 *  associated documentation files (the "Software"), to deal in the Software without restriction, including
 *  without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 *  following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all copies or substantial
 *  portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 *  LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 *  NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 *  SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *  @author Dennis Lang  (Jan-2020)
 *  @see http://landenlabs.com
 *
 */
#include <pthread.h>
#include <sys/time.h>
#include <unistd.h>
#include <sys/resource.h>

typedef pthread_t Thread_t;
typedef void *Thread_return_t;


#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <math.h>
#include <memory.h>
#include "plog/plog.hpp"
#include "appJniMethods.hpp"

#define USECSPERSEC 1000000
const pthread_attr_t *pthread_attr_default = NULL;
#define MAX_THREADS 50

unsigned long g_distance = 1;
unsigned long g_iteration_count = 300000000;
unsigned int g_thread_count = 4;

typedef unsigned long Counter_t;
typedef unsigned long Counter_t;

Counter_t *g_pCounter = 0;
volatile Counter_t g_value[MAX_THREADS];
volatile double g_elapsed[MAX_THREADS];
volatile pthread_t g_apicId[MAX_THREADS];

typedef struct timeval Tics_t;
typedef unsigned int WORD;
const char TAG[] = "ThreadPenalty";

/* ------------------------------------------------------------------------- */
void get_time(Tics_t *pTics) {
    gettimeofday(pTics, NULL);
}

/* ------------------------------------------------------------------------ */
double secondsElapsed(Tics_t end, Tics_t start) {
    Tics_t delta;

    delta.tv_sec = end.tv_sec - start.tv_sec;
    delta.tv_usec = end.tv_usec - start.tv_usec;

    if (delta.tv_usec < 0) {
        delta.tv_sec--;
        delta.tv_usec += USECSPERSEC;
    }

    return delta.tv_sec + (double) delta.tv_usec / 1e6;
}

/* ------------------------------------------------------------------------- */
Thread_return_t runLocalityTest(void *who_data) {
    int who = (int) (unsigned long) who_data;
    volatile Counter_t &counter = g_pCounter[who * (g_distance)];
    Counter_t finalCounter = 0;

    unsigned long i;
    unsigned long total_iterations = g_iteration_count;
    Tics_t start, end;
    double elapsed;

    // Run test
    get_time(&start);

    for (i = 0; i < total_iterations; i++) {
        // Increment counter which may be shared by other threads or maybe
        // 'distance' fields apart.
        counter++;
        // Exercise counter to slow down the loop some.
        if ((counter & 0x5555) == counter << 2)
            counter++;

        finalCounter = counter;
    }

    get_time(&end);

    elapsed = secondsElapsed(end, start);

    g_value[who] = finalCounter;
    g_elapsed[who] = elapsed;
    g_apicId[who] = pthread_self();
    pthread_exit(0);
}

bool sortElepased (double i, double j) { return (i<j); }

/* ------------------------------------------------------------------------- */
double measureThreadPenalty(JNIEnv* env, bool setThreadAffinity, WORD processorCount) {
    Tics_t start, end;
    double deltaSeconds;
    unsigned int threadIdx;
    Thread_t thread[MAX_THREADS];

    // Allocate memory region where threads will update their counters.
    // User defines how far apart the counters sit in memory.
    // Distance is in units of sizeof(Counter_t).
    // Force allocation to a multiple of 4096 because it feels right.
    unsigned long request_size = (g_distance + 1) * sizeof(Counter_t) * MAX_THREADS + 4;
    request_size = (request_size | 0xfff) + 1;    // force to multiples of 4096
    g_pCounter = (Counter_t *) malloc(request_size);
    memset(g_pCounter, 0, request_size);
    // Get a pointer which starts on a 4byte boundary.
    g_pCounter = (Counter_t *) ((unsigned long) g_pCounter | 0x3) + 1;    // force to 4 byte boundary

    // Invoke the tests
    get_time(&start);
    for ( threadIdx = 0; threadIdx < g_thread_count; threadIdx++) {
        if (pthread_create(&(thread[threadIdx]), pthread_attr_default,
                &runLocalityTest,  (void *) (unsigned long) threadIdx)) {
            PLog::printf(PLOG_ERROR, TAG, "Thread create failed, errno=%d", errno);
        }
    }

    // Wait for tests to finish
    for ( threadIdx = 0; threadIdx < g_thread_count; threadIdx++) {
        pthread_join(thread[threadIdx], NULL);
    }

    get_time(&end);
    deltaSeconds = secondsElapsed(end, start);

    PLog::printf(PLOG_INFO, TAG,
                 "Test took: %.6f seconds, distance:%u, iterations:%u, threads:%u\n",
                 deltaSeconds, (unsigned int) g_distance, (unsigned int) g_iteration_count,
                 g_thread_count);
    AppJniMethods::sendJavaMsg(env, "Completed Gap:%d Avg:%.6f seconds", g_distance, deltaSeconds);

    for ( threadIdx = 0; threadIdx < g_thread_count; threadIdx++) {
        PLog::printf(PLOG_INFO, TAG,
                     "%2d: seconds=%10.6f value=%8u tID=%04x\n",
                     threadIdx,
                     g_elapsed[threadIdx],
                     (unsigned int) g_value[threadIdx],
                     g_apicId[threadIdx]);
    }

    // Sort thread elapsed times
    // Discard smallest and largest
    // Return average of middle values.
    std::sort(&g_elapsed[0], &g_elapsed[g_thread_count], sortElepased);
    deltaSeconds = 0;
    for (unsigned idx = 1; idx < g_thread_count-1; idx++) {
        deltaSeconds += g_elapsed[idx];
    }
    return deltaSeconds / (g_thread_count-2);
}

// Iterate over gap range startGap..endGap by stepGap
// Store average milliseconds in gapTimedMilliseconds
void threadPenalty(JNIEnv* env, jint startGap, jint endGap, jint stepGap, size_t arrayLen, jlong gapTimedMilliseconds[]) {

    const WORD processorCount = 1;
    const bool setThreadAffinity = false;
    int idx = 0;
    g_distance = (unsigned)startGap;

    while (g_distance <= endGap) {
        double gapSeconds = measureThreadPenalty(env, setThreadAffinity, processorCount);
        if (idx < arrayLen) {
            gapTimedMilliseconds[idx] = (long)(gapSeconds * 1000);
            idx++;
        }
        g_distance += stepGap;
    }
}

#include <jni.h>

extern "C" {
JNIEXPORT void JNICALL
Java_com_landenlabs_allThreadPenalty_FragBottomNavOne_startThreadPenalty(
        JNIEnv* env, jobject instance,
        jint startGap, jint endGap, jint stepGap,
        jlongArray gapTimedMilliseconds) {

    jsize len = env->GetArrayLength(gapTimedMilliseconds);
    jlong *longArray = env->GetLongArrayElements(gapTimedMilliseconds, 0);

    threadPenalty(env, startGap, endGap, stepGap, (size_t)len, longArray);
    // AppJniMethods::sendJavaMsg(env, "Completed Gap:%d to %d by %d", startGap, endGap, stepGap);

    env->ReleaseLongArrayElements(gapTimedMilliseconds, longArray, JNI_COMMIT);
}
}
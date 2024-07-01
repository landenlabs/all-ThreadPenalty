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
 *  @see https://landenlabs.com
 *
 */

//
// Initialize Java / Cpp bridge
// Proved Tick thread example
//

#include <string.h>
#include <inttypes.h>
#include <pthread.h>
#include <jni.h>
#include <android/log.h>
#include <assert.h>

#include "plog/plog.h"

// Log wrappers to call Private Logger
static const char *kTAG = "PLogTagTm";
#define LOGI(...) \
  ((void)plogs_printf(PLOG_INFO, kTAG, __VA_ARGS__))
#define LOGW(...) \
  ((void)plogs_printf(PLOG_WARN, kTAG, __VA_ARGS__))
#define LOGE(...) \
  ((void)plogs_printf(PLOG_ERROR, kTAG, __VA_ARGS__))

#include "appJniMethods.hpp"


typedef struct tick_context {
    // JavaVM  *javaVM;
    // jclass   jniHelperClz;
    // jobject  jniHelperObj;
    jclass testFragClz;
    jobject testFragObj;
    pthread_mutex_t lock;
    bool interrupt; // Set to interrupt thread.
    int id;
} TickContext;
const int MAX_THREADS = 2;
TickContext g_ctx[MAX_THREADS];

/* This is a trivial JNI example where we use a native method
 * to return a new VM String.
 *
 * Return information about how this code was compiled.
 */
extern "C" JNIEXPORT jstring JNICALL
Java_com_landenlabs_allThreadPenalty_FragBottomNavOne_howCompiledMsg(JNIEnv *env, jobject thiz) {
#if defined(__arm__)
#if defined(__ARM_ARCH_7A__)
#if defined(__ARM_NEON__)
#if defined(__ARM_PCS_VFP)
#define ABI "armeabi-v7a/NEON (hard-float)"
#else
#define ABI "armeabi-v7a/NEON"
#endif
#else
#if defined(__ARM_PCS_VFP)
#define ABI "armeabi-v7a (hard-float)"
#else
#define ABI "armeabi-v7a"
#endif
#endif
#else
#define ABI "armeabi"
#endif
#elif defined(__i386__)
#define ABI "x86"
#elif defined(__x86_64__)
#define ABI "x86_64"
#elif defined(__mips64)  /* mips64el-* toolchain defines __mips__ too */
#define ABI "mips64"
#elif defined(__mips__)
#define ABI "mips"
#elif defined(__aarch64__)
#define ABI "arm64-v8a"
#else
#define ABI "unknown"
#endif
    return env->NewStringUTF(ABI);
}

AppJniMethods javaBridge;

// C++ example calling Java methods
void queryRuntimeInfo(JNIEnv *env, jobject instance) {
    // Find out which OS we are running on. It does not matter for this app
    // LOGI("Android Version - %s", javaBridge.getVersion().c_str());

    // Query available memory size from a non-static public function
    unsigned long  result = (unsigned long)javaBridge.getMemory(instance);
    LOGI("Runtime free memory size: %lu" , result);
}

// Java bridge initialization called automatically
/*
 * processing one time initialization:
 *     Cache the javaVM into our context
 *     Find class ID for JniHelper
 *     Create an instance of JniHelper
 *     Make global reference since we are using them from a native thread
 * Note:
 *     All resources allocated here are never released by application
 *     we rely on system to free all global refs when it goes away;
 *     the pairing function JNI_OnUnload() never gets called at all.
 */
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {

    // 1. Setup Java bridge (JNI)
    JNIEnv *env;
    javaBridge = AppJniMethods::init(vm, env);

    // 2. Demonstrate calling some Java methods (just for fun)
    queryRuntimeInfo(env, javaBridge.appContext.jniHandlerObj);

    // 3. Initialize ThreadPenalty data structure.
    memset(&g_ctx, 0, sizeof(g_ctx));
    for (int idx = 0; idx < MAX_THREADS; idx++) {
        g_ctx[idx].interrupt = false;
        g_ctx[idx].testFragObj = NULL;
        g_ctx[idx].id = idx;
    }

    return JNI_VERSION_1_6;
}

/*
 * For Demonstration only (not part of Thread Locality Penalty)
 *
 * C++ method called by a pthread which in turn calls up to Java every 'n' seconds.
 * From a pthread,
 *     calling back to javaClass::updateTimer() to display ticks on UI
 *     call PLog to test private logger.
 *     calling back to JniHelper::updateStatus(String msg) for msg
 */
void* UpdateTicks(void *context) {
    TickContext *pctx = (TickContext *) context;
    JNIEnv* threadEnv;
    javaBridge.attachThread(threadEnv);

    // Get  "updateTimer" function on its associated class instance.
    javaBridge.updateTimerJavaId = threadEnv->GetMethodID(pctx->testFragClz,  "updateTimer", "(I)V");

    // Two possible time intervals, selected by parameter passed inside TickContext.
    struct timeval beginTime, curTime, usedTime, leftTime;
    const struct timeval kOneSecond = {
            (__kernel_time_t) 1,
            (__kernel_suseconds_t) 0
    };
    const struct timeval kTwoSeconds = {
            (__kernel_time_t) 2,
            (__kernel_suseconds_t) 0     // micro seconds
    };

    AppJniMethods::sendJavaMsg(threadEnv, "TickerThread[%d] Started", pctx->id);

    // Loop until interrupted.
    while (1) {
        gettimeofday(&beginTime, NULL);
        pthread_mutex_lock(&pctx->lock);
        bool interrupt = pctx->interrupt;
        if (pctx->interrupt) {
            pctx->interrupt = false;
        }
        pthread_mutex_unlock(&pctx->lock);
        if (interrupt) {
            break;
        }

        if (pctx->id == 0) {
            // Silly test of plogs printf
            plogs_printf(PLOG_DEBUG, kTAG,  "UpdateTicks %d %s %s %s %s", pctx->id, "This",  "is", "a", "test.");
        }

        threadEnv->CallVoidMethod(pctx->testFragObj, javaBridge.updateTimerJavaId, pctx->id);

        gettimeofday(&curTime, NULL);
        timersub(&curTime, &beginTime, &usedTime);
        timersub((pctx->id == 0 ? &kOneSecond : &kTwoSeconds), &usedTime, &leftTime);
        struct timespec sleepTime;
        sleepTime.tv_sec = leftTime.tv_sec;
        sleepTime.tv_nsec = leftTime.tv_usec * 1000;

        if (sleepTime.tv_sec <= 1) {
            nanosleep(&sleepTime, NULL);
        } else {
            AppJniMethods::sendJavaMsg(threadEnv, "TickerThread error: processing too long!");
        }
    }

    AppJniMethods::sendJavaMsg(threadEnv, "TickerThread[%d] stopped", pctx->id);
    javaBridge.detachThread();
    return context;
}

// Example thread which periodically calls a Java side method every 'n' seconds.
// Java callable -
extern "C" JNIEXPORT void JNICALL
Java_com_landenlabs_allThreadPenalty_FragBottomNavOne_startTickThread(
        JNIEnv *env, jobject instance, jint threadIdx) {

    if (threadIdx >= MAX_THREADS) {
        plogs_printf(PLOG_ERROR, kTAG, "startTicks bad threadIdx %d, range 0..%d", threadIdx, MAX_THREADS-1);
        return;
    }
    pthread_t threadInfo_;
    pthread_attr_t threadAttr_;

    pthread_attr_init(&threadAttr_);
    pthread_attr_setdetachstate(&threadAttr_, PTHREAD_CREATE_DETACHED);

    pthread_mutex_init(&g_ctx[threadIdx].lock, NULL);

    jclass clz = env->GetObjectClass(instance);
    g_ctx[threadIdx].testFragClz = (jclass)env->NewGlobalRef(clz);
    g_ctx[threadIdx].testFragObj = (jobject)env->NewGlobalRef(instance);

    int result = pthread_create(&threadInfo_, &threadAttr_, UpdateTicks, &g_ctx[threadIdx]);
    assert(result == 0);

    pthread_attr_destroy(&threadAttr_);
}

// Stop tick timer thread.
// Java callable -
extern "C" JNIEXPORT void JNICALL
Java_com_landenlabs_allThreadPenalty_FragBottomNavOne_stopTickThread(JNIEnv *env, jobject instance, jint idx) {
    pthread_mutex_lock(&g_ctx[idx].lock);
    g_ctx[idx].interrupt = true;
    pthread_mutex_unlock(&g_ctx[idx].lock);

    // waiting for ticking thread to flip the done flag
    struct timespec sleepTime;
    memset(&sleepTime, 0, sizeof(sleepTime));
    sleepTime.tv_nsec = 100000000;
    while (g_ctx[idx].interrupt) {
        nanosleep(&sleepTime, NULL);
    }

    // release object we allocated from StartTicks() function
    env->DeleteGlobalRef( g_ctx[idx].testFragClz);
    env->DeleteGlobalRef( g_ctx[idx].testFragObj);
    g_ctx[idx].testFragObj = NULL;
    g_ctx[idx].testFragClz = NULL;

    pthread_mutex_destroy(&g_ctx[idx].lock);
}

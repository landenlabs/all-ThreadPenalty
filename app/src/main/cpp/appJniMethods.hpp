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
#pragma once

//
//  Helper class to manage Java bridge
//

#include "common.hpp"
#include <jni.h>

// processing callback to handler class
typedef struct app_context {
    JavaVM *javaVM;
    JNIEnv *env;
    jclass jniHandlerClz;   // JniHandler class
    jobject jniHandlerObj;  // JniHandler  instance
    jclass   testFragClz;
    jobject  testFragObj;
    // pthread_mutex_t  lock;
    // int      done;
} AppContext;


#ifdef __cplusplus

#include <string>
#include <cstdarg>

class AppJniMethods {
public:
    static AppJniMethods& get() {
        static AppJniMethods instance;
        return instance;
    }

    AppContext appContext;

    // JniHandler methods
    jmethodID updateStatusJavaId;
    jmethodID versionFuncStatic;
    jmethodID memFunc;

    // FragBottomNavOne methods
    jmethodID updateTimerJavaId;

    static AppJniMethods& init( JavaVM *vm,  JNIEnv*& env) {
        get().privateInit(vm, env);
        return get();
    }

    int privateInit(JavaVM *vm,  JNIEnv*& env) {
        memset(&appContext, 0, sizeof(appContext));
        appContext.javaVM = vm;
        if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
            return JNI_ERR; // JNI version not supported.
        }
        appContext.env = env;

        jclass clz = env->FindClass("com/landenlabs/allThreadPenalty/penalty/JniHandler");
        appContext.jniHandlerClz = (jclass)env->NewGlobalRef(clz);
        jmethodID jniHandlerCtor = env->GetMethodID(appContext.jniHandlerClz, "<init>", "()V");
        jobject handlerObj = env->NewObject(appContext.jniHandlerClz, jniHandlerCtor);
        appContext.jniHandlerObj = env->NewGlobalRef(handlerObj);

        try {
            versionFuncStatic = env->GetStaticMethodID(
                    appContext.jniHandlerClz, "getBuildVersion", "()Ljava/lang/String;");
            memFunc = env->GetMethodID(
                    appContext.jniHandlerClz, "getRuntimeMemorySize", "()J");
            updateStatusJavaId = env->GetMethodID(
                    appContext.jniHandlerClz, "updateStatus", "(Ljava/lang/String;)V");
        } catch (const std::runtime_error& e) {
            // LOGE("%s", e.what());
        } catch (const std::exception& e) {
            // LOGE("%s", e.what());
        }

        // LOGI("jni init done");
        return 0;
    }

    void attachThread(JNIEnv*& env) {
        JavaVM *javaVM = appContext.javaVM;
        jint res = (jint)javaVM->GetEnv((void **) &env, JNI_VERSION_1_6);
        if (res != JNI_OK) {
            res = javaVM->AttachCurrentThread(&env, NULL);
            if (JNI_OK != res) {
                // LOGE("Failed to AttachCurrentThread, ErrorCode = %d", res);
            }
        }
    }

    void detachThread() {
        appContext.javaVM->DetachCurrentThread();
    }

    // Helper - get app version info.
    std::string version;
    const std::string& getVersion() {
        JNIEnv *env = appContext.env;
        jstring versionJref = (jstring)env->CallStaticObjectMethod(appContext.jniHandlerClz, versionFuncStatic);
        const char * versionP = env->GetStringUTFChars(versionJref, NULL);
        if (versionP != nullptr) {
            version = versionP;
            env->ReleaseStringUTFChars(versionJref, versionP);
            env->DeleteLocalRef(versionJref);
        }
        return version;
    }

    long getMemory(jobject instance) {
        JNIEnv *env = appContext.env;
        jlong result = env->CallLongMethod(instance, memFunc);
        return result;
    }

    /*
     * A helper function to wrap java JniHelper::updateStatus(String msg)
     * JNI allow us to call this function via an instance even it is
     * private function.
     */
    static void sendJavaMsg(JNIEnv* threadEnv, char const *const fmt, ...) {
        va_list args;
        va_start(args, fmt);
        char msg[200];
        vsnprintf(msg, sizeof(msg), fmt, args);
        va_end(args);

        AppJniMethods appJniMethods = get();
        jstring javaMsg = (jstring)threadEnv->NewStringUTF(msg);
        threadEnv->CallVoidMethod(appJniMethods.appContext.jniHandlerObj, appJniMethods.updateStatusJavaId, javaMsg);
        threadEnv->DeleteLocalRef(javaMsg);
    }
};
#endif
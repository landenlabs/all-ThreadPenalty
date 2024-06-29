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

// ----------------- Java bridge [ PLOG ] ------------------
#include "plog/plog.hpp"

extern "C" {

JNIEXPORT void JNICALL
Java_com_landenlabs_allThreadPenalty_log_PLog_setLevel(JNIEnv *env, jobject instance, jint level) {
    PLog::setLevel((PLOG_LEVELS) level);
}

JNIEXPORT void JNICALL
Java_com_landenlabs_allThreadPenalty_log_PLog_print(JNIEnv *env, jobject instance, jint level, jstring tag,
                                             jstring msg) {
    const char *tagStr = env->GetStringUTFChars(tag, 0);
    const char *msgStr = env->GetStringUTFChars(msg, 0);
    PLog::print((PLOG_LEVELS) level, tagStr, msgStr);
    env->ReleaseStringUTFChars(msg, msgStr);
    env->ReleaseStringUTFChars(tag, tagStr);
}
}


// ----------------- Java bridge [ PLOGS ] ------------------

#include "plogs/plogs.hpp"
#include "plogs/plogCollection.hpp"

extern "C" {

JNIEXPORT void JNICALL
Java_com_landenlabs_allThreadPenalty_log_PLogs_setLevel(JNIEnv *env, jobject instance, jint level) {
    PLOG_BRIDGE.setLevel((PLOG_LEVELS) level);
}

JNIEXPORT void JNICALL
Java_com_landenlabs_allThreadPenalty_log_PLogs_print(JNIEnv *env, jobject instance, jint level, jstring tag, jstring msg) {
    const char *tagStr = env->GetStringUTFChars(tag, 0);
    const char *msgStr = env->GetStringUTFChars(msg, 0);
    PLOG_BRIDGE.print((PLOG_LEVELS) level, tagStr, msgStr);
    env->ReleaseStringUTFChars(msg, msgStr);
    env->ReleaseStringUTFChars(tag, tagStr);
}
}

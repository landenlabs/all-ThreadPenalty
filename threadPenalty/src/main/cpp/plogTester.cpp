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
#include "plog/plog.hpp"
#include "plogs/plogs.hpp"
#include "plogs/plogCollection.hpp"

void plogTester() {
    PLog::setLevel(PLOG_INFO);

    PLog::printf(PLOG_DEBUG, "PLogTag", "TESTER - format %s %s", "hello", "debug");
    PLog::printf(PLOG_INFO, "PLogTag", "TESTER - format %s %s", "hello", "info");
    PLog::print(PLOG_WARN, "PLogTag", "TESTER - Warning message");
}

void plogsTester() {

    PLog::print(PLOG_INFO, "PLogTag", "Test PLogs");

    PLOG_LAYERS.setAllLevels(PLOG_INFO);
    PLOG_LAYERS.printf(PLOG_INFO, "PLogTag", "TESTER (layers=sys) - format %s %s", "hello", "info");
    PLOG_START.printf(PLOG_WARN, "PLogTag", "TESTER (start=file) - format %s %s", "hello", "info");
    PLOG_RENDER.printf(PLOG_ERROR, "PLogTag", "TESTER (render=none) - format %s %s", "hello",
                       "info");
}


extern "C" {
JNIEXPORT void JNICALL
Java_com_landenlabs_allThreadPenalty_FragBottomNavOne_plogTester(/* JNIEnv *env, jobject instance */) {
    plogsTester();
}

}

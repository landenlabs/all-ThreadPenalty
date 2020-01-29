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
#include "plog.hpp"

#if CODE_TARGET_PLATFORM(IOS) || CODE_TARGET_PLATFORM(WINDOWS)
#include <cstdio>
#endif

#if CODE_TARGET_PLATFORM(ANDROID)
#include <android/log.h>
#endif

// namespace AllThreadTest::v2 {

PLog &PLog::instance() {
    static PLog plog;
    // __android_log_print(PLOG_INFO, "PLog", "PLog class=%s level=%d", typeid(plog).name(), plog.minLevel_);
    return plog;
}

PLog::PLog() : minLevel_(PLOG_SILENT) {
}

PLog::~PLog() noexcept = default;

void PLog::setLevel(PLOG_LEVELS level) {
    instance().minLevel_ = level;
}

#if CODE_TARGET_PLATFORM(ANDROID)
// -------------------------------------------------------------------------------------------------
// Android C++

void PLog::printf(PLOG_LEVELS level, char const *const tag, char const *const format, ...) {

    if (level >= instance().minLevel_) {
        va_list args;
        va_start(args, format);
        // char msg[200];
        // vsnprintf(msg, sizeof(msg), format, args);
        // __android_log_write(level, tag, msg);
        __android_log_vprint(level, tag, format, args);
        va_end(args);
    }
}

void PLog::print(PLOG_LEVELS level, char const *const tag, char const *const msg) {
    if (level >= instance().minLevel_) {
        __android_log_write(level, tag, msg);
    }
}

// Used by C-bridge
void PLog::printv(PLOG_LEVELS level, char const *const tag, char const *format, va_list &args) {
    if (level >= instance().minLevel_) {
        __android_log_vprint(level, tag, format, args);
    }
}


#elif CODE_TARGET_PLATFORM(IOS) || CODE_TARGET_PLATFORM(WINDOWS) || CODE_TARGET_PLATFORM(OSX)
void PLog::printf(PLOG_LEVELS level, char const* const tag, char const* const format, ...) {
    va_list args;
    va_start(args, format);
    printv(level, tag, format, args);
    va_end(args);
}
void PLog::print(PLOG_LEVELS level, char const* const tag, char const* const msg) {
    if (level >= instance().minLevel_) {
        puts(msg);
    }
}
void PLog::printv(PLOG_LEVELS level, char const* const tag, char const* format, va_list& args) {
{
    if (level >= instance().minLevel_) {
        std::unique_lock lock{instance().mutex_};
        vprintf(format, args);
        puts("");
    }
}
#endif

// ----------------- C bridge ------------
// TODO - Remove if no "C" code support requried.
#include "plogs/plogCollection.hpp"

extern "C" {
// "C" callable plog printf
void plogs_printf(PLOG_LEVELS level, char const *const tag, char const *const format, ...) {

    va_list args;
    va_start(args, format);
    // __android_log_write(level, tag, format);
    // __android_log_vprint(level, tag, format, args);
    PLOG_BRIDGE.printv(level, tag, format, args);
    va_end(args);
}

void plogs_setLevel(PLOG_LEVELS level) {
    PLOG_BRIDGE.setLevel(level);
}
}

// } // namespace PLOG
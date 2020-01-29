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
#include "plogs.hpp"
#include <vector>

#if CODE_TARGET_PLATFORM(IOS) || CODE_TARGET_PLATFORM(WINDOWS)

#    include <cstdio>

#endif

#if CODE_TARGET_PLATFORM(ANDROID)

#    include <android/log.h>

#endif

// namespace PLog {
PLOG_LEVELS PLogs::minLevel_ = PLOG_SILENT;
static std::vector<PLogs *> AVAILABLE_PLOGS(10);

// Default log configuration and track active instances, see setAllLevels()
PLogs::PLogs(char const *const id, PLOG_LEVELS level) : id_(id) {
    setLevel(level);
    AVAILABLE_PLOGS.push_back(this);
}

PLogs::~PLogs() noexcept = default;

// Set minimum log level in all instance of PLogXXXXX, see plogCollection.hpp
void PLogs::setAllLevels(PLOG_LEVELS level) {
    minLevel_ = level;
    for (PLogs *plogsPtr : AVAILABLE_PLOGS) {
        plogsPtr->setLevel(level);
    }
}

// Deprecated - Parent classes log level only used if calling PLogs directly.
// See setAllLevels() or setLevel on specific PLogXXXX instances.
void PLogs::setLevel(PLOG_LEVELS level) {
    minLevel_ = level;
}

#if CODE_TARGET_PLATFORM(ANDROID)

void PLogs::printf(PLOG_LEVELS level, char const *const tag, char const *const format, ...) {
    if (level >= minLevel_) {
        va_list args;
        va_start(args, format);
        __android_log_vprint(level, tag, format, args);
        va_end(args);
    }
}

void PLogs::print(PLOG_LEVELS level, char const *const tag, char const *const msg) {
    if (level >= minLevel_) {
        __android_log_write(level, tag, msg);
    }
}

// Used by C-bridge
void PLogs::printv(PLOG_LEVELS level, char const *const tag, char const *format, va_list &args) {
    if (level >= minLevel_) {
        __android_log_vprint(level, tag, format, args);
    }
}


#elif CODE_TARGET_PLATFORM(IOS) || CODE_TARGET_PLATFORM(WINDOWS) || CODE_TARGET_PLATFORM(OSX)

void PLogs::printf(PLOG_LEVELS level, char const* const tag, char const* const format, ...)  {
    va_list args;
    va_start(args, format);
    printv(level, tag, format, args);
    va_end(args);
}
void PLogs::print(PLOG_LEVELS level, char const* const tag, char const* const msg)  {
    if (level >= minLevel_) {
        puts(msg);
    }
}
void PLogs::printv(PLOG_LEVELS level, char const* const tag, char const* format, va_list& args)  {
{
    static std::mutex mutex;
    if (level >= minLevel_) {
        std::unique_lock lock{mutex};
        vprintf(format, args);
        puts("");
    }
}
#endif


// } // namespace PLog
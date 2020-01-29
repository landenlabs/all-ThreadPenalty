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
#include "plogfile.hpp"
#include <mutex>

// DEBUG messages emitted by this class
#include "plog/plog.hpp"

static std::mutex mutex;
#if CODE_TARGET_PLATFORM(ANDROID)

// Send formatted message to file log.
void PLogFile::printf(PLOG_LEVELS level, char const *const tag, char const *const format, ...) {
    if (level >= minLevel_) {
        std::lock_guard<std::mutex> lockGuard(mutex);
        if (open()) {
            writeTimeLevelTag(level, tag);
            va_list args;
            va_start(args, format);
            vfprintf(file_, format, args);
            va_end(args);
            flush();
        }
    }
}

// Send message to file log.
void PLogFile::print(PLOG_LEVELS level, char const *const tag, char const *const msg) {
    if (level >= minLevel_) {
        std::lock_guard<std::mutex> lockGuard(mutex);
        if (open()) {
            writeTimeLevelTag(level, tag);
            fputs(msg, file_);
            flush();
        }
    }
}

// Used by C-bridge
void PLogFile::printv(PLOG_LEVELS level, char const *const tag, char const *format, va_list &args) {
    if (level >= minLevel_) {
        std::lock_guard<std::mutex> lockGuard(mutex);
        if (open()) {
            writeTimeLevelTag(level, tag);
            vfprintf(file_, format, args);
            flush();
        }
    }
}

// Open logfile in append mode (see accessMode_)
bool PLogFile::open() {
    if (file_ == NULL) {
        file_ = fopen(filename_, accessMode_);
        if (file_ != NULL) {
            PLogs::printf(PLOG_INFO, "PLogFile", "Opened file %s", filename_);
        } else {
            int err = errno;
            PLogs::printf(PLOG_ERROR, "PLogFile", "Opened FAILED on %s error %d", filename_, err);
        }
    }
    return file_ != NULL;
}

// Prefix log message with standard Android log time, level anc category
void PLogFile::writeTimeLevelTag(PLOG_LEVELS level, const char *tag) const {
    if (file_ != NULL) {
        int pid = 0;
        time_t nowTime;
        struct tm *nowTm;
        char tmStr[80];
        time(&nowTime);
        nowTm = localtime(&nowTime);
        strftime(tmStr, sizeof(tmStr), "%m-%d %h:%M:%H:%S", nowTm);
        fprintf(file_, "%s %d %d %c %s : ", tmStr, pid, pid, levelId(level), tag);
    }
}

// Write line termination and flush file buffer, optionally close and truncate if too large.
void PLogFile::flush() {
    if (file_ != NULL) {
        fputs("\n", file_);
        fflush(file_);
        size_t pos = ftell(file_);
        // PLogs::printf(PLOG_INFO, "PLogFile", "Flush %s at %u", filename_, (unsigned int)pos);
        if (pos >= maxLength) {
            close();
            accessMode_ = "w+"; // Truncate on next message request
        }
    }
}

// Close file and set file to null
void PLogFile::close() {
    if (file_ != NULL) {
        // PLogs::printf(PLOG_INFO, "PLogFile", "Close %s length %u", filename_, (unsigned int)ftell(file_));
        fclose(file_);
        file_ = NULL;
    }
}

#endif
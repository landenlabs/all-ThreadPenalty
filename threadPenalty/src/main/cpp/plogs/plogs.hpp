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

#include "common.hpp"
#include <typeinfo>

#include "plogLevels.h"


class PLogs {
public:
    // Set minimum level on all PLogXXXX instances, defaults to SILENT
    // Each individual PLogXXXX instance can override its private value
    static void setAllLevels(PLOG_LEVELS level);

    void setLevel(PLOG_LEVELS level);

    // Log message if level at or above minimum set, see setLevel()
    [[gnu::format(printf, 4, 5)]] void
    printf(PLOG_LEVELS level, char const *const tag, char const *const fmt, ...);

    void print(PLOG_LEVELS level, char const *const tag, char const *const msg);

    // Used by java and c bridge
    void printv(PLOG_LEVELS level, char const *const tag, char const *const fmt, va_list &args);

    void toString(const char *msg) const;

protected:
    PLogs(char const *const id, PLOG_LEVELS level = PLOG_SILENT);

    virtual ~PLogs() noexcept;

    char const *id_;
    static PLOG_LEVELS minLevel_;
};



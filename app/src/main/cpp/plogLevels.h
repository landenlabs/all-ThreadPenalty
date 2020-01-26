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


// Move enum into plog.hpp if "C" access is not required.
enum _PLOG_LEVELS {
    PLOG_UNKNOWN = 0,
    PLOG_DEFAULT,
    PLOG_VERBOSE,
    PLOG_DEBUG,
    PLOG_INFO,
    PLOG_WARN,
    PLOG_ERROR,
    PLOG_FATAL,
    PLOG_SILENT
};
typedef enum _PLOG_LEVELS PLOG_LEVELS;

inline char levelId(PLOG_LEVELS plogLevel) {
    static const char LEVELS[] = {'?', '-', 'V', 'D', 'I', 'W', 'E', 'F'};
    return LEVELS[plogLevel & 0x7];
}
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
package com.landenlabs.allThreadPenalty.log;

/**
 * Java bridge to access Private Logger
 */
public class PLog {

    // Keep in sync with PLOG_LEVELS enum
    public static int PLOG_DEBUG = 3;
    public static final int PLOG_INFO = 4;
    public static int PLOG_WARN = 5;
    public static final int PLOG_ERROR = 6;
    public static int PLOG_FATAL = 7;
    public static int PLOG_SILENT = 8;


    public static native void setLevel(int level);

    public static native void print(int level, String tag, String msg);

    public static void setMinLevel(int level) {
        setLevel(level);
    }

    public static void printf(int level, String tag, String fmt, Object... args) {
        print(level, tag, String.format(fmt, args));
    }
}

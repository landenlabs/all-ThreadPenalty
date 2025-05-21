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
 *  @see https://LanDenLabs.com
 *
 */
package com.landenlabs.allThreadPenalty.penalty;

import android.os.Build;
import android.util.Log;

import androidx.annotation.Keep;

/*
 * A helper class to demo that JNI could call into:
 *     private non-static function
 *     public non-static function
 *     static public function
 * The calling code is inside cppcode.c
 */
public class JniHandler {

    public interface ShareMsg {
        void message(String msg);
    }

    public static ShareMsg shareMsg;

    // Print out status to logcat
    // Called by cpp code.
    @Keep
    public void updateStatus(String msg) {
        if (msg.toLowerCase().contains("error")) {
            Log.e("JniHandler", "Native Err: " + msg);
        } else {
            Log.i("JniHandler", "Native Msg: " + msg);
        }

        if (shareMsg != null) {
            shareMsg.message(msg);
        }
    }

    // Return OS build version: a static function
    // Called by cpp code.
    @Keep
    public static String getBuildVersion() {
        return Build.VERSION.RELEASE;
    }


    //  Return Java memory info
    // Called by cpp code
    @Keep
    public long getRuntimeMemorySize() {
        return Runtime.getRuntime().freeMemory();
    }
}

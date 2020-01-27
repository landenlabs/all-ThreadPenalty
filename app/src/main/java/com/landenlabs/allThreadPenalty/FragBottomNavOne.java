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
package com.landenlabs.allThreadPenalty;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.jjoe64.graphview.GraphView;
import com.landenlabs.allThreadPenalty.log.PLog;
import com.landenlabs.allThreadPenalty.log.PLogs;
import com.landenlabs.allThreadPenalty.penalty.Events;
import com.landenlabs.allThreadPenalty.penalty.JniHandler;
import com.landenlabs.allThreadPenalty.penalty.LineGraph;
import com.landenlabs.allThreadPenalty.util.ShareUtil;

import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.landenlabs.allThreadPenalty.log.PLog.PLOG_ERROR;
import static com.landenlabs.allThreadPenalty.log.PLog.PLOG_INFO;
import static com.landenlabs.allThreadPenalty.log.PLogs.PLOGS_DEBUG;
import static com.landenlabs.allThreadPenalty.log.PLogs.PLOGS_INFO;
import static com.landenlabs.allThreadPenalty.log.PLogs.PLOGS_WARN;

/**
 * Measure Thread Locality Penalty (accessing similar memory by concurrent threads)
 */
public class FragBottomNavOne extends FragBottomNavBase
        implements View.OnClickListener, JniHandler.ShareMsg, Handler.Callback {

    private ToggleButton startTestBtn;
    private ViewGroup msgHolder;
    private SeekBar testProgressBar;
    private TextView testProgressPercent;
    private String timeStr = "00:00";

    // Silly timer
    private long startMilli;
    private TextView tickView0;
    private TextView tickView1;

    // Thread locality penalty coverage
    private boolean isTestRunning = false;
    private TesterThread testerThread;
    private static final int GAP_LENGTH = 2;
    private static final int GAP_END = 64;
    private static final int GAP_STEP = 1;

    // Thread event messages
    //  Text Message
    static class MsgEvent extends Events.QueueEvent {
        String msg;
        MsgEvent(String msg) {
            this.eventType = Events.EventType.MSG;
            this.msg = msg;
        }
        @NonNull
        @Override
        public String toString() {
            return msg;
        }
    }
    //  Data message
    static class DataEvent extends Events.QueueEvent {
        int startGap;
        int endGap;
        int stepGap;
        long[] gapMilliseconds = new long[GAP_END + 1];

        DataEvent(int startGap) {
            this.eventType = Events.EventType.DATA;
            this.startGap = startGap;
            this.endGap = startGap + GAP_LENGTH - 1;
            this.stepGap = GAP_STEP;
        }
    }

    // Thread communication queue and UI handler
    private final ArrayBlockingQueue<Events.QueueEvent> dataQueue = new ArrayBlockingQueue<>(32);
    private static final int MORE_TEST_DATA = 123;
    private Handler uiHandler;

    @Override
    public void message(String msg) {
        dataQueue.add(new MsgEvent(msg));
    }


    // ---------------------------------------------------------------------------------------------


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, R.layout.frag_bottom_nav_one);
        setBarTitle("Thread Locality Penalty");

        PLog.setMinLevel(PLOG_INFO);

        startTestBtn = root.findViewById(R.id.startTestBtn);
        startTestBtn.setOnClickListener(this);
        msgHolder = root.findViewById(R.id.sample_holder);
        testProgressBar = root.findViewById(R.id.testProgressBar);
        testProgressPercent = root.findViewById(R.id.testProgressPercent);
        testProgressBar.setEnabled(false);

        // Silly background timer to demonstrate threads
        tickView0 = root.findViewById(R.id.tickView0);
        tickView1 = root.findViewById(R.id.tickView1);

        // Get write permission for file log.
        getWritePermission(getActivity());

        // Setup line graph.
        setupGraph(root);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (uiHandler == null) {
            uiHandler = new Handler(this);
        }
        JniHandler.shareMsg = this;
        fixUI(true);
    }

    @Override
    public void onPause() {
        JniHandler.shareMsg = null;
        stopTest();
        uiHandler.removeMessages(MORE_TEST_DATA);
        fixUI(false);
        super.onPause();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        fixUI(true);
    }

    private void fixUI(boolean onStart) {
        int orientation = this.getResources().getConfiguration().orientation;
        int vis = (!onStart ||  orientation == Configuration.ORIENTATION_PORTRAIT)
             ? View.VISIBLE : View.INVISIBLE;

        setBarVisibility(vis);

        View bottomNavView = requireActivity().findViewById(R.id.bottomNavigation);
        if (bottomNavView != null) {
            bottomNavView.setVisibility(vis);
        }
    }
    // ---------------------------------------------------------------------------------------------
    @Override
    public void onClick(View view) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (view.getId()) {
            case R.id.startTestBtn:
                testPlogsToFile();
                plogTester();
                if (isTestRunning) {
                    stopTest();
                } else {
                    startTest();
                }
                break;
        }
    }


    // ---------------------------------------------------------------------------------------------
    // Move control from background thread to UI thread to update UI components.

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (msg.what) {
            case MORE_TEST_DATA:
                moreQueuedEvents();
                return true;    // Message handled
        }
        return false;   // Message was not handled
    }

    private void moreQueuedEvents() {
        TextView tv;
        try {
            while (!dataQueue.isEmpty()) {
                Events.QueueEvent event = dataQueue.take();
                switch (event.eventType) {
                    case MSG:
                        tv = new TextView(msgHolder.getContext());
                        tv.setText(tv.getResources().getString(R.string.test_log_msg, timeStr, ((MsgEvent) event).msg));
                        msgHolder.addView(tv);
                        break;
                    case DATA:
                        DataEvent dataEvent = (DataEvent) event;
                        for (int idx = 0; idx <  dataEvent.endGap - dataEvent.startGap+1; idx++) {
                            lineGraph.append(dataEvent.startGap + idx, dataEvent.gapMilliseconds[idx] / 1000f);
                        }

                        testProgressBar.setProgress(dataEvent.endGap);
                        testProgressPercent.setText(String.format(Locale.US, "%d%%", dataEvent.endGap * 100 / GAP_END));

                        if (dataEvent.endGap >= GAP_END) {
                            stopTest();
                            testProgressPercent.setText(R.string.test_done);
                            tv = new TextView(msgHolder.getContext());
                            tv.setText(tv.getResources().getString(R.string.test_log_done, timeStr));
                            msgHolder.addView(tv);
                        }
                        break;
                }
            }
        } catch (Exception ex) {
            tv = new TextView(msgHolder.getContext());
            tv.setText(tv.getResources().getString(R.string.test_log_error, timeStr, ex.getMessage()));
            msgHolder.addView(tv);
        }
    }

    // ---------------------------------------------------------------------------------------------

    private void startTest() {
        // Silly background timer threads.
        startMilli = System.currentTimeMillis();
        startTickThread(0);
        startTickThread(1);

        testProgressBar.setEnabled(true);
        testProgressBar.setMax(GAP_END);
        testProgressBar.setProgress(0);
        testProgressPercent.setText(R.string.test_riunning);

        isTestRunning = true;
        testerThread = new TesterThread();
        getLifecycle().addObserver(testerThread);
        testerThread.start();
    }

    private void stopTest() {
        if (isTestRunning) {
            isTestRunning = false;
            testerThread.interrupt();
            testerThread = null;

            // Stop silly timer threads
            stopTickThread(0);
            stopTickThread(1);
        }
    }

    // Test using Plogs, unrelated to Thread Locality Penalty test.
    private void testPlogsToFile() {
        if (canWrite(requireActivity())) {
            // Current setup to log to a file.
            PLogs.setMinLevel(PLOGS_INFO);
            PLogs.printf(PLOGS_INFO, "PLogsTag1", "Set plog minLevel to %d", PLOG_INFO);
            PLogs.printf(PLOGS_DEBUG, "PLosgTag2", "Test fmt %d %s", PLOG_INFO, "hello world");
            PLogs.printf(PLOGS_WARN, "PLogsTag3", "Just msg");
        }
    }

    // Called by cppCode
    // A function calling from JNI to update current timer
    @SuppressWarnings("unused")
    @Keep
    private void updateTimer(final int idx) {
        requireActivity().runOnUiThread(() -> {
            long delta = System.currentTimeMillis() - startMilli;
            long min = TimeUnit.MILLISECONDS.toMinutes(delta);
            long sec = TimeUnit.MILLISECONDS.toSeconds(delta - TimeUnit.MINUTES.toMillis(min));
            timeStr = String.format(Locale.US, "%2d:%02d", min, sec);
            if (idx == 0) {
                tickView0.setText(timeStr);
            } else {
                tickView1.setText(timeStr);
            }
        });
    }

    // ---------------------------------------------------------------------------------------------
    // Permission - check and/or request

    private static final int REQUEST_WRITE_CODE = 12345;

    private boolean canWrite(Activity activity) {
        return ContextCompat.checkSelfPermission(
                activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void getWritePermission(Activity activity) {

        if (!canWrite(activity)) {
            final String[] wantPermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(activity, wantPermissions, REQUEST_WRITE_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_WRITE_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Got permission - all is good
                PLog.printf(PLOG_INFO, "AllThreadPenalty", "Got write permission");
            } else {
                // Failed to get permission - we are in trouble
                PLog.printf(PLOG_ERROR, "AllThreadPenalty", "Failed to get write permission");
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Graph

    private LineGraph lineGraph;

    private void setupGraph(View rootView) {

        lineGraph = new LineGraph();
        getLifecycle().addObserver(lineGraph);

        GraphView graph = rootView.findViewById(R.id.graph);
        lineGraph.initGraph(graph, GAP_END);

        String devTitle = Build.MANUFACTURER + " " + Build.MODEL + " (" + howCompiledMsg() + ") ";
        ((TextView)rootView.findViewById(R.id.graph_title)).setText(devTitle);
        rootView.findViewById(R.id.imgFullscreen).setOnClickListener(view -> openWebPage());
        rootView.findViewById(R.id.graph_share).setOnClickListener(view -> {
            ShareUtil.shareScreen(requireActivity(), rootView, "Share All-ThreadPenalty", null);
        });
    }

    private void openWebPage() {
        // Intent intent = new Intent(getActivity(), FullscreenActivity.class);
        // intent.putExtra(FullscreenExample.ARG_ID, helloWorld.name());
        // startActivity(intent);
    }

    // ---------------------------------------------------------------------------------------------
    // Tester run in a background thread, results added to a queue

    class TesterThread extends Thread implements LifecycleObserver {
        public void run() {
            DataEvent dataEvent = new DataEvent(1);
            try {
                // Loop forever waiting on queue.
                while (!isInterrupted() && dataEvent.startGap < GAP_END) {
                    startThreadPenalty(dataEvent.startGap, dataEvent.endGap, dataEvent.stepGap, dataEvent.gapMilliseconds);
                    dataQueue.add(dataEvent);
                    dataEvent = new DataEvent(dataEvent.endGap + 1);
                    uiHandler.sendEmptyMessage(MORE_TEST_DATA);
                }
            } catch (Exception ex) {
                dataQueue.add(new MsgEvent("Test Exception " + ex.getMessage()));
                uiHandler.sendEmptyMessage(MORE_TEST_DATA);
            }
            dataQueue.add(new MsgEvent("Test Done"));
            uiHandler.sendEmptyMessage(MORE_TEST_DATA);
        }

        @SuppressWarnings("unused")
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        public void onDestroy() {
            interrupt();
        }

        @SuppressWarnings("unused")
        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        public void onStop() {
            interrupt();
        }
    }

    // ---------------------------------------------------------------------------------------------
    // JNI C++

    // JNI bridge
    static {
        // See CMakeList.txt for library to load.
        System.loadLibrary("cppcode");
    }

    public native void startThreadPenalty(int startGap, int endGap, int stepGap, long[] gapTimedMilliseconds);

    // Extra support methods callable by C++
    public native String howCompiledMsg();

    // Silly threads which run in C++ and call up to Java UI.
    public native void startTickThread(int threadIdx);  // threadIdx 0 or 1
    public native void stopTickThread(int threadIdx);   // threadIdx 0 or 1

    // Test Private logging class
    public native void plogTester();
}

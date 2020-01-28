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
package com.landenlabs.allThreadPenalty.penalty;

import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Iterator;

/**
 *  Create a line graph to scroll when data is updated.
 *  https://github.com/jjoe64/GraphView
 */
public class LineGraph implements LifecycleObserver {
    private final Handler mHandler = new Handler();
    private LineGraphSeries<DataPoint> mSeries;
    private GraphView graphView;

    public void initGraph(GraphView graph, int maxX) {
        this.graphView = graph;

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(maxX);
        // graph.getViewport().setYAxisBoundsManual(false);
        // graph.getViewport().setXAxisBoundsStatus(Viewport.AxisBoundsStatus.AUTO_ADJUSTED);
        graph.getViewport().setYAxisBoundsStatus(Viewport.AxisBoundsStatus.AUTO_ADJUSTED);
        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);

        graph.getGridLabelRenderer().setLabelVerticalWidth(100);
        graph.getGridLabelRenderer().setVerticalAxisTitle("Seconds");

        mSeries = new LineGraphSeries<>();
        mSeries.setDrawDataPoints(true);
        mSeries.setDrawBackground(true);
        graphView.addSeries(mSeries);
    }

    public void clear() {
        DataPoint[] data = new DataPoint[0];
        mSeries.resetData(data);
    }

    public void append(int xValue, float yValue) {
        mSeries.appendData(new DataPoint(xValue, yValue), true, 100);
        graphView.getViewport().setMinX(0);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void onResume() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    void onPause() {
    }

    private static final String ARRAY_X_LEN = "arrayXlen";
    private static final String ARRAY_X_MIN = "arrayXmin";
    private static final String ARRAY_Y_FLT = "arrayYflt";

    public void onSave(@NonNull Bundle bundle) {
        int minX = (int)mSeries.getLowestValueX();
        int maxX = (int)mSeries.getHighestValueX();
        int lenX = maxX - minX + 1;
        bundle.putInt(ARRAY_X_LEN, lenX);
        bundle.putInt(ARRAY_X_MIN, minX);
        float[] dataYarray = new float[lenX];
        int y = 0;
        Iterator<DataPoint> iter = mSeries.getValues(minX, maxX);
        while (iter.hasNext()) {
            dataYarray[y++] = (float)iter.next().getY();
        }
        bundle.putFloatArray(ARRAY_Y_FLT, dataYarray);
    }

    public void onRestore(@NonNull Bundle bundle) {
        int lenX = bundle.getInt(ARRAY_X_LEN);
        int minX = bundle.getInt(ARRAY_X_MIN);
        float[] dataYarray = bundle.getFloatArray(ARRAY_Y_FLT);
        DataPoint[] data = new DataPoint[lenX];
        for (int idx = 0; idx < lenX ; idx++) {
            data[idx] = new DataPoint(minX++, dataYarray[idx]);
        }
        mSeries.resetData(data);
    }

    private static Bundle saveRestore = new Bundle();
    public void save() {
        saveRestore = new Bundle();
        onSave(saveRestore);
    }
    public void restore() {
        onRestore(saveRestore);
    }
}

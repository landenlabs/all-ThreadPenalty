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
package com.landenlabs.allThreadPenalty;

import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.AppBarLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SuppressWarnings("WeakerAccess")
public abstract class FragBottomNavBase extends Fragment {

    @IntDef({View.VISIBLE, View.INVISIBLE, View.GONE})
    @Retention(RetentionPolicy.SOURCE)
    @interface Visibility {}

    protected ViewGroup root;
    protected androidx.appcompat.widget.Toolbar toolbar;
    protected AppBarLayout appBar;


    @SuppressWarnings("UnusedReturnValue")
    View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, int layoutRes) {
        root = (ViewGroup)inflater.inflate(layoutRes, container, false);
        appBar = container.getRootView().findViewById(R.id.appbar);
        toolbar = container.getRootView().findViewById( R.id.toolbar);
        return root;
    }

    void setBarTitle(String title) {
        if (toolbar != null) {
            toolbar.setTitle(title);
            setBarVisibility(View.VISIBLE);
        }
    }

    void setBarVisibility(@Visibility int visibility) {
        if (appBar != null && appBar.getVisibility() != visibility) {
            AutoTransition autoTransition = new AutoTransition();
            autoTransition.setDuration(500);
            TransitionManager.beginDelayedTransition((ViewGroup)appBar.getRootView(), autoTransition);
            appBar.setVisibility(visibility);
            appBar.invalidate();
        }
    }
}

package com.applilandia.viewgroupmotion;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {

    private final static String LOG_TAG = MainActivity.class.getSimpleName();

    private boolean mIntercept = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initSwitch();
        initLayout();
    }

    private void initSwitch() {
        Switch switchInterceptor = (Switch) findViewById(R.id.switchInterceptor);
        switchInterceptor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.v(LOG_TAG, "onCheckedChange");
                mIntercept = isChecked;
            }
        });
    }

    private void initLayout() {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.layoutMain);
        linearLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.v(LOG_TAG, "onTouch");
                return false;
            }
        });
        TextViewTest textViewTest = new TextViewTest(this, null);
        Layout layout = new Layout(this, null);
        layout.addView(textViewTest);
        linearLayout.addView(layout);
    }

    private class Layout extends LinearLayout {

        private final String LOG_TAG = Layout.class.getSimpleName();

        public Layout(Context context, AttributeSet attrs) {
            super(context, attrs);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            setLayoutParams(layoutParams);
            setOrientation(VERTICAL);
            setGravity(Gravity.CENTER);
            setBackgroundColor(getResources().getColor(R.color.accent_material_dark));
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            Log.v(LOG_TAG, "onInterceptTouchEvent");
            return mIntercept;
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            Log.v(LOG_TAG, "onTouchEvent");
            return false;
        }
    }

    private class TextViewTest extends TextView {

        private final String LOG_TAG = TextViewTest.class.getSimpleName();

        private TextViewTest(Context context, AttributeSet attrs) {
            super(context, attrs);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            setLayoutParams(layoutParams);
            setBackgroundColor(getResources().getColor(R.color.background_floating_material_light));
            setText(getResources().getString(R.string.text_view_message));
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            Log.v(LOG_TAG, "onTouchEvent");
            return true;
        }
    }

}

package com.applilandia.snackbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by JuanCarlos on 09/03/2015.
 */
public class SnackBar extends RelativeLayout {

    private final static String LOG_TAG = SnackBar.class.getSimpleName();

    public interface OnSnackBarListener {
        public void onClose();
        public void onUndo();
    }

    private OnSnackBarListener mOnSnackBarListener;
    private TextView mText;
    private TextView mActionText;

    public SnackBar(Context context) {
        this(context, null);
    }

    public SnackBar(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.snackBarStyle);

        LayoutParams layoutParams = new LayoutParams(context, attrs);
        //Layout_Gravity
        //////layoutParams.gravity = Gravity.BOTTOM|Gravity.START;
        setLayoutParams(layoutParams);
        //Gravity
        //setGravity(Gravity.CENTER);
        /////setOrientation(HORIZONTAL);

        setVisibility(View.GONE);
        //Add text and action views
        createSnackBarText(context);
        createSnackBarAction(context);
    }


    private void createSnackBarText(Context context) {
        mText = new TextView(context, null, R.attr.snackBarText);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        mText.setLayoutParams(layoutParams);
        mText.setGravity(Gravity.CENTER);
        addView(mText);
    }

    private void createSnackBarAction(Context context) {
        mActionText = new TextView(context, null, R.attr.snackBarAction);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mActionText.setLayoutParams(layoutParams);
        mActionText.setPadding(getPixels(40), 0, 0, 0);
        mActionText.setGravity(Gravity.CENTER);
        mActionText.setText(R.string.snack_bar_action_text);
        mActionText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnSnackBarListener != null) {
                    mOnSnackBarListener.onUndo();
                }
            }
        });
        addView(mActionText);
    }

    public void show(int messageResId) {
        mText.setText(messageResId);
        setVisibility(View.VISIBLE);
        setAlpha(1);
        animate().setDuration(5000)
                .setInterpolator(new AccelerateInterpolator())
                .alpha(0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        hide();
                        if (mOnSnackBarListener != null) {
                            mOnSnackBarListener.onClose();
                        }
                    }
                }).start();
    }

    public void hide() {
        setVisibility(View.GONE);
    }

    public void setOnSnackBarListener(OnSnackBarListener l) {
        mOnSnackBarListener = l;
    }

    private int getPixels(int dpValue) {
        DisplayMetrics metrics;
        metrics = getResources().getDisplayMetrics();
        return (int) (metrics.density * dpValue);
    }
}

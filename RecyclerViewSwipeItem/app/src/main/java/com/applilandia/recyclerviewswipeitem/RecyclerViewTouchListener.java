package com.applilandia.recyclerviewswipeitem;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ListView;

/**
 * Created by JuanCarlos on 12/03/2015.
 */
public class RecyclerViewTouchListener implements View.OnTouchListener {

    public interface OnRecyclerViewTouchListener {
        public void onDismiss(int position);
    }

    private RecyclerView mRecyclerView = null;
    private VelocityTracker mVelocityTracker = null;
    private OnRecyclerViewTouchListener mOnRecyclerViewTouchListener;
    private View mDownView = null;
    private float mDownX, mDownY;
    private int mDownPosition;
    private float mSlop, mSwipingSlop;
    private boolean mSwiping = false;
    private int mViewWidth = 1;

    public RecyclerViewTouchListener(RecyclerView recyclerView, OnRecyclerViewTouchListener l) {
        mRecyclerView = recyclerView;
        mOnRecyclerViewTouchListener = l;
        ViewConfiguration viewConfiguration = ViewConfiguration.get(recyclerView.getContext());
        mSlop = viewConfiguration.getScaledTouchSlop();
    }

    private View getViewPressed(MotionEvent event) {
        int[] listCoordinates = new int[2];

        mRecyclerView.getLocationOnScreen(listCoordinates);

        int x = (int) event.getRawX() - listCoordinates[0];
        int y = (int) event.getRawY() - listCoordinates[1];

        Rect rect = new Rect();
        int childCount = mRecyclerView.getChildCount();
        View viewChild = null;
        for (int index = 0; index < childCount; index++) {
            viewChild = mRecyclerView.getChildAt(index);
            viewChild.getHitRect(rect);
            if (rect.contains(x, y)) {
                return viewChild;
            }
        }
        return viewChild;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (mViewWidth < 2) {
            mViewWidth = mRecyclerView.getWidth();
        }

        int actionId = event.getActionMasked();

        switch (actionId) {
            case MotionEvent.ACTION_DOWN:
                mDownView = getViewPressed(event);
                if (mDownView != null) {
                    mDownX = event.getRawX();
                    mDownY = event.getRawY();
                    mDownPosition = mRecyclerView.getChildPosition(mDownView);
                    mVelocityTracker = VelocityTracker.obtain();
                    mVelocityTracker.addMovement(event);
                }
                break;

            case MotionEvent.ACTION_UP:
                if (mVelocityTracker == null) {
                    break;
                }
                float deltaX = event.getRawX() - mDownX;
                boolean dismiss = false;
                if (Math.abs(deltaX) > mViewWidth/2 && mSwiping) {
                    dismiss = true;
                }
                if (dismiss && mDownPosition != ListView.INVALID_POSITION) {
                    final int position = mDownPosition;
                    mDownView.animate()
                            .translationX(mViewWidth)
                            .alpha(0)
                            .setDuration(mRecyclerView.getContext().getResources()
                                            .getInteger(android.R.integer.config_shortAnimTime))
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    if (mOnRecyclerViewTouchListener != null) {
                                        mOnRecyclerViewTouchListener.onDismiss(position);
                                    }
                                }
                            })
                            .start();
                } else {
                    mDownView.animate()
                            .translationX(0)
                            .setDuration(mRecyclerView.getContext().getResources()
                                    .getInteger(android.R.integer.config_shortAnimTime))
                            .start();
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mDownX = 0;
                mDownY = 0;
                mDownView = null;
                mDownPosition = ListView.INVALID_POSITION;
                mSwiping = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mVelocityTracker == null) {
                    break;
                }
                mVelocityTracker.addMovement(event);
                deltaX = event.getRawX() - mDownX;
                if (deltaX > mSlop) {
                    mSwiping = true;
                    mSwipingSlop = (deltaX > 0) ? mSlop : -mSlop;
                    mRecyclerView.requestDisallowInterceptTouchEvent(true);

                    // Cancel ListView's touch (un-highlighting the item)
                    MotionEvent cancelEvent = MotionEvent.obtain(event);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                            (event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                    mRecyclerView.onTouchEvent(cancelEvent);
                    cancelEvent.recycle();
                }

                if (mSwiping) {
                    mDownView.setTranslationX(deltaX - mSwipingSlop);
                }

                break;

            case MotionEvent.ACTION_CANCEL:
                if (mVelocityTracker == null) {
                    break;
                }

                if (mDownView != null && mSwiping) {
                    // cancel
                    mDownView.animate()
                            .translationX(0)
                            .alpha(1)
                            .setDuration(mRecyclerView.getResources().getInteger(android.R.integer.config_shortAnimTime))
                            .setListener(null);
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mDownX = 0;
                mDownY = 0;
                mDownView = null;
                mDownPosition = ListView.INVALID_POSITION;
                mSwiping = false;
                break;
        }

        return false;
    }
}

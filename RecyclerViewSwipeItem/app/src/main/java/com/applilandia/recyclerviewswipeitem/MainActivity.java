package com.applilandia.recyclerviewswipeitem;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    private final static String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int ITEMS_COUNT = 20;
    private List<String> mItems;

    private RecyclerView mRecyclerView;
    private RecyclerAdapter mAdapter;
    private OnItemClickListener mOnItemClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initRecyclerView();
    }

    private void initData() {
        mItems = new ArrayList<>();
        for (int i = 0; i < ITEMS_COUNT; i++) {
            mItems.add("Item " + (i + 1));
        }
    }

    private void initRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new RecyclerAdapter();
        mRecyclerView.setAdapter(mAdapter);

        mOnItemClickListener = new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.v(LOG_TAG, "Click");
                Toast.makeText(view.getContext(), "click", Toast.LENGTH_SHORT).show();
            }
        };
        mRecyclerView.addOnItemTouchListener(new RegisterItemClickListener(this, mOnItemClickListener));
        mRecyclerView.setOnTouchListener(new RecyclerViewTouchListener(mRecyclerView,
                new RecyclerViewTouchListener.OnRecyclerViewTouchListener() {
                    @Override
                    public void onDismiss(int position) {
                        mItems.remove(position);
                        mAdapter.notifyDataSetChanged();
                    }
                }));
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(android.R.layout.simple_list_item_1,
                    viewGroup, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            viewHolder.mTextView.setText(mItems.get(i));
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(android.R.id.text1);
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    private class RegisterItemClickListener implements RecyclerView.OnItemTouchListener {

        private OnItemClickListener mListener;
        private GestureDetector mGestureDetector;

        public RegisterItemClickListener(Context context, OnItemClickListener listener) {
            mListener = listener;
            mGestureDetector = new GestureDetector(context,
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onSingleTapUp(MotionEvent e) {
                            Log.v(LOG_TAG, "onSingleTapUp");
                            return true;
                        }

                        @Override
                        public void onLongPress(MotionEvent e) {
                            Log.v(LOG_TAG, "onLongPress");
                        }
                    });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
            Log.v(LOG_TAG, "onInterceptTouchEvent");
            View view = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
            if ((view != null) && (mListener != null) && (mGestureDetector.onTouchEvent(motionEvent))) {
                mListener.onItemClick(view, recyclerView.getChildPosition(view));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {

        }
    }


}

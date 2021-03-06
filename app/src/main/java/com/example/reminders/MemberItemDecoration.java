package com.example.reminders;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class MemberItemDecoration extends RecyclerView.ItemDecoration {
    private int mBottomOffset;

    public MemberItemDecoration(int bottomOffset) {
        mBottomOffset = bottomOffset;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int dataSize = state.getItemCount();
        int position = parent.getChildAdapterPosition(view);
        if (dataSize > 0 && position == dataSize - 1) {
            outRect.set(0, 0, 0, mBottomOffset);
        } else {
            outRect.set(0, 0, 0, 0);
        }
    }
}


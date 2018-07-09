package com.player.testplayer.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.player.testplayer.R;

public class WindowView extends FrameLayout implements View.OnClickListener {
    private View rootView;
    private FrameLayout fl_parent;
    private ImageButton imageView;

    public WindowView(@NonNull Context context) {
        super(context);
        init();
    }

    public WindowView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WindowView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.layout_window, this);
        imageView = rootView.findViewById(R.id.iv_img);
        fl_parent = rootView.findViewById(R.id.fl_parent);
//        imageView.setOnClickListener(this);
    }

    public ImageButton getImageView() {
        return imageView;
    }

    public FrameLayout getFl_parent() {
        return fl_parent;
    }

    @Override
    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.iv_img:
//                Toast.makeText(getContext(), "点击", Toast.LENGTH_SHORT).show();
//                break;
//        }
    }
}

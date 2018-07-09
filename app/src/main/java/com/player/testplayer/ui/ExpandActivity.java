package com.player.testplayer.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.player.testplayer.R;

import butterknife.ButterKnife;

public class ExpandActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expand);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {

    }

}

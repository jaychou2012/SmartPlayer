package com.player.testplayer.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.player.testplayer.R;
import com.player.testplayer.utils.Utils;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PageActivity extends AppCompatActivity {
    @BindView(R.id.iv_img)
    ImageView iv_img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        Glide.with(this).load
                (new File(Environment.getExternalStorageDirectory() +
                        "/tencent/QQfile_recv/20180705091854.png")).into(iv_img);
        Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() +
                "/tencent/QQfile_recv/20180705091854.png").copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(15);
        paint.setColor(Color.BLACK);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float height = fontMetrics.bottom - fontMetrics.top;
        canvas.drawText("文字文字文字", 0, height, paint);
        Utils.saveBitmap(bitmap);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Glide.with(PageActivity.this).
                        load(new File(Environment.getExternalStorageDirectory() + "/image.jpg")).into(iv_img);
            }
        }, 2000);
        Utils.setFileDocument(
                Environment.getExternalStorageDirectory() + "/image.jpg",
                Environment.getExternalStorageDirectory() + "/image.pdf");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(PageActivity.this, ViewActivity.class);
                startActivity(intent);
            }
        }, 3000);
    }
}

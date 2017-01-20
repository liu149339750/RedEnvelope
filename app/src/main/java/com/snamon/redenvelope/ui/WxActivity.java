package com.snamon.redenvelope.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.snamon.redenvelope.R;

public class WxActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wx);

        findViewById(R.id.btn_mock_wx)
                .setOnClickListener(v -> Toast.makeText(WxActivity.this ,"模拟点击了",Toast.LENGTH_LONG).show());
    }
}

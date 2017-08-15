package com.guangyao.bluetoothtest.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.guangyao.bluetoothtest.R;
import com.guangyao.bluetoothtest.command.CommandManager;

/**
 * Created by liuqiong on 2017/7/13.
 */

public class PrayActivity extends AppCompatActivity {

    private Integer integer;
    private Integer integer1;
    private Integer integer2;
    private Integer integer3;
    private Integer integer4;
    private Integer integer5;
    private Integer integer6;
    private CommandManager manager;
    private EditText id;
    private EditText switchh;
    private EditText startH;
    private EditText startM;

    private EditText number;
    private Button button;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pray);
        getSupportActionBar().setTitle(R.string.pray);
        id = (EditText) findViewById(R.id.id);
        switchh = (EditText) findViewById(R.id.switchh);
        startH = (EditText) findViewById(R.id.startH);
        startM = (EditText) findViewById(R.id.startM);
        number = (EditText) findViewById(R.id.number);
        button = (Button) findViewById(R.id.send);

        manager = CommandManager.getInstance(this);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!"".equals(id.getText().toString())
                        &&!"".equals(switchh.getText().toString())
                        &&!"".equals(startH.getText().toString())
                        &&!"".equals(startM.getText().toString())
                        &&!"".equals(number.getText().toString())){

                    integer = Integer.valueOf(id.getText().toString());
                    integer1 = Integer.valueOf(switchh.getText().toString());
                    integer2 = Integer.valueOf(startH.getText().toString());
                    integer3 = Integer.valueOf(startM.getText().toString());
                    integer6 = Integer.valueOf(number.getText().toString());
                    manager.setPray(integer,integer1,integer2,integer3,integer6);
                }




            }
        });


    }
}

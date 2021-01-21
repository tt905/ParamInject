package com.mo.annotation.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mo.annotation.annos.ParamInject;
import com.mo.annotation.library.ExtraInjectHelper;

public class ParamActivity extends AppCompatActivity {

    public static void open(Context context) {
        Intent intent = new Intent(context, ParamActivity.class);
        intent.putExtra("myParam", "哈哈哈哈");
        intent.putExtra("myParam2", 333);
        intent.putExtra("userModel", new UserModel("名称"));
        context.startActivity(intent);
    }

    @ParamInject("myParam")
    public String param1 = "";

    @ParamInject("myParam2")
    public int param2 = 0;

    @ParamInject("userModel")
    public UserModel userModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ExtraInjectHelper.inject(this);

//        param1 = getIntent().getStringExtra("myParam");
//        param1 = getIntent().getParcelableExtra()
//        userModel = getIntent().getParcelableExtra("userModel");
//        Log.d("MLog", "param1:" + param1 + " param2:" + param2 + " userModel.name:"+ this.userModel.name);
    }
}

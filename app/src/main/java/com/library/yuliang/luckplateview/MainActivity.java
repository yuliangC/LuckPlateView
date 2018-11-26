package com.library.yuliang.luckplateview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.library.yuliang.luckplateview.widget.LuckPlateView;

public class MainActivity extends AppCompatActivity {

    private LuckPlateView plateViewNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        plateViewNew=findViewById(R.id.plateViewNew);
        ImageView startIv=findViewById(R.id.startIv);
        startIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plateViewNew.startRotate(1);
            }
        });



    }





}

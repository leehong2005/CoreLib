package com.lee.sdk.test.anim;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.lee.sdk.test.GABaseActivity;
import com.lee.sdk.test.effect.BitmapMesh2;
import com.lee.sdk.utils.Utils;

public class CycleAnimationActivity extends GABaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout linearLayout = new LinearLayout(this);
        
        String dataDir = this.getApplicationInfo().dataDir;
        Log.d("leehong", "dataDir = " + dataDir);

        int width = (int) Utils.pixelToDp(this, 150);
        final BitmapMesh2.SampleView sampleView = new BitmapMesh2.SampleView(this);
        sampleView.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        Button btn = new Button(this);
        btn.setText("Run");
        btn.setTextSize(20.0f);
        btn.setLayoutParams(new LinearLayout.LayoutParams(width, -2));

        btn.setOnClickListener(new View.OnClickListener() {
            boolean mReverse = false;

            @Override
            public void onClick(View v) {
                if (sampleView.startAnimation(mReverse)) {
                    mReverse = !mReverse;
                }
            }
        });

        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER_VERTICAL);
        linearLayout.addView(btn);

        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(LinearLayout.VERTICAL);
        linearLayout2.addView(linearLayout);
        linearLayout2.addView(sampleView);

        setContentView(linearLayout2);
    }
}

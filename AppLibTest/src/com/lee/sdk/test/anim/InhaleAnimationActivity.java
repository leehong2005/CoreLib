package com.lee.sdk.test.anim;

import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.lee.sdk.mesh.InhaleMesh.InhaleDir;
import com.lee.sdk.test.GABaseActivity;
import com.lee.sdk.test.R;
import com.lee.sdk.test.effect.BitmapMesh;
import com.lee.sdk.utils.Utils;

public class InhaleAnimationActivity extends GABaseActivity {
    private static final boolean DEBUG_MODE = true;
    private BitmapMesh.SampleView mSampleView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout linearLayout = new LinearLayout(this);

        mSampleView = new BitmapMesh.SampleView(this);
        mSampleView.setIsDebug(DEBUG_MODE);
        mSampleView.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        Button btn = new Button(this);
        btn.setText("Run");
        btn.setTextSize(20.0f);
        int width = (int) Utils.pixelToDp(this, 150);
        btn.setLayoutParams(new LinearLayout.LayoutParams(width, -2));
        btn.setOnClickListener(new View.OnClickListener() {
            boolean mReverse = false;

            @Override
            public void onClick(View v) {
                if (mSampleView.startAnimation(mReverse)) {
                    mReverse = !mReverse;
                }
            }
        });

        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER_VERTICAL);
        linearLayout.addView(btn);
        linearLayout.addView(mSampleView);

        setContentView(linearLayout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.inhale_anim_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_inhale_down:
            mSampleView.setInhaleDir(InhaleDir.DOWN);
            break;

        case R.id.menu_inhale_up:
            mSampleView.setInhaleDir(InhaleDir.UP);
            break;

        case R.id.menu_inhale_left:
            mSampleView.setInhaleDir(InhaleDir.LEFT);
            break;

        case R.id.menu_inhale_right:
            mSampleView.setInhaleDir(InhaleDir.RIGHT);
            break;
        }

        return super.onOptionsItemSelected(item);
    }
}

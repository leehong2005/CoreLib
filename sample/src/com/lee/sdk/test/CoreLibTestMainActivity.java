package com.lee.sdk.test;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;

public class CoreLibTestMainActivity extends BaseListActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.i("DemoShell", "os_name:" + System.getProperty("os.name"));
        Log.i("DemoShell", "os_arch:" + System.getProperty("os.arch"));
        Log.i("DemoShell", "os_version:" + System.getProperty("os.version"));
        
        //testStr();
        testTypedValue();
        testIntent();
        
        setUseDefaultPendingTransition(true);
    }
    
    void testStr() {
        byte[] arrays = { -119, -28, -112, -91, -113, -84, -119, 74, -107, -126, -119, 93, -127, 67, -115, -95, -109,
                86, -118, 119, 63, -105, -71, 81, 82, 67, 111, 100, 101, 0 };
        
        String encoding = "GBK";
        String str;
        try {
            str = new String(arrays, encoding);
            Log.d("CoreLibTestMainActivity", "  str = " + str);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    
    void testTypedValue() {
        try {
            Resources res = this.getResources();
            int id = R.drawable.picture_guide_next2;
            TypedValue outValue = new TypedValue();
            res.getValue(id, outValue, true);
            float density =res.getDisplayMetrics().density;
            
            Drawable dd = res.getDrawable(id);
            
            int i = 0;
            ++i;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    void testIntent() {
        
        int color = Color.argb(0xff, 0xd7, 0x33, 0x33);
        
        Intent intent = new Intent();
        intent.setClassName(this.getPackageName(), "com.baidu.searchbox.LightBrowserActivity");
        intent.putExtra("bdsb_light_start_url", "www.baidu.com");
        String str = intent.toString();
        String str2 = intent.toUri(0);
        
        String str3 = "#Intent;component=com.lee.sdk.test/com.baidu.searchbox.LightBrowserActivity;S.bdsb_light_start_url=www.baidu.com;end";
        
        try {
            intent = Intent.parseUri(str3, 0);
            int i = 0;
            ++i;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        
        int i = 0;
        ++i;
    }
    
    @Override
    protected void onDestroy() {
//        GoogleAnalyticsBL.getInstance().dispatch();

        super.onDestroy();
    }

    @Override
    public Intent getQueryIntent() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(CORELIT_TEST_CAGEGORY);

        return intent;
    }
    
    @Override
    public void finish() {
        super.finish();
    }
}

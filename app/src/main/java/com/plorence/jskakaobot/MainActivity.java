package com.plorence.jskakaobot;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.mozilla.javascript.Script;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static String PREFS_KEY = "bot";
    private static String ON_KEY = "on";
    private boolean granted = true;
    static TextView tv1,tv2,tv3;
    Script ErrorCheck;
    int PrerequisiteCondition = 0;
    // UI
    /*
    TedPermission,jsoup 라이브러리를 사용하였습니다.
    만들어주셔서 감사합니다~! 덕분에 생존할수있었어요.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                ErrorCheck = KakaotalkListener.initializeScript(); //에러 체크를 하기 위해 initializeScript메서드 반환형을 Script로 함
                onReloadClick(getCurrentFocus());
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {

            }
        };
        new TedPermission(this)
                .setPermissionListener(permissionlistener)
                .setRationaleMessage("JS파일 생성을 위해 반드시 허용을 하셔야 합니다.") //창띄우기전에 설명해줌
                .setDeniedMessage("거부하시면 안돌아갑니다.")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
        tv1 = (TextView)findViewById(R.id.textView2);
        tv2 = (TextView)findViewById(R.id.textView3);
        tv3 = (TextView)findViewById(R.id.textView);
        String version = Build.VERSION.RELEASE;
        if(5.0 <= Float.parseFloat(version)) {
            tv1.setText("OS 버전이 5.0 이상이라 사용 가능합니다.\n");
        }
        else{
            tv1.setText("OS 버전이 5.0 이하라서 사용 불가능합니다.\n");
            tv1.setTextColor(Color.parseColor("#1DDB16"));
            PrerequisiteCondition++;
        }
        if(null == getPackageManager().getLaunchIntentForPackage("com.google.android.wearable.app")){
            tv1.setText(tv1.getText().toString()+"Android Wear를 설치해야 합니다!\n");
            tv1.setTextColor(Color.parseColor("#1DDB16"));
            PrerequisiteCondition++;
        }
        else {
            tv1.setText(tv1.getText().toString()+"Android Wear가 설치되어있습니다.\n");
        }
        if(ErrorCheck == null){
            tv2.setText("JS 문법적인 문제가 있습니다.");
            tv2.setTextColor(Color.parseColor("#FF0000"));
        }
        else{
            tv2.setText("JS 문법적인 문제가 없습니다.");
            tv2.setTextColor(Color.parseColor("#1DDB16"));
        }
        if(PrerequisiteCondition == 0){
            tv3.setVisibility(View.INVISIBLE);
        }
        else{
            tv3.setVisibility(View.VISIBLE);
        }
        Switch onOffSwitch = (Switch) findViewById(R.id.switch1);
        onOffSwitch.setChecked(getOn(this));
        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean b) {
                putOn(getApplicationContext(), b);
            }
        });
    }
    public void onSettingsClick(View v) {
        startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
    }

    public void onReloadClick(View v) {
        // 현재시간을 msec 으로 구한다.
        long now = System.currentTimeMillis();
        // 현재시간을 date 변수에 저장한다.
        Date date = new Date(now);
        // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
        SimpleDateFormat sdfNow = new SimpleDateFormat("HH:mm:ss");
        // nowDate 변수에 값을 저장한다.
        String formatDate = sdfNow.format(date);
        ErrorCheck = KakaotalkListener.initializeScript();
        if(ErrorCheck == null){
            tv2.setText("JS 문법적인 문제가 있습니다.\n최근 리로드 날짜:"+formatDate);
            tv2.setTextColor(Color.parseColor("#FF0000"));
        }
        else{
            tv2.setText("JS 문법적인 문제가 없습니다.\n최근 리로드 날짜:" + formatDate);
            tv2.setTextColor(Color.parseColor("#1DDB16"));
        }
    }

    public void edittor(View v){
        Intent intent = new Intent(MainActivity.this, EdittorActivity.class);
        startActivity(intent);
    }

    // Util


    static boolean getOn(Context ctx) {
        return ctx.getSharedPreferences(PREFS_KEY, MODE_PRIVATE).getBoolean(ON_KEY, false);
    }

    private static void putOn(Context ctx, boolean value) {
        ctx.getSharedPreferences(PREFS_KEY, MODE_PRIVATE).edit().putBoolean(ON_KEY, value).apply();
    }
}

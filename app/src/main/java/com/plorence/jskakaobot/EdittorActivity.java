package com.plorence.jskakaobot;

import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.EditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EdittorActivity extends AppCompatActivity {
    TextFileManager mTextFileManager = new TextFileManager(this);
     EditText et1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edittor);
        et1 = (EditText)findViewById(R.id.memo_edit);
        String memoData = mTextFileManager.load();
        highLight(memoData);
        //et1.setText(memoData);
    }
    public void Save(View v){
        String memoData = et1.getText().toString();
        mTextFileManager.save(memoData);
        //et1.setText("");
        highLight(memoData);
    }

    public String highLight(String str){
        Pattern p;
        Matcher m;
        final SpannableStringBuilder sp = new SpannableStringBuilder(str);

        p = Pattern.compile("\\\".*?\\\"");
        m = p.matcher(str);
        while (m.find()){
            System.out.println("start:"+m.start()+"end:"+m.end());
                sp.setSpan(new ForegroundColorSpan(Color.GREEN),m.start(),m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        p = Pattern.compile("[^https:]\\/\\/.*");
        m = p.matcher(str);
        while (m.find()){
            System.out.println("start:"+m.start()+"end:"+m.end());
            sp.setSpan(new ForegroundColorSpan(Color.GRAY),m.start(),m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        p = Pattern.compile("\\/\\*.*?\\*\\/",Pattern.DOTALL);
        m = p.matcher(str);
        while (m.find()){
            System.out.println("start:"+m.start()+"end:"+m.end());
            sp.setSpan(new ForegroundColorSpan(Color.GRAY),m.start(),m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        p = Pattern.compile("var");
        m = p.matcher(str);
        while (m.find()){
            System.out.println("start:"+m.start()+"end:"+m.end());
            sp.setSpan(new ForegroundColorSpan(Color.RED),m.start(),m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        p = Pattern.compile("equals|reply",Pattern.DOTALL);
        m = p.matcher(str);
        while (m.find()){
            System.out.println("start:"+m.start()+"end:"+m.end());
            sp.setSpan(new ForegroundColorSpan(Color.CYAN),m.start(),m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        p = Pattern.compile("room|sender|msg|isGroupChat,replier",Pattern.DOTALL);
        m = p.matcher(str);
        while (m.find()){
            System.out.println("start:"+m.start()+"end:"+m.end());
            sp.setSpan(new ForegroundColorSpan(Color.MAGENTA),m.start(),m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }


        et1.setText(sp);

        return str;
    }
}

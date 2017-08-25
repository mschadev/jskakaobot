package com.plorence.jskakaobot;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by john on 2017-08-24.
 */

public class TextFileManager {
    private static final String FILE_ROOT = Environment.getExternalStorageDirectory()+"/jskbot/"+"response.js";
    // 메모 내용을 저장할 파일 이름
    Context mContext = null;

    public TextFileManager(Context context) {
        mContext = context;
    }
    // 파일에 메모를 저장하는 함수
    public void save(String strData) {
        if( strData == null || strData.equals("") ) {
            return;
        }
        FileOutputStream fosMemo = null;
        try {
// 파일에 데이터를 쓰기 위해서 output 스트림 생성
            fosMemo = new FileOutputStream(new File(FILE_ROOT));
// 파일에 메모 적기
            fosMemo.write( strData.getBytes() );
            fosMemo.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // 저장된 메모를 불러오는 함수
    public String load() {
        try {
// 파일에서 데이터를 읽기 위해서 input 스트림 생성
            System.out.println("ROOT:"+Environment.getExternalStorageDirectory()+"/jskbot/"+"response.js");
            FileInputStream fisMemo = new FileInputStream (new File(FILE_ROOT));
// 데이터를 읽어 온 뒤, String 타입 객체로 반환
            byte[] memoData = new byte[fisMemo.available()];
            while (fisMemo.read(memoData) != -1) { }
            return new String(memoData);
        } catch (IOException e) {
            System.out.println("Error"+e);
        }
        return "";
    }
    // 저장된 메모를 삭제하는 함수
    public void delete() {
        mContext.deleteFile(FILE_ROOT);
    }
}

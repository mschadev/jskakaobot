package com.plorence.jskakaobot;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.mozilla.javascript.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;


public class KakaotalkListener extends NotificationListenerService {
    private static Function responder;
    private static ScriptableObject execScope;
    private static android.content.Context execContext;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        if (!MainActivity.getOn(getApplicationContext())) return;

        if (sbn.getPackageName().equals("com.kakao.talk")) {
            Notification.WearableExtender wExt = new Notification.WearableExtender(sbn.getNotification());
            for (Notification.Action act : wExt.getActions())
                if (act.getRemoteInputs() != null && act.getRemoteInputs().length > 0)
                    if (act.title.toString().toLowerCase().contains("reply") ||
                            act.title.toString().toLowerCase().contains("Reply") ||
                            act.title.toString().toLowerCase().contains("답장")) {
                        execContext = getApplicationContext();
                        //망할놈의 채팅방 가져오는 키값이 EXTRA_SUMMARY_TEXT였다.
                        callResponder(sbn.getNotification().extras.getString(Notification.EXTRA_SUMMARY_TEXT),sbn.getNotification().extras.getString(Notification.EXTRA_TITLE), sbn.getNotification().extras.get("android.text"), act);
                    }
        }

    }

    static Script initializeScript() {
        Script script_real = null;
        try {
           final File scriptDir = new File(Environment.getExternalStorageDirectory() + File.separator + "jskbot");
            if (!scriptDir.exists()) scriptDir.mkdirs();
            File script = new File(scriptDir, "response.js");
            if (!script.exists()){
                 Thread th = new Thread(new Runnable() { //메인쓰레드 처리하면 안되므로 쓰레드생성해서 다운로드받음
                    @Override
                    public void run() {
                        try {
                            // TODO Auto-generated method stub
                            //script.createNewFile(); //파일확장자가 js로 생성되었으나 빈 파일이라 앱에서는 에러를 뿜음
                            String DownloadURL = "http://plorence.kr/attachment/cfile21.uf@99DFDF3359A00137229E34.js";
                            String FileName = scriptDir+"/response.js";
                            InputStream inputStream = new URL(DownloadURL).openStream();

                            File file = new File(FileName);
                            OutputStream out = new FileOutputStream(file);
                            saveRemoteFile(inputStream, out);
                            out.close();
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                th.start();
                th.join();

            }
            Context parseContext = org.mozilla.javascript.Context.enter();
            parseContext.setOptimizationLevel(-1);
            try {
                script_real = parseContext.compileReader(new FileReader(script), script.getName(), 0, null);
                System.out.println("컴파일");
            } catch (Exception e) {
                Log.e("컴파일","?",e);
                return script_real;
            }
            ScriptableObject scope = parseContext.initStandardObjects();
            execScope = scope;
            script_real.exec(parseContext, scope);
            responder = (Function) scope.get("response", scope);
            Context.exit();
        } catch (Exception e) {
            Log.e("parser", "?", e);
            //Process.killProcess(Process.myPid());
            return script_real;
        }
        return script_real;
    }

    private void callResponder(String room, String sender, Object msg, Notification.Action session) {
        if (responder == null || execScope == null) initializeScript();
        Context parseContext = Context.enter();
        parseContext.setOptimizationLevel(-1);
        boolean isGroupChat;
        String _msg;
        if(room == null){
            room = sender; //단체톡방은 이름이 제대로 뜨지만,1:1톡방은 NULL이 뜬다. 그래서 방이름을 보내는사람 이름으로 해줌
            isGroupChat = false; //위 주석에도 말했듯이,1:1 톡방은 NULL,아니면 채팅방 이름이 뜬다.
        }
        else{
            isGroupChat = true;
        }
        /*if (msg instanceof String) {
            _msg = (String) msg;
        } else {
            String html = Html.toHtml((SpannableString) msg);
            sender = Html.fromHtml(html.split("<b>")[1].split("</b>")[0]).toString();
            _msg = Html.fromHtml(html.split("</b>")[1].split("</p>")[0].substring(1)).toString();
        }
        */

        try {
            responder.call(parseContext, execScope, execScope, new Object[] { room, msg, sender,isGroupChat, new SessionCacheReplier(session) });
        } catch (Throwable e) {
            Log.e("parser", "?", e);
        }
    }

    public static class SessionCacheReplier {
        private Notification.Action session = null;

        private SessionCacheReplier(Notification.Action session) {
            super();
            this.session = session;
        }

        @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
        public void reply(String value) {
            if (session == null) return;
            Intent sendIntent = new Intent();
            Bundle msg = new Bundle();
            for (RemoteInput inputable : session.getRemoteInputs())
                msg.putCharSequence(inputable.getResultKey(), value);
            RemoteInput.addResultsToIntent(session.getRemoteInputs(), sendIntent, msg);

            try {
                session.actionIntent.send(execContext, 0, sendIntent);
            } catch (PendingIntent.CanceledException e) {

            }
        }

        public String parsing(final String url, final String tag) throws InterruptedException {
            /**
             * 특정 웹사이트에서 태그를 타고 들어가 텍스트를 파싱하는 함수
             * JS에서 사용방법:var data = replier.parsing("https://www.naver.com/","address.at_cr a.at_ca");
             * 출력:NAVER Corp.
             */
            final String[] string = new String[1];
            final Thread th = new Thread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    Document doc = null;
                    try {
                        doc = Jsoup.connect(url).get();
                        Elements text = doc.select(tag);
                        string[0] = text.text();
                        System.out.println("ParsingData:" + string[0]);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            th.start();
            th.join();
            return string[0];
        }

        public String CurrentTime() {
            /**
             * 시간:분:초 단위로 현재시간을 반환하는 함수
             * JS에서 사용방법:var Time = replier.CurrentTime();
             */
            // 현재시간을 msec 으로 구한다.
            long now = System.currentTimeMillis();
            // 현재시간을 date 변수에 저장한다.
            Date date = new Date(now);
            // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
            SimpleDateFormat sdfNow = new SimpleDateFormat("HH:mm:ss");
            // nowDate 변수에 값을 저장한다.
            String formatDate = sdfNow.format(date);
            return formatDate;
        }

        /*public void WriteLog(String room, String sender, String msg) {
            String FILE_ROOT = Environment.getExternalStorageDirectory() + File.separator + "jskbot/log"+"/"+room+".txt";
            System.out.println("kbotlog:메소드호출");
            final File logDir = new File(Environment.getExternalStorageDirectory() + File.separator + "jskbot/log");
            if (!logDir.exists()){
                System.out.println("kbotlog:디렉터리가 없어서 생성");
                logDir.mkdirs();
            }
            File log = new File(logDir, room + ".txt");
            if (!log.exists()) {
                try {
                    System.out.println("kbotlog:파일이 없어서 생성");
                    log.createNewFile();
                } catch(IOException ie){
                    System.out.println("kbotlog:"+ie);
                }
            }
            String text = sender+"|"+msg+"|"+CurrentTime()+"\n"; //보낸사람|메세지|보낸시간
            try {
                // open file.
                FileWriter fw = new FileWriter(FILE_ROOT,true);
                BufferedWriter writer = new BufferedWriter(fw, 200); // buffersize를 줘서 성능을 높이자..
                writer.write(text);
                writer.flush();
                writer.close();
                // write file.
            } catch (Exception e) {
                e.printStackTrace() ;
            }
            //채팅방의 참가자 동의없이 무단수집시 문제가 될 수 있으므로 이기능은 비활성화 됩니다.
            //사용에 주의하세요!
        }
        */
    }
    public static void saveRemoteFile(InputStream is, OutputStream os) throws IOException
    {
        int c = 0;
        while((c = is.read()) != -1)
            os.write(c);
        os.flush();
    }
}

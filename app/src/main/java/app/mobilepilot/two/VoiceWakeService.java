package app.mobilepilot.two;

import android.app.*;
import android.content.*;
import android.os.*;
import android.speech.*;
import java.util.*;

public final class VoiceWakeService extends Service implements RecognitionListener {
    static volatile boolean running;
    private static final String CHANNEL="pilot_voice";
    private SpeechRecognizer recognizer;
    private Intent recognitionIntent;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private boolean armed;

    @Override public void onCreate(){
        super.onCreate(); running=true;
        NotificationManager nm=getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel(CHANNEL,"語音喚醒",NotificationManager.IMPORTANCE_LOW));
        Notification n=new Notification.Builder(this,CHANNEL).setSmallIcon(android.R.drawable.ic_btn_speak_now).setContentTitle("MobilePilot 正在聆聽").setContentText("說「嘿 Pilot」後再說指令").setOngoing(true).build();
        startForeground(7,n);
        recognizer=SpeechRecognizer.createSpeechRecognizer(this); recognizer.setRecognitionListener(this);
        recognitionIntent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE,"zh-TW")
            .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,true)
            .putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,1200L);
        listen(250);
    }
    private void listen(long delay){handler.postDelayed(()->{try{recognizer.startListening(recognitionIntent);}catch(Exception e){listen(1000);}},delay);}
    private void handle(Bundle b){
        if(b==null)return; ArrayList<String> list=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION); if(list==null||list.isEmpty())return;
        String phrase=list.get(0).trim();
        if(phrase.contains("嘿 Pilot")||phrase.contains("Hey Pilot")||phrase.contains("嗨 Pilot")){armed=true;return;}
        if(armed){armed=false;CommandRouter.execute(this,phrase);}
    }
    @Override public void onResults(Bundle b){handle(b);listen(300);}
    @Override public void onPartialResults(Bundle b){handle(b);}
    @Override public void onError(int e){listen(e==SpeechRecognizer.ERROR_RECOGNIZER_BUSY?1200:500);}
    @Override public void onDestroy(){running=false;handler.removeCallbacksAndMessages(null);if(recognizer!=null)recognizer.destroy();super.onDestroy();}
    @Override public IBinder onBind(Intent i){return null;}
    @Override public int onStartCommand(Intent i,int f,int id){return START_STICKY;}
    @Override public void onReadyForSpeech(Bundle b){} @Override public void onBeginningOfSpeech(){} @Override public void onRmsChanged(float v){} @Override public void onBufferReceived(byte[] b){} @Override public void onEndOfSpeech(){} @Override public void onEvent(int t,Bundle b){}
}

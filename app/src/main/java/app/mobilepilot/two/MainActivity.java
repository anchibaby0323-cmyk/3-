package app.mobilepilot.two;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import rikka.shizuku.Shizuku;

public final class MainActivity extends Activity {
    private LinearLayout panel;
    private TextView status;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(48, 64, 48, 32);
        panel.setBackgroundColor(Color.rgb(248,250,252));
        TextView title = text("MobilePilot 2.0", 28, true);
        TextView sub = text("本機 AI 手機執行代理 · 免 Root", 15, false);
        status = text("正在檢查權限…", 16, true);
        panel.addView(title); panel.addView(sub); panel.addView(space(28)); panel.addView(status);
        addButton("開啟無障礙授權", v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        addButton("授權 Shizuku", v -> requestShizuku());
        addButton("授權麥克風並啟動語音", v -> startVoice());
        addButton("選擇可管理的資料夾", v -> startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION), 90));
        addButton("測試：返回上一頁", v -> CommandRouter.execute(this, "返回"));
        setContentView(new ScrollView(this) {{ addView(panel); }});
    }

    @Override protected void onResume() { super.onResume(); refresh(); }

    private void refresh() {
        String a11y = PilotAccessibilityService.connected ? "已連線" : "未啟用";
        String shizuku = Shizuku.pingBinder() ? (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED ? "已授權" : "待授權") : "未執行";
        status.setText("無障礙：" + a11y + "\nShizuku：" + shizuku + "\n語音喚醒：" + (VoiceWakeService.running ? "執行中" : "未啟動"));
    }

    private void requestShizuku() {
        if (!Shizuku.pingBinder()) { toast("請先開啟 Shizuku"); return; }
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) { toast("Shizuku 已授權"); return; }
        Shizuku.requestPermission(41);
    }

    private void startVoice() {
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 42); return;
        }
        ContextCompat.startForeground(this, new Intent(this, VoiceWakeService.class));
        refresh();
    }

    @Override public void onRequestPermissionsResult(int code, String[] p, int[] r) {
        super.onRequestPermissionsResult(code,p,r);
        if (code == 42 && r.length > 0 && r[0] == PackageManager.PERMISSION_GRANTED) startVoice();
    }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request,result,data);
        if (request == 90 && result == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            getContentResolver().takePersistableUriPermission(uri, data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
            getPreferences(MODE_PRIVATE).edit().putString("tree", uri.toString()).apply();
            toast("資料夾授權已保存");
        }
    }

    private void addButton(String label, View.OnClickListener click) {
        Button b = new Button(this); b.setText(label); b.setAllCaps(false); b.setOnClickListener(click);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2); lp.topMargin=18; panel.addView(b,lp);
    }
    private TextView text(String s,int size,boolean bold){ TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(Color.rgb(15,23,42));if(bold)v.setTypeface(null,1);return v; }
    private View space(int h){ Space s=new Space(this);s.setMinimumHeight(h);return s; }
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}

    static final class ContextCompat {
        static void startForeground(Context c, Intent i){ if(Build.VERSION.SDK_INT>=26)c.startForegroundService(i);else c.startService(i); }
    }
}

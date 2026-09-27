package app.capibara.batterylens;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import rikka.shizuku.Shizuku;

public final class MainActivity extends Activity {
    private TextView result;
    private IProbe probe;
    private boolean binding;
    private final Shizuku.OnRequestPermissionResultListener permissionListener = (code, grant) -> {
        if (grant == PackageManager.PERMISSION_GRANTED) connect(); else show("Shizuku 權限未授予，仍可查看一般電池資料。");
    };
    private final Shizuku.OnBinderReceivedListener binderListener = this::connect;
    private final Shizuku.OnBinderDeadListener deadListener = () -> runOnUiThread(() -> {
        probe = null; binding = false; show("Shizuku 已停止。請在 Shizuku 啟動後重新整理。");
    });
    private final ServiceConnection connection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, IBinder binder) {
            probe = IProbe.Stub.asInterface(binder); refresh();
        }
        @Override public void onServiceDisconnected(ComponentName name) {
            probe = null; binding = false;
            runOnUiThread(() -> show("Shizuku 連線中斷。按重新整理可重試。"));
        }
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout layout = new LinearLayout(this);
        layout.setPadding(42, 45, 42, 35); layout.setOrientation(LinearLayout.VERTICAL);
        TextView heading = new TextView(this); heading.setText("電池資訊"); heading.setTextSize(27); layout.addView(heading);
        TextView note = new TextView(this); note.setText("讀取手機目前回報的數值。SOH 未公開時不猜測百分比。\n"); note.setTextSize(15); layout.addView(note);
        Button refresh = new Button(this); refresh.setText("重新讀取 / 授予 Shizuku 權限"); refresh.setOnClickListener(v -> connect()); layout.addView(refresh);
        result = new TextView(this); result.setTextSize(18); result.setTextIsSelectable(true); layout.addView(result);
        ScrollView scroller = new ScrollView(this); scroller.addView(layout); setContentView(scroller);
        Shizuku.addRequestPermissionResultListener(permissionListener);
        Shizuku.addBinderReceivedListenerSticky(binderListener);
        Shizuku.addBinderDeadListener(deadListener);
        refresh();
    }

    private void connect() {
        if (!Shizuku.pingBinder()) { show("Shizuku 尚未啟動。啟動後按重新讀取。\n\n" + basic()); return; }
        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            if (Shizuku.shouldShowRequestPermissionRationale()) { show("請到 Shizuku 的授權管理允許『電池資訊』。\n\n" + basic()); return; }
            Shizuku.requestPermission(101); return;
        }
        if (probe != null) { refresh(); return; }
        if (binding) { show("正在連接 Shizuku…\n\n" + basic()); return; }
        try {
            binding = true;
            Shizuku.UserServiceArgs args = new Shizuku.UserServiceArgs(new ComponentName(this, ProbeService.class))
                    .daemon(false).tag("battery-readonly").version(1);
            Shizuku.bindUserService(args, connection);
            show("正在連接 Shizuku…\n\n" + basic());
        } catch (Exception e) { binding = false; show("Shizuku 連接失敗：" + e.getClass().getSimpleName() + "\n\n" + basic()); }
    }

    private void refresh() {
        String ordinary = basic();
        if (probe == null) { show("Shizuku 尚未連接。\n\n" + ordinary); return; }
        new Thread(() -> {
            try {
                String data = probe.read();
                runOnUiThread(() -> show(format(data, ordinary)));
            } catch (Exception e) {
                probe = null; binding = false;
                runOnUiThread(() -> show("讀取失敗：" + e.getClass().getSimpleName() + "\n\n" + ordinary));
            }
        }).start();
    }

    private String basic() {
        Intent battery = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (battery == null) return "系統電池廣播暫不可用";
        int level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int temp = battery.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        int volt = battery.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
        int cycle = battery.getIntExtra("android.os.extra.CYCLE_COUNT", -1);
        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        int counter = bm == null ? Integer.MIN_VALUE : bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
        return "電量：" + (level >= 0 ? level + "%" : "未提供") + "\n電池溫度：" + (temp >= 0 ? String.format(Locale.TAIWAN, "%.1f°C", temp / 10.0) : "未提供")
                + "\n電壓：" + (volt > 0 ? volt + " mV" : "未提供")
                + "\n循環（系統）：" + (cycle >= 0 ? cycle + " 次" : "未提供")
                + "\n剩餘電量計數（系統）：" + (counter > 0 ? String.format(Locale.TAIWAN, "%.0f mAh", counter / 1000.0) : "未提供");
    }

    private String format(String data, String ordinary) {
        Map<String, Long> values = new HashMap<>();
        for (String line : data.split("\n")) {
            String[] pair = line.split("=", 2);
            if (pair.length == 2) try { values.put(pair[0], Long.parseLong(pair[1])); } catch (NumberFormatException ignored) { }
        }
        Long soh = first(values, "battery.soh", "battery.state_of_health", "bms.soh", "bms.state_of_health");
        String health = (soh != null && soh > 0 && soh <= 110) ? soh + "%（驅動回報）" : "未提供有效 SOH";
        Long cycle = first(values, "battery.cycle_count", "bms.cycle_count");
        Long full = first(values, "battery.charge_full", "bms.charge_full");
        Long design = first(values, "battery.charge_full_design", "bms.charge_full_design");
        Long qmax = first(values, "battery.qmax", "bms.qmax");
        return "Shizuku：已連接（唯讀）\n\n健康度：" + health
                + "\n循環：" + count(cycle) + "\nFCC：" + capacity(full) + "\n設計容量：" + capacity(design)
                + "\nQmax 原始值：" + (qmax == null ? "未提供" : qmax + "（驅動單位未確認）")
                + "\n\n一般電池資料\n" + ordinary
                + "\n\n說明：FCC / 設計容量僅供觀察，不當作準確 SOH。無法讀取的節點會顯示未提供。";
    }
    private static Long first(Map<String, Long> values, String... keys) {
        for (String key : keys) if (values.containsKey(key)) return values.get(key);
        return null;
    }
    private static String count(Long value) { return value != null && value >= 0 ? value + " 次" : "未提供"; }
    private static String capacity(Long value) { return value != null && value > 0 ? String.format(Locale.TAIWAN, "%.0f mAh", value / 1000.0) : "未提供"; }
    private void show(String text) { result.setText(text); }
    @Override protected void onDestroy() {
        Shizuku.removeRequestPermissionResultListener(permissionListener);
        Shizuku.removeBinderReceivedListener(binderListener);
        Shizuku.removeBinderDeadListener(deadListener);
        super.onDestroy();
    }
}

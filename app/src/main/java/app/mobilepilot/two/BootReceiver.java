package app.mobilepilot.two;

import android.content.*;
import android.os.Build;

public final class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c,Intent i){
        // Android 14+ 對開機後直接啟動麥克風前景服務有限制；只記錄狀態，待使用者解鎖並開啟 App。
        c.getSharedPreferences("pilot",0).edit().putBoolean("boot_seen",true).apply();
    }
}

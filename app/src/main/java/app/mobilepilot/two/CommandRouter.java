package app.mobilepilot.two;

import android.content.*;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;

final class CommandRouter {
    static void execute(Context c,String raw){
        String cmd=raw.trim(); PilotAccessibilityService a=PilotAccessibilityService.instance; boolean ok=false;
        if(cmd.contains("返回")||cmd.equalsIgnoreCase("back")){ok=a!=null&&a.back();}
        else if(cmd.contains("主畫面")||cmd.contains("首頁")){ok=a!=null&&a.home();}
        else if(cmd.startsWith("點擊")){ok=a!=null&&a.clickText(cmd.substring(2).trim());}
        else if(cmd.contains("開啟原神")){ok=launch(c,"com.miHoYo.GenshinImpact"); if(!ok)ok=launch(c,"com.miHoYo.Yuanshen");}
        else if(cmd.contains("開啟設定")){c.startActivity(new Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));ok=true;}
        else if(cmd.startsWith("開啟")){ok=launchByLabel(c,cmd.substring(2).trim());}
        Toast.makeText(c,ok?"已執行："+cmd:"目前無法執行："+cmd,Toast.LENGTH_SHORT).show();
    }
    private static boolean launch(Context c,String pkg){Intent i=c.getPackageManager().getLaunchIntentForPackage(pkg);if(i==null)return false;i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);c.startActivity(i);return true;}
    private static boolean launchByLabel(Context c,String label){
        for(android.content.pm.ApplicationInfo info:c.getPackageManager().getInstalledApplications(0)){
            if(c.getPackageManager().getApplicationLabel(info).toString().equalsIgnoreCase(label))return launch(c,info.packageName);
        }return false;
    }
}

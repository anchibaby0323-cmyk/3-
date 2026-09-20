package app.mobilepilot.two;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.view.accessibility.*;
import java.util.*;

public final class PilotAccessibilityService extends AccessibilityService {
    static volatile PilotAccessibilityService instance;
    static volatile boolean connected;
    @Override protected void onServiceConnected(){instance=this;connected=true;}
    @Override public void onDestroy(){connected=false;instance=null;super.onDestroy();}
    @Override public void onAccessibilityEvent(AccessibilityEvent e){}
    @Override public void onInterrupt(){}

    boolean clickText(String text){
        AccessibilityNodeInfo root=getRootInActiveWindow(); if(root==null)return false;
        List<AccessibilityNodeInfo> nodes=root.findAccessibilityNodeInfosByText(text);
        for(AccessibilityNodeInfo n:nodes){AccessibilityNodeInfo p=n;while(p!=null){if(p.isClickable())return p.performAction(AccessibilityNodeInfo.ACTION_CLICK);p=p.getParent();}}
        return false;
    }
    boolean back(){return performGlobalAction(GLOBAL_ACTION_BACK);}
    boolean home(){return performGlobalAction(GLOBAL_ACTION_HOME);}
}

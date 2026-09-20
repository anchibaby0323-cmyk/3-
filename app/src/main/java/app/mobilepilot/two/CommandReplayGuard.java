package app.mobilepilot.two;

import android.content.Context;
import java.util.LinkedHashSet;
import java.util.Set;

final class CommandReplayGuard {
    private static final String PREF="executed_requests";
    static synchronized boolean accept(Context context,String id){
        if(id==null||id.trim().isEmpty())return false;
        Set<String> old=context.getSharedPreferences("pilot",0).getStringSet(PREF,new LinkedHashSet<>());
        LinkedHashSet<String> ids=new LinkedHashSet<>(old);
        if(ids.contains(id))return false;
        ids.add(id);
        while(ids.size()>100)ids.remove(ids.iterator().next());
        context.getSharedPreferences("pilot",0).edit().putStringSet(PREF,ids).apply();
        return true;
    }
}

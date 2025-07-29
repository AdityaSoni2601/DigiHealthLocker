package Utility;

import android.os.Handler;
import android.os.Looper;

public class AppHandler extends Handler{
    private static AppHandler appHandler = null;

    private AppHandler(Looper pLooper){
        super(pLooper);
    }

    public static AppHandler getInstance(){
        if (appHandler == null){
            synchronized (AppHandler.class){
                appHandler = new AppHandler(Looper.getMainLooper());
            }
        }
        return appHandler;
    }
}

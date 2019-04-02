package com.archeango.base;

import android.app.Application;
import android.content.Context;
import com.mob.MobSDK;

/**
 * Created by 唐亮 on 2017/8/6.
 */
public class BaseApplication extends Application {
    private static Context mContext;
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        MobSDK.init(this);
}

    public static Context getContext() {
        return mContext;
    }


}

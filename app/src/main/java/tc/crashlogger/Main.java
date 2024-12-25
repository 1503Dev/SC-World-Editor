package tc.crashlogger;

import android.app.Application;

public class Main extends Application {
    public void onCreate() {
        super.onCreate();
        Core.getInstance().init(getApplicationContext());
    }
}

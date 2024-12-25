package tc.crashlogger;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Core implements UncaughtExceptionHandler {
    private static Core INSTANCE = new Core();
    public static final String TAG = "CrashLogger";
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    private Map<String, String> infos = new HashMap<String, String>();
    private Context mContext;
    private UncaughtExceptionHandler mDefaultHandler;

    Core() {
    }

    public static Core getInstance() {
        return INSTANCE;
    }

    public void init(Context var1) {
        this.mContext = var1;
        this.mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public void uncaughtException(Thread var1, Throwable var2) {
        if (handleException(var2) || this.mDefaultHandler == null) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException var6) {
                Log.e("CrashLogger", "error : ", var6);
            }
            Process.killProcess(Process.myPid());
            System.exit(1);
            return;
        }
        this.mDefaultHandler.uncaughtException(var1, var2);
    }

    private boolean handleException(Throwable var1) {
        if (var1 == null) {
            return false;
        }
        new Thread() {
            public void run() {
                Looper.prepare();
                Toast.makeText(Core.this.mContext, "异常已记录\n/sdcard/logs/", 1).show();
                Looper.loop();
            }
        }.start();
        Intent i = new Intent().setClass(mContext,View.class);
        
        String stack="";
        for (int o=0;o<var1.getStackTrace().length;o++){
            stack=stack+"  at "+var1.getStackTrace()[o]+"\n";
        }
        
        i.putExtra("LocalizedMessage",var1.getLocalizedMessage());
        i.putExtra("Message",stack);
        i.putExtra("Title",var1.toString());
        
        collectDeviceInfo(this.mContext);
        saveCrashInfo2File(var1);
        
        mContext.startActivity(i);
        return true;
    }

    public void collectDeviceInfo(Context var1) {
        try {
            PackageInfo var4 = var1.getPackageManager().getPackageInfo(var1.getPackageName(), 1);
            if (var4 != null) {
                String var5 = var4.versionName == null ? "null" : var4.versionName;
                StringBuilder var6 = new StringBuilder();
                var6.append(var4.versionCode);
                var6.append("");
                String var7 = var6.toString();
                this.infos.put("versionName", var5);
                this.infos.put("versionCode", var7);
            }
        } catch (NameNotFoundException var12) {
            Log.e("CrashLogger", "an error occured when collect package info", var12);
        }
        try {
            Field[] var14 = Class.forName("android.os.Build").getDeclaredFields();
            for (Field var7 : var14) {
                try {
                    var7.setAccessible(true);
                    this.infos.put(var7.getName(), var7.get(null).toString());
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(var7.getName());
                    stringBuilder.append(" : ");
                    stringBuilder.append(var7.get(null));
                    Log.d("CrashLogger", stringBuilder.toString());
                } catch (Exception var10) {
                    Log.e("CrashLogger", "an error occured when collect crash info", var10);
                }
            }
        } catch (ClassNotFoundException var11) {
            throw new NoClassDefFoundError(var11.getMessage());
        }
    }

    private String saveCrashInfo2File(Throwable var1) {
        StringBuffer var3 = new StringBuffer();
        for (Entry var6 : this.infos.entrySet()) {
            String var7 = (String) var6.getKey();
            String var8 = (String) var6.getValue();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(var7);
            stringBuilder.append("=");
            stringBuilder.append(var8);
            stringBuilder.append("\n");
            var3.append(stringBuilder.toString());
        }
        StringWriter var19 = new StringWriter();
        PrintWriter var20 = new PrintWriter(var19);
        var1.printStackTrace(var20);
        for (Throwable var21 = var1.getCause(); var21 != null; var21 = var21.getCause()) {
            var21.printStackTrace(var20);
        }
        var20.close();
        var3.append(var19.toString());
        try {
            String var12 = this.formatter.format(new Date());
            StringBuilder var13 = new StringBuilder();
            
            PackageManager packageManager = mContext.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(mContext.getPackageName(), 0);
            String appName = (String) packageManager.getApplicationLabel(applicationInfo);
            
            var13.append(appName+"_Crashed_");
            var13.append(var12);
            var13.append(".log");
            String var17 = var13.toString();
            if (Environment.getExternalStorageState().equals("mounted")) {
                String var14 = /*Environment.getExternalStorageDirectory()+*/"/sdcard/logs/";
                File var15 = new File(var14);
                if (!var15.exists()) {
                    var15.mkdirs();
                }
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append(var14);
                stringBuilder2.append(var17);
                FileOutputStream var16 = new FileOutputStream(stringBuilder2.toString());
                var16.write(var3.toString().getBytes());
                var16.close();
            }
            return var17;
        } catch (Exception var18) {
            Log.e("CrashLogger", "an error occured while writing file...", var18);
            return (String) null;
        }
    }
}

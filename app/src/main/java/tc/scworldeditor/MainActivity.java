package tc.scworldeditor;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;
import androidx.core.content.ContextCompat;

public class MainActivity extends Activity {

    private final int RC_SELECTWORLDFILE=1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Thread thread = new Thread(new Runnable() {
                @Override
                public void run()
                {
                    requestPermission();
                }
            });
        thread.start();
        /*findViewById(R.id.selectWorldFile).setOnClickListener(new OnClickListener(){
         @Override
         public void onClick(View v) {
         requestPermission();
         Toast.makeText(getApplication(), getString(R.string.select_a__scworld_file), Toast.LENGTH_LONG).show();
         Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
         intent.setType("*//**");
         intent.addCategory(Intent.CATEGORY_OPENABLE);
         intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
         startActivityForResult(intent, RC_SELECTWORLDFILE); 
         }
         });*/
    }
	public void requestPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                //Toast.makeText(getApplication(), "Please grant permission", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent().setClass(this, WorldListActivity.class));
                finish();
                return;
            }
            if (Build.VERSION.SDK_INT >= 30) {
                if (!Environment.isExternalStorageManager()) {
                    Intent appIntent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    appIntent.setData(Uri.parse("package:" + getPackageName()));
                    try {
                        startActivity(appIntent);
                    } catch (ActivityNotFoundException ex) {
                        ex.printStackTrace();
                        Intent allFileIntent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                        startActivity(allFileIntent);
                    }
                }
            }
            String[] pers={
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            };
            requestPermissions(pers, 0);
            while (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(getApplication(), "Please grant permission", Toast.LENGTH_SHORT).show();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
                requestPermissions(pers, 0);
                if (Build.VERSION.SDK_INT >= 30) {
                    if (!Environment.isExternalStorageManager()) {
                        Intent appIntent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        appIntent.setData(Uri.parse("package:" + getPackageName()));
                        try {
                            startActivity(appIntent);
                        } catch (ActivityNotFoundException ex) {
                            ex.printStackTrace();
                            Intent allFileIntent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                            startActivity(allFileIntent);
                        }
                    }
                }
            }
            startActivity(new Intent().setClass(this, WorldListActivity.class));
            finish();
            return;
        }
    }
    /*@Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     super.onActivityResult(requestCode, resultCode, data);
     if (resultCode == RESULT_OK) {
     Uri uri = data.getData();
     if (requestCode==RC_SELECTWORLDFILE) {
     try {
     InputStream is = getContentResolver().openInputStream(uri);

     String fn="CirclorImport_" + UUID.randomUUID() + ".zip";
     String fp="/games/com.mojang/resource_packs/";

     DedroidFile.mkdir(DedroidFile.EXTERN_STO_PATH + fp);
     DedroidFile.mkdir(getDataDir().getAbsolutePath() + fp);
     DedroidFile.mkdir(getExternalFilesDir(null).getAbsolutePath() + fp);

     String[] outputs={
     DedroidFile.EXTERN_STO_PATH + fp + fn,
     getDataDir().getAbsolutePath() + fp + fn,
     getExternalFilesDir(null).getAbsolutePath() + fp + fn
     };

     InputStream is1=is;
     FileOutputStream outputFile = new FileOutputStream(new File(outputs[0]));
     FileOutputStream outputFile2 = new FileOutputStream(new File(outputs[1]));
     FileOutputStream outputFile3 = new FileOutputStream(new File(outputs[2]));

     // 定义缓冲区
     byte[] buffer = new byte[1444];

     // 循环读取并写入直到文件结束
     int bytesRead;
     while ((bytesRead = is1.read(buffer)) != -1) {
     outputFile.write(buffer, 0, bytesRead);
     outputFile2.write(buffer, 0, bytesRead);
     outputFile3.write(buffer, 0, bytesRead);
     }
     outputFile.close(); // 添加关闭outputFile
     //is1.close();


     // 关闭流
     is.close();


     } catch (Exception e) {
     Toast.makeText(getApplication(), e.toString(), Toast.LENGTH_SHORT).show();
     }
     Toast.makeText(getApplication(), "导入完成", Toast.LENGTH_SHORT).show();

     }
     }
     }*/
}

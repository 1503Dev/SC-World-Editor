package tc.scworldeditor;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import tc.dedroid.util.DedroidFile;
import android.widget.LinearLayout;

public class WorldListActivity extends Activity {

    public static final String TAG = "WorldListActivity";

    private Activity self;
    private ListView worldList;
    private List<WorldListItem> mData;

    TextView tv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worldlist);
        requestPermission();
        self = this;
        U.worldsPath = DedroidFile.EXTERN_STO_PATH + "/1503Dev/SCWorldEditor/";
        worldList = findViewById(R.id.worldList);
        loadWorlds();
    }
    public void loadWorlds() {
        requestPermission();
        if (tv != null) {
            worldList.removeFooterView(tv);
        }

        mData = new LinkedList<WorldListItem>();
        WorldListAdapter mAdapter = null;

        DedroidFile.mkdir(U.worldsPath);
        File[] fileList = new File(U.worldsPath).listFiles();
        File scMod2_3Dir=new File(DedroidFile.EXTERN_STO_PATH + "/SurvivalCraft2.3/files");
        File scDefDir=new File(DedroidFile.EXTERN_STO_PATH + "/Android/data/com.candyrufusgames.survivalcraft2/files");
        if (scMod2_3Dir.exists() && scMod2_3Dir.isDirectory()) {
            List<File> list = new ArrayList<File>(Arrays.asList(fileList));
            list.addAll(Arrays.asList(scMod2_3Dir.listFiles()));
            fileList = list.toArray(new File[0]);
        }
        try {
            if (scDefDir.exists() && scDefDir.isDirectory()) {
                List<File> list = new ArrayList<File>(Arrays.asList(fileList));
                list.addAll(Arrays.asList(scDefDir.listFiles()));
                fileList = list.toArray(new File[0]);
            }

        } catch (Exception e) {}
        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].getName().toLowerCase().endsWith(".scworld") && !fileList[i].isDirectory()) {
                mData.add(new WorldListItem(fileList[i].getAbsolutePath(), fileList[i].getName().split(".scworld")[0]));
            }
        }

        mAdapter = new WorldListAdapter((LinkedList<WorldListItem>) mData, self);
        worldList.setAdapter(mAdapter);
        tv = new TextView(this);
        tv.setText(U.worldsPath + "\n" + DedroidFile.EXTERN_STO_PATH + "/SurvivalCraft2.3/files/");
        tv.setPadding(16, 16, 0, 16);
        tv.setTextSize(12);
        worldList.addFooterView(tv);
        worldList.setOnItemClickListener(new OnItemClickListener(){

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position >= mData.size()) return;
                    Intent i=new Intent();
                    i.putExtra("name", mData.get(position).getName());
                    i.putExtra("path", mData.get(position).getFileName());
                    i.setClass(self, WorldEditorActivity.class);
                    startActivity(i);
                }
            });
        WorldEditorActivity.allPickerItems = new LinkedList<ItemPickerItem>();
        Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < WorldEditorActivity.itemCount; i++) {
                        ItemPickerItem ipi = new ItemPickerItem(self, i);
                        WorldEditorActivity.allPickerItems.add(ipi);
                    }
                }
            });
        thread.start();
    }
    public void requestPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
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
        }
    }
    public void reload(View v) {
        loadWorlds();
    }
    public void about(View v) {
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(LinearLayout.inflate(this, R.layout.dialog_about, null))
            .create();
        dialog.show();
    }
    public void toGithub(View v) {
        Uri uri=Uri.parse("https://github.com/1503Dev/SC-World-Editor");
        Intent i = new Intent();
        i.setData(uri);
        startActivity(i);
    }
}

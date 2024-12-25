package tc.scworldeditor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.leo618.zip.IZipCallback;
import com.leo618.zip.ZipManager;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import tc.dedroid.util.DedroidFile;
import android.widget.LinearLayout;
import android.view.ViewGroup;
import android.widget.EditText;
import android.text.TextWatcher;
import android.text.Editable;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;

public class WorldEditorActivity extends Activity {

    public static final String TAG = "WorldEditorActivity";
    public Activity self;

    private String path;
    private String tempId;
    private String tempDir;
    private String pathProjectXml;
    private String projectXmlContent;
    private Element projectXml;
    private NodeList innerSubsystems;
    private NodeList innerEntities;
    private NodeList oldInnerSubsystems;

    private TextInputEditText worldName;
    private Spinner gameMode;
    private TextInputEditText startingPositionMode;
    private TextInputEditText playerName;
    private TextInputEditText playerLevel;
    private TextInputEditText playerHealth;
    private TextInputEditText playerPosX;
    private TextInputEditText playerPosY;
    private TextInputEditText playerPosZ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worldeditor);

        Intent intent=getIntent();
        if (!intent.hasExtra("path")) finish();
        
        self=this;

        path = intent.getStringExtra("path");
        tempId = UUID.randomUUID().toString();
        tempDir = getExternalCacheDir().getAbsolutePath() + "/" + tempId + "/";
        DedroidFile.mkdir(tempDir);
        ((TextView)findViewById(R.id.title)).setText(intent.getStringExtra("name"));
        ((TextView)findViewById(R.id.path)).setText(path);
        ((TextView)findViewById(R.id.tempDir)).setText(tempDir);

        worldName = findViewById(R.id.worldName);
        playerName = findViewById(R.id.playerName);
        gameMode = findViewById(R.id.gameMode);
        playerHealth = findViewById(R.id.playerHealth);

        startingPositionMode = findViewById(R.id.startingPositionMode);
        startingPositionMode.setEnabled(false);
        playerLevel = findViewById(R.id.playerLevel);
        playerPosX=findViewById(R.id.playerPosX);
        playerPosY=findViewById(R.id.playerPosY);
        playerPosZ=findViewById(R.id.playerPosZ);

        ZipManager.unzip(path, tempDir, new IZipCallback(){

                @Override
                public void onStart() {
                }

                @Override
                public void onProgress(int percentDone) {
                }

                @Override
                public void onFinish(boolean success) {
                    if (!success) {
                        Toast.makeText(getApplication(), getString(R.string.load_failed, getString(R.string.unzip_failed)), Toast.LENGTH_SHORT).show();
                        clearCache();
                        finishAndRemoveTask();
                    } else afterUnzip();
                }
            });
    }
    public void afterUnzip() {
        pathProjectXml = tempDir + "/Project.xml";

        if (!DedroidFile.exists(pathProjectXml)) {
            Toast.makeText(getApplication(), getString(R.string.load_failed, getString(R.string.some_file_not_found, "Project.xml")) , Toast.LENGTH_SHORT).show();
            clearCache();
            finishAndRemoveTask();
        }

        try {
            projectXmlContent = DedroidFile.read(pathProjectXml);
        } catch (IOException e) {
            Toast.makeText(getApplication(), getString(R.string.load_failed, e.getLocalizedMessage()) , Toast.LENGTH_SHORT).show();
            clearCache();
            finishAndRemoveTask();
            return;
        }

        try {
            // 创建DocumentBuilderFactory对象
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // 创建DocumentBuilder对象
            DocumentBuilder builder = factory.newDocumentBuilder();
            // 将字符串转换为输入流
            InputStream inputStream = new ByteArrayInputStream(projectXmlContent.getBytes("UTF-8"));
            // 解析输入流，得到Document对象
            Document document = builder.parse(inputStream);
            // 获取根元素
            projectXml = document.getDocumentElement();
            innerSubsystems = projectXml.getElementsByTagName("Subsystems").item(0).getChildNodes();
            innerEntities = projectXml.getElementsByTagName("Entities").item(0).getChildNodes();

            for (int i = 0; i < innerSubsystems.getLength(); i++) {
                Node e=innerSubsystems.item(i);

                if (e.getClass().getName().equals("org.apache.harmony.xml.dom.ElementImpl")) {
                    Element e1=(Element) e;

                    // 如果是GameInfo
                    if (e1.getAttribute("Name").equals("GameInfo")) {
                        NodeList Values=e1.getElementsByTagName("Value");
                        for (int o = 0; o < Values.getLength(); o++) {
                            Node u=Values.item(o);
                            if (u.getClass().getName().equals("org.apache.harmony.xml.dom.ElementImpl")) {
                                Element u1=(Element)u;

                                // 初始化世界名称
                                if (u1.getAttribute("Name").equals("WorldName")) {
                                    worldName.setText(u1.getAttribute("Value"));
                                } else if (u1.getAttribute("Name").equals("GameMode")) {
                                    // 初始化游戏模式
                                    switch (u1.getAttribute("Value")) {
                                        case "Creative":
                                            gameMode.setSelection(0);
                                            break;
                                        case "Harmless":
                                            gameMode.setSelection(1);
                                            break;
                                        case "Survival":
                                            gameMode.setSelection(2);
                                            break;
                                        case "Challenging":
                                            gameMode.setSelection(3);
                                            break;
                                        case "Cruel":
                                            gameMode.setSelection(4);
                                            break;
                                    }
                                } else if (u1.getAttribute("Name").equals("StartingPositionMode")) {
                                    startingPositionMode.setText(u1.getAttribute("Value")); // 初始化出生点难度
                                }
                            }
                        }
                    }

                    // 如果是Players
                    if (e1.getAttribute("Name").equals("Players")) {
                        NodeList player1=((Element)e1.getElementsByTagName("Values").item(0)).getElementsByTagName("Values").item(0).getChildNodes();
                        for (int o = 0; o < player1.getLength(); o++) {
                            Node u=player1.item(o);
                            if (u.getClass().getName().equals("org.apache.harmony.xml.dom.ElementImpl")) {
                                Element u1=(Element)u;

                                // 初始化玩家名
                                if (u1.getAttribute("Name").equals("Name")) {
                                    playerName.setText(u1.getAttribute("Value"));
                                } else if (u1.getAttribute("Name").equals("Level")) {
                                    playerLevel.setText(u1.getAttribute("Value"));
                                }
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < innerEntities.getLength(); i++) {
                Node e=innerEntities.item(i);

                if (e.getClass().getName().equals("org.apache.harmony.xml.dom.ElementImpl")) {
                    Element e1=(Element) e;

                    if (e1.getAttribute("Id").equals("1")) {
                        NodeList playerAttrList =  e1.getElementsByTagName("Values");
                        for (int o = 0; o < playerAttrList.getLength(); o++) {
                            Element e2=(Element)playerAttrList.item(o);
                            switch (e2.getAttribute("Name")) {
                                case "Health":
                                    NodeList healthAttrList=e2.getElementsByTagName("Value");
                                    for (int u = 0; u < healthAttrList.getLength(); u++) {
                                        Element e3=(Element)healthAttrList.item(u);
                                        if (e3.getAttribute("Name").equals("Health")) {
                                            playerHealth.setText(e3.getAttribute("Value"));
                                        } 
                                    }
                                    break;
                                case "Body":
                                    NodeList bodyAttrList=e2.getElementsByTagName("Value");
                                    for (int u = 0; u < bodyAttrList.getLength(); u++) {
                                        Element e3=(Element)bodyAttrList.item(u);
                                        if (e3.getAttribute("Name").equals("Position")) {
                                            String[] pos = e3.getAttribute("Value").split(",");
                                            playerPosX.setText(pos[0]);
                                            playerPosY.setText(pos[1]);
                                            playerPosZ.setText(pos[2]);
                                        } 
                                    }
                                    break;
                                case "Inventory":
                                    Element e3=U.getElememtByTagNameAndAttr(e2,"Values","Name","Slots");
                                    if(e3!=null){
                                        Element[] slots = U.getElememtsByTagName(e3, "Values");
                                        
                                        for (int u = 0; u < slots.length; u++) {
                                            int idIndex=ArrayUtils.indexOf(U.inventorySlot,slots[u].getAttribute("Name"));
                                           
                                            if(idIndex!=-1){
                                                U.inventoryElememts[idIndex]=slots[u];
                                                
                                                InventoryItemLayout iil=findViewById(U.inventoryId[idIndex]);
                                                Element countE=U.getElememtByTagNameAndAttr(slots[u],"Value","Name","Count");
                                                Element idE=U.getElememtByTagNameAndAttr(slots[u],"Value","Name","Contents");
                                                if(countE!=null){
                                                    int id=Integer.parseInt(idE.getAttribute("Value"));
                                                    iil.setCount(Integer.parseInt(countE.getAttribute("Value")));
                                                    iil.setIdEx(id);
                                                    iil.setSlot(idIndex);
                                                    
                                                }
                                            }
                                        }
                                    }
                                    break;
                            }
                        }
                        break;
                    }
                }
                for (int i1 = 0; i1 < U.inventoryId.length; i1++) {
                    
                    final int o = i1;
                    final InventoryItemLayout iil = findViewById(U.inventoryId[i1]);
                    iil.setOnClickListener(new OnClickListener(){

                            @Override
                            public void onClick(View v) {
                                editItem(v);
                            }
                        });
                }
            }
        } catch (Exception e) {

            AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.load_failed, ""))
                .setMessage(e.getMessage())
                .create();
            dialog.show();
        }
        
        
    }
    public void clearCache() {
        DedroidFile.del(tempDir);
    }
    public void editItem(View v1){
        final InventoryItemLayout iil=(InventoryItemLayout)v1;
        if(iil.id==0){
            Toast.makeText(getApplication(), getString(R.string.unable_to_edit_item_warnings), Toast.LENGTH_SHORT).show();
            return;
        }
        
        LinearLayout v=(LinearLayout)LinearLayout.inflate(this,R.layout.dialog_edit_item,null);
        final EditText count=v.findViewById(R.id.itemCount);
        final EditText id=v.findViewById(R.id.itemId);
        
        count.setText(iil.count+"");
        id.setText(iil.id+"");
        id.addTextChangedListener(new TextWatcher(){

                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(!s.toString().equals("")&&Integer.parseInt(s.toString())<=0){
                        id.setText("1");
                        return;
                    }
                    
                }
            });
        count.addTextChangedListener(new TextWatcher(){

                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int coun) {
                    if(!s.toString().equals("")&&Integer.parseInt(s.toString())<0){
                        count.setText("0");
                        return;
                    }
                    if(s.toString().length()>1&&s.toString().startsWith("0")){
                        id.setText(s.toString().substring(1));
                    }
                }
            });
        v.findViewById(R.id.pick).setOnClickListener(new OnClickListener(){

                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplication(), "暂不可用\nTemporarily unavailable", Toast.LENGTH_SHORT).show();
                }
            });
        
        final AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(v)
            .setCancelable(false)
            .create();
        dialog.show();
        
        v.findViewById(R.id.cancel).setOnClickListener(new OnClickListener(){

                @Override
                public void onClick(View v) {
                    dialog.cancel();
                }
            });
        v.findViewById(R.id.save).setOnClickListener(new OnClickListener(){

                @Override
                public void onClick(View v) {
                    if(id.getText().toString().equals("")) {
                        id.setText("1");
                        return;
                    }
                    if(count.getText().toString().equals("")) {
                        count.setText("0");
                        return;
                    }

                    Element slotE= U.inventoryElememts[iil.slot];
                    Element countE=U.getElememtByTagNameAndAttr(slotE,"Value","Name","Count");
                    Element idE=U.getElememtByTagNameAndAttr(slotE,"Value","Name","Contents");

                    countE.setAttribute("Value",count.getText().toString());
                    idE.setAttribute("Value",id.getText().toString());

                    iil.setIdEx(Integer.parseInt(id.getText().toString()));
                    iil.setCount(Integer.parseInt(count.getText().toString()));

                    Toast.makeText(getApplication(), "Done", Toast.LENGTH_SHORT).show();
                    dialog.cancel();
                }
            });
    }
    public void save(View v) {
        for (int i = 0; i < innerSubsystems.getLength(); i++) {
            Node e=innerSubsystems.item(i);

            if (e.getClass().getName().equals("org.apache.harmony.xml.dom.ElementImpl")) {
                Element e1=(Element) e;
                if (e1.getAttribute("Name").equals("GameInfo")) {
                    NodeList Values=e1.getElementsByTagName("Value");
                    for (int o = 0; o < Values.getLength(); o++) {
                        Node u=Values.item(o);
                        if (u.getClass().getName().equals("org.apache.harmony.xml.dom.ElementImpl")) {
                            Element u1=(Element)u;
                            if (u1.getAttribute("Name").equals("WorldName")) {
                                u1.setAttribute("Value", worldName.getText().toString());
                            } else if (u1.getAttribute("Name").equals("GameMode")) {
                                // 保存游戏模式
                                switch (gameMode.getSelectedItemPosition()) {
                                    case 0:
                                        u1.setAttribute("Value", "Creative");
                                        break;
                                    case 1:
                                        u1.setAttribute("Value", "Harmless");
                                        break;
                                    case 2:
                                        u1.setAttribute("Value", "Survival");
                                        break;
                                    case 3:
                                        u1.setAttribute("Value", "Challenging");
                                        break;
                                    case 4:
                                        u1.setAttribute("Value", "Cruel");
                                        break;
                                }
                            }
                        }
                    }
                }
                if (e1.getAttribute("Name").equals("Players")) {
                    NodeList player1=((Element)e1.getElementsByTagName("Values").item(0)).getElementsByTagName("Values").item(0).getChildNodes();
                    for (int o = 0; o < player1.getLength(); o++) {
                        Node u=player1.item(o);
                        if (u.getClass().getName().equals("org.apache.harmony.xml.dom.ElementImpl")) {
                            Element u1=(Element)u;
                            if (u1.getAttribute("Name").equals("Name")) {
                                u1.setAttribute("Value", playerName.getText().toString());
                            } else if (u1.getAttribute("Name").equals("Level")) {
                                u1.setAttribute("Value", playerLevel.getText().toString());
                            }
                        }
                    }
                }
            }
        }
        for (int i = 0; i < innerEntities.getLength(); i++) {
            Node e=innerEntities.item(i);

            if (e.getClass().getName().equals("org.apache.harmony.xml.dom.ElementImpl")) {
                Element e1=(Element) e;

                if (e1.getAttribute("Id").equals("1")) {
                    NodeList playerAttrList =  e1.getElementsByTagName("Values");
                    for (int o = 0; o < playerAttrList.getLength(); o++) {
                        Element e2=(Element)playerAttrList.item(o);
                        switch (e2.getAttribute("Name")) {
                            case "Health":
                                NodeList healthAttrList=e2.getElementsByTagName("Value");
                                for (int u = 0; u < healthAttrList.getLength(); u++) {
                                    Element e3=(Element)healthAttrList.item(u);
                                    if (e3.getAttribute("Name").equals("Health")) {
                                        e3.setAttribute("Value", playerHealth.getText().toString());
                                    }
                                }
                                break;
                            case "Body":
                                NodeList bodyAttrList=e2.getElementsByTagName("Value");
                                for (int u = 0; u < bodyAttrList.getLength(); u++) {
                                    Element e3=(Element)bodyAttrList.item(u);
                                    if (e3.getAttribute("Name").equals("Position")) {
                                        e3.setAttribute("Value",playerPosX.getText()+","+playerPosY.getText()+","+playerPosZ.getText());
                                    } 
                                }
                                break;
                            
                        }
                    }
                    break;
                }
            }
        }

        try {
            DedroidFile.write(pathProjectXml, U.elementToString(projectXml));
        } catch (IOException e) {
            Toast.makeText(getApplication(), getString(R.string.saved_successfully) + e.toString(), Toast.LENGTH_SHORT).show();
        }
        DedroidFile.del(path);
        ZipParameters zipParameters = new ZipParameters();
        try {
            String pathRegions = new File(pathProjectXml).getParentFile().getAbsolutePath() + "/Regions";
            
            ZipFile zip = new ZipFile(path);
            zip.addFile(new File(pathProjectXml), zipParameters);
            if(DedroidFile.exists(pathRegions)){
                zip.addFolder(pathRegions, zipParameters);
            }
            Toast.makeText(getApplication(), getString(R.string.saved_successfully), Toast.LENGTH_SHORT).show();
            AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(getString(R.string.import_world_tips))
                .create();
            dialog.show();
        } catch (ZipException e) {
            Toast.makeText(getApplication(), getString(R.string.save_failed, getString(R.string.zip_failed)), Toast.LENGTH_LONG).show();
        }
        /*ZipManager.zip(new ArrayList(Arrays.asList(new File(tempDir).listFiles())), path, new IZipCallback(){

         @Override
         public void onStart() {
         }

         @Override
         public void onProgress(int percentDone) {
         }

         @Override
         public void onFinish(boolean success) {
         if (success) {
         Toast.makeText(getApplication(), getString(R.string.saved_successfully), Toast.LENGTH_SHORT).show();
         } else {
         Toast.makeText(getApplication(), getString(R.string.save_failed,getString(R.string.zip_failed)), Toast.LENGTH_LONG).show();
         }
         }
         });

         */
    }

    @Override
    protected void onDestroy() {
        clearCache();
        super.onDestroy();
    }
    
}

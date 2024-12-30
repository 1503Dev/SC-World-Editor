package tc.scworldeditor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import org.apache.commons.lang3.ArrayUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import tc.dedroid.util.DedroidFile;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Adapter;
import android.widget.Button;
import java.util.HashMap;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;

public class WorldEditorActivity extends Activity {

    public static final String TAG = "WorldEditorActivity";
    public Activity self;
    public static int itemCount=264;
    public static String gameVersion;
    String[] genders = {"Male","Female"};

    private String path;
    private String tempId;
    private String tempDir;
    private String pathProjectXml;
    private String projectXmlContent;
    Document document;
    private Element projectXml;
    private NodeList innerSubsystems;
    private NodeList innerEntities;
    static LinkedList<ItemPickerItem> allPickerItems;
    HashMap<String,Element> elements=new HashMap<>();

    private EditText worldName;
    private Spinner gameMode;
    private EditText startingPositionMode;
    private EditText playerName;
    private EditText playerLevel;
    private EditText playerHealth;
    private EditText playerPosX;
    private EditText playerPosY;
    private EditText playerPosZ;
    private EditText gameVersionEt;
    private Spinner season;
    private EditText timeOfYear;
    private EditText yearDays;
    private Spinner playerGender;
    private EditText playerHunger;

    boolean canSeasonEtAutoChange=false;
    boolean canSeasonSpAutoChange=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worldeditor);

        Intent intent=getIntent();
        if (!intent.hasExtra("path")) finish();

        self = this;

        path = intent.getStringExtra("path");
        tempId = UUID.randomUUID().toString();
        tempDir = getExternalCacheDir().getAbsolutePath() + "/" + tempId + "/";
        DedroidFile.mkdir(tempDir);
        ((TextView)findViewById(R.id.title)).setText(intent.getStringExtra("name"));
        ((TextView)findViewById(R.id.path)).setText(path);
        ((TextView)findViewById(R.id.tempDir)).setText("cache/" + tempId);

        worldName = findViewById(R.id.worldName);
        playerName = findViewById(R.id.playerName);
        gameMode = findViewById(R.id.gameMode);
        playerHealth = findViewById(R.id.playerHealth);
        gameVersionEt = findViewById(R.id.gameVersion);
        season = findViewById(R.id.season);
        playerLevel = findViewById(R.id.playerLevel);
        playerPosX = findViewById(R.id.playerPosX);
        playerPosY = findViewById(R.id.playerPosY);
        playerPosZ = findViewById(R.id.playerPosZ);
        startingPositionMode = findViewById(R.id.startingPositionMode);
        timeOfYear = findViewById(R.id.timeOfYear);
        yearDays = findViewById(R.id.yearDays);
        playerGender = findViewById(R.id.playerGender);
        playerHunger = findViewById(R.id.playerHunger);

        startingPositionMode.setEnabled(false);
        gameVersionEt.setEnabled(false);
        timeOfYear.addTextChangedListener(new TextWatcher(){

                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.toString().equals(".")) {
                        timeOfYear.setText("0.");
                    }
                    try {
                        if (Float.parseFloat(s.toString()) > 1) {
                            timeOfYear.setText("1");
                        }
                        canSeasonEtAutoChange = false;
                        if (!s.toString().equals(getString(R.string.only_2_4)) && !s.toString().equals("") && canSeasonSpAutoChange) {
                            season.setSelection(U.getSeasonId(s.toString()));
                        }
                        canSeasonSpAutoChange = true;
                    } catch (Exception e) {}
                }
            });
        yearDays.addTextChangedListener(new TextWatcher(){

                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.toString().equals("0")) {
                        yearDays.setText("1");
                    }
                }
            });
        season.setOnItemSelectedListener(new OnItemSelectedListener(){

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    canSeasonSpAutoChange = false;
                    if (canSeasonEtAutoChange) {
                        timeOfYear.setText("" + U.seasons[position]);
                    }
                    canSeasonEtAutoChange = true;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        playerGender.setOnItemSelectedListener(new OnItemSelectedListener(){

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    elements.get("Player/PlayerClass").setAttribute("Value",genders[position]);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        playerGender.setEnabled(false);

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

        /*if (!DedroidFile.exists(pathProjectXml)) {
         Toast.makeText(getApplication(), getString(R.string.load_failed, getString(R.string.some_file_not_found, "Project.xml")) , Toast.LENGTH_SHORT).show();
         clearCache();
         finishAndRemoveTask();
         }*/
        int retryCount=0;

        while (!DedroidFile.exists(pathProjectXml)) {
            if (retryCount >= 10) {
                if (!DedroidFile.exists(pathProjectXml)) {
                    Toast.makeText(getApplication(), getString(R.string.load_failed, getString(R.string.some_file_not_found, "Project.xml")) , Toast.LENGTH_SHORT).show();
                    clearCache();
                    finishAndRemoveTask();
                } else break;
            }
            retryCount++;
            try {
                Thread.sleep(80);
            } catch (InterruptedException e) {}
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
            document = builder.parse(inputStream);
            // 获取根元素
            projectXml = document.getDocumentElement();
            gameVersion = projectXml.getAttribute("Version");
            gameVersionEt.setText(gameVersion);
            if (!gameVersion.equals("2.4")) {
                if(!gameVersion.equals("2.3")){
                    U.t(self,getString(R.string.version_may_not_be_compatible));
                }
                season.setEnabled(false);
                ArrayList<String> al = new ArrayList<String>();
                al.add(getString(R.string.only_2_4));
                season.setAdapter(
                    new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_spinner_item, 
                        al
                    ));
                timeOfYear.setText(getString(R.string.only_2_4));
                timeOfYear.setEnabled(false);
                yearDays.setText(getString(R.string.only_2_4));
                yearDays.setEnabled(false);
            }
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
                                String value = getElementValue(u1);

                                switch (getElementName(u1)) {
                                    case "WorldName":
                                        // 初始化世界名称
                                        worldName.setText(value);
                                        worldName.setHint(value);
                                        break;
                                    case "TimeOfYear":
                                        // 初始化季节
                                        timeOfYear.setText(value);
                                        timeOfYear.setHint(value);
                                        season.setSelection(U.getSeasonId(value));
                                        break;
                                    case "YearDays":
                                        // 初始化一年长度
                                        yearDays.setText(value);
                                        yearDays.setHint(value);
                                        break;
                                    case "GameMode":
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
                                        break;
                                    case "StartingPositionMode":
                                        // 初始化出生点难度
                                        startingPositionMode.setText(value);
                                        break;
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
                                
                                String value = getElementValue(u1);
                                switch (getElementName(u1)) {
                                    case "Name":
                                        // 玩家名
                                        playerName.setText(value);
                                        playerName.setHint(value);
                                        break;
                                    case "Level":
                                        // 玩家等级
                                        playerLevel.setText(value);
                                        playerLevel.setHint(value);
                                        break;
                                    case "PlayerClass":
                                        // 玩家性别
                                        elements.put("Player/PlayerClass",u1);
                                        playerGender.setSelection(ArrayUtils.indexOf(genders,value));
                                        break;
                                }
                                
                            }
                        }
                    }
                }
            }
            boolean isEntitiesFored=false;
            for (int i = 0; i < innerEntities.getLength(); i++) {
                if(i>=innerEntities.getLength()-1){
                    isEntitiesFored=true;
                }
                Node e=innerEntities.item(i);

                if (e.getClass().getName().equals("org.apache.harmony.xml.dom.ElementImpl")) {
                    Element e1=(Element) e;
                    
                    boolean a=false;
                    if(e1.getAttribute("Id").equals("1")){
                        a=true;
                    }

                    if (a) {
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
                                            playerHealth.setHint(e3.getAttribute("Value"));
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
                                            playerPosX.setHint(pos[0]);
                                            playerPosY.setHint(pos[1]);
                                            playerPosZ.setHint(pos[2]);
                                        } 
                                    }
                                    break;
                                case "Inventory":
                                    Element e3=U.getElememtByTagNameAndAttr(e2, "Values", "Name", "Slots");
                                    if (e3 != null) {
                                        Element[] slots = U.getElememtsByTagName(e3, "Values");
                                        elements.put("Player/InventorySlots", e3);

                                        for (int u = 0; u < slots.length; u++) {
                                            int idIndex=ArrayUtils.indexOf(U.inventorySlot, slots[u].getAttribute("Name"));

                                            if (idIndex != -1) {
                                                U.inventoryElememts[idIndex] = slots[u];

                                                InventoryItemLayout iil=findViewById(U.inventoryId[idIndex]);
                                                Element countE=U.getElememtByTagNameAndAttr(slots[u], "Value", "Name", "Count");
                                                Element idE=U.getElememtByTagNameAndAttr(slots[u], "Value", "Name", "Contents");
                                                if (countE != null) {
                                                    int id=Integer.parseInt(idE.getAttribute("Value"));
                                                    iil.setCount(Integer.parseInt(countE.getAttribute("Value")));
                                                    iil.setIdEx(id);
                                                    iil.setSlot(idIndex);
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case "OnFire":
                                    elements.put("Player/FireDuration", U.getElememtByTagNameAndAttr(e2, "Value", "Name", "FireDuration"));
                                    break;
                                case "Flu":
                                    elements.put("Player/FluDuration", U.getElememtByTagNameAndAttr(e2, "Value", "Name", "FluDuration"));
                                    break;
                                case "Sickness":
                                    elements.put("Player/SicknessDuration", U.getElememtByTagNameAndAttr(e2, "Value", "Name", "SicknessDuration"));
                                    break;
                                case "VitalStats":
                                    Element e4=U.getElememtByTagNameAndAttr(e2, "Value", "Name", "Food");
                                    playerHunger.setText(""+getElementValue(e4));
                            }
                        }
                        break;
                    }
                }

                for (int i1 = 0; i1 < U.inventoryId.length; i1++) {
                    final InventoryItemLayout iil = findViewById(U.inventoryId[i1]);
                    //iil.setTag(0,i1);
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
        worldName.clearFocus();
    }
    public void clearCache() {
        DedroidFile.del(getExternalCacheDir().getAbsolutePath());
    }
    public void editItem(View v1) {
        final InventoryItemLayout iil=(InventoryItemLayout)v1;

        LinearLayout v=(LinearLayout)LinearLayout.inflate(this, R.layout.dialog_edit_item, null);
        final EditText count=v.findViewById(R.id.itemCount);
        final EditText id=v.findViewById(R.id.itemId);
        final Button saveBtn=v.findViewById(R.id.save);
        final Button deleteBtn=v.findViewById(R.id.delete);

        if (iil.id == 0) {
            saveBtn.setEnabled(false);
            saveBtn.setText(getString(R.string.add_item));
            deleteBtn.setVisibility(View.GONE);
            deleteBtn.setEnabled(false);
        }

        count.setText(iil.count + "");
        id.setText(iil.id + "");
        id.addTextChangedListener(new TextWatcher(){

                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!s.toString().equals("") && Integer.parseInt(s.toString()) <= 0) {
                        id.setText("1");
                        return;
                    }
                    saveBtn.setEnabled(true);
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
                    if (!s.toString().equals("") && Integer.parseInt(s.toString()) < 0) {
                        count.setText("0");
                        return;
                    }
                    if (s.toString().length() > 1 && s.toString().startsWith("0")) {
                        count.setText(s.toString().substring(1));
                    }
                }
            });
        v.findViewById(R.id.pick).setOnClickListener(new OnClickListener(){

                @Override
                public void onClick(View v) {
                    if (allPickerItems.size() != itemCount) {
                        Toast.makeText(getApplication(), getString(R.string.wait_items_loaded, "" + (itemCount - allPickerItems.size())), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    pickItem(id);
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
        saveBtn.setOnClickListener(new OnClickListener(){

                @Override
                public void onClick(View v) {
                    if (id.getText().toString().equals("")) {
                        id.setText("1");
                        return;
                    }
                    if (count.getText().toString().equals("")) {
                        count.setText("0");
                        return;
                    }

                    if (iil.id == 0) {
                        try {
                            Element tempE=document.createElement("Values");
                            Element tempE1=document.createElement("Value");
                            Element tempE2=document.createElement("Value");

                            tempE1.setAttribute("Name", "Contents");
                            tempE1.setAttribute("Type", "int");
                            tempE1.setAttribute("Value", id.getText().toString());

                            tempE2.setAttribute("Name", "Count");
                            tempE2.setAttribute("Type", "int");
                            tempE2.setAttribute("Value", count.getText().toString());

                            tempE.setAttribute("Name", "Slot" + iil.getTag());
                            tempE.appendChild(tempE1);
                            tempE.appendChild(tempE2);

                            iil.slot = Integer.parseInt((String)iil.getTag());

                            U.inventoryElememts[iil.slot] = tempE;
                            elements.get("Player/InventorySlots").appendChild(tempE);
                        } catch (Exception e) {
                            U.t(self, e.getMessage());
                            return;
                        }
                    }

                    Element slotE = U.inventoryElememts[iil.slot];
                    Element countE = U.getElememtByTagNameAndAttr(slotE, "Value", "Name", "Count");
                    Element idE = U.getElememtByTagNameAndAttr(slotE, "Value", "Name", "Contents");

                    countE.setAttribute("Value", count.getText().toString());
                    idE.setAttribute("Value", id.getText().toString());

                    iil.setIdEx(Integer.parseInt(id.getText().toString()));
                    iil.setCount(Integer.parseInt(count.getText().toString()));

                    Toast.makeText(getApplication(), "Done", Toast.LENGTH_SHORT).show();
                    dialog.cancel();
                }
            });
        deleteBtn.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    ((Button)v).setText(R.string.delete_confirm);
                    v.setOnClickListener(new OnClickListener(){
                            @Override
                            public void onClick(View v) {
                                U.inventoryElememts[iil.slot].getParentNode().removeChild(U.inventoryElememts[iil.slot]);
                                iil.delete();
                                dialog.cancel();
                            }
                        });
                }
            });
    }
    public void pickItem(final EditText et) {
        LinearLayout v=(LinearLayout)LinearLayout.inflate(this, R.layout.dialog_item_picker, null);
        final EditText search=v.findViewById(R.id.searchInput);
        final ListView list=v.findViewById(R.id.list);

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
        v.findViewById(R.id.search).setOnClickListener(new OnClickListener(){

                @Override
                public void onClick(View v) {
                    pickerSearch(list, search.getText().toString());
                }
            });
        list.setOnItemClickListener(new OnItemClickListener(){

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    et.setText(((TextView)view.findViewById(R.id.id)).getText());
                    dialog.cancel();
                }
            });
        pickerSearch(list, "");
    }
    public LinkedList<ItemPickerItem> pickerSearch(ListView lv, String s) {
        LinkedList<ItemPickerItem> mData = new LinkedList<ItemPickerItem>();
        ItemPickerAdapter mAdapter;

        if (s.equals("")) {
            mData = allPickerItems;
        } else for (ItemPickerItem ipi : allPickerItems) {
                if (ipi.toString().indexOf(s) != -1) {
                    mData.add(ipi);
                }
            }
        mAdapter = new ItemPickerAdapter(mData, self);
        lv.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        return mData;
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
                            switch (getElementName(u1)) {
                                case "WorldName":
                                    // 保存世界名称
                                    u1.setAttribute("Value", worldName.getText().toString());
                                    break;
                                case "TimeOfYear":
                                    // 保存季节
                                    u1.setAttribute("Value", timeOfYear.getText().toString());
                                    break;
                                case "YearDays":
                                    // 保存一年长度
                                    if(yearDays.getText().toString().equals("")||yearDays.getText().toString().equals("0")){
                                        yearDays.setText("1");
                                    }
                                    u1.setAttribute("Value", yearDays.getText().toString());
                                    break;
                                case "GameMode":
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
                                    break;
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
                            // 保存玩家信息
                            switch (getElementName(u1)) {
                            case "Name":
                                // 保存玩家名称
                                u1.setAttribute("Value", playerName.getText().toString());
                                break;
                            case "Level":
                                // 保存玩家等级
                                u1.setAttribute("Value", playerLevel.getText().toString());
                                break;
                            case "PlayerClass":
                                // 保存玩家性别
                                u1.setAttribute("Value", genders[playerGender.getSelectedItemPosition()]);
                                break;
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
                                        e3.setAttribute("Value", playerPosX.getText() + "," + playerPosY.getText() + "," + playerPosZ.getText());
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
        ZipParameters zipParameters = new ZipParameters();
        try {
            String pathRegions = new File(pathProjectXml).getParentFile().getAbsolutePath() + "/Regions";

            DedroidFile.del(path + ".bak");
            DedroidFile.copy(path, path + ".bak");
            DedroidFile.del(path);

            ZipFile zip = new ZipFile(path);
            zip.addFile(new File(pathProjectXml), zipParameters);
            if (DedroidFile.exists(pathRegions)) {
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

    public void putOutFire(View v) {
        elements.get("Player/FireDuration").setAttribute("Value", "0");
        Toast.makeText(getApplication(), "Done", Toast.LENGTH_SHORT).show();
    }
    public void clearFlu(View v) {
        elements.get("Player/FluDuration").setAttribute("Value", "0");
        Toast.makeText(getApplication(), "Done", Toast.LENGTH_SHORT).show();
    }
    public void clearSick(View v) {
        elements.get("Player/SicknessDuration").setAttribute("Value", "0");
        Toast.makeText(getApplication(), "Done", Toast.LENGTH_SHORT).show();
    }
    public static String getElementName(Element e){
        return e.getAttribute("Name");
    }
    public static String getElementValue(Element e){
        return e.getAttribute("Value");
    }
}

package tc.scworldeditor;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.widget.ImageView;
import android.graphics.Bitmap;

public class InventoryItemLayout extends LinearLayout {

    public static final String TAG = "InventoryItemLayout";

    private TextView itemName;
    private TextView itemCount;
    private TextView itemId;
    private ImageView itemIcon;

    public String name;
    public int count=0;
    public int id=0;
    public int slot=0;

    public InventoryItemLayout(Context ctx) {
        super(ctx);
        init(null);
    }

    public InventoryItemLayout(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        init(attrs);
    }

    public InventoryItemLayout(Context ctx, AttributeSet attrs, int defStyleAttr) {
        super(ctx, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        inflate(getContext(), R.layout.inventory_item_layout, this);

        itemName = findViewById(R.id.itemName);
        itemCount = findViewById(R.id.itemCount);
        itemId = findViewById(R.id.itemId);
        itemIcon = findViewById(R.id.itemIcon);
    }
    public void setName(String name) {
        itemName.setText(name);
        this.name = name;
    }
    public void setCount(int count) {
        itemCount.setText("x" + count);
        this.count = count;
    }
    public void setIdEx(int id) {
        itemId.setText("ID " + id);
        this.id = id;
        try {
            JSONObject ja=new JSONObject(U.getStringFromAssets(getContext(), getContext().getString(R.string._path_items_display_name)));
            setName(ja.getString(id + ""));
        } catch (JSONException e) {
            setName("UNK ITEM");
        }
        try {
            JSONObject ja=new JSONObject(U.getStringFromAssets(getContext(), "items.json"));
            setIcon(U.getBitmapFromAssets(getContext(), "textures/" + ja.getString(id + "") + ".png"));
        } catch (JSONException e) {
            setIcon(null);
        }
    }
    public void setSlot(int slot) {
        this.slot = slot;
    }
    public void setIcon(Bitmap bm) {
        itemIcon.setImageBitmap(bm);
    }
    public void delete() {
        setName("");
        itemCount.setText("");
        itemId.setText("");
        setIcon(null);
        id = 0;
        count = 0;
    }
}

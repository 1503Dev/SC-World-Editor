package tc.scworldeditor;

import org.json.JSONArray;
import org.json.JSONException;
import android.content.Context;
import org.json.JSONObject;

public class ItemPickerItem {
    public String name="";
    public int idEx=0;
    public String nameSpaceName="";

    public ItemPickerItem(Context ctx, int id) {
        this.idEx = id;
        try {
            JSONObject ja=new JSONObject(U.getStringFromAssets(ctx, ctx.getString(R.string._path_items_display_name)));
            name = ja.getString(id + "");
        } catch (JSONException e) {
            name = "UNK ITEM";
        }
        try {
            JSONObject ja=new JSONObject(U.getStringFromAssets(ctx, "items.json"));
            nameSpaceName = ja.getString(id + "");
        } catch (JSONException e) {
            nameSpaceName = "unknown_item";
        }
    }

    @Override
    public String toString() {
        return idEx + "\n" + name + "\n" + nameSpaceName;
    }
}

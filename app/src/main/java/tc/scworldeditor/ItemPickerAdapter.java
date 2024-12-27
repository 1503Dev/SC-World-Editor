package tc.scworldeditor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.LinkedList;
import tc.scworldeditor.R;

public class ItemPickerAdapter extends BaseAdapter {

    private LinkedList<ItemPickerItem> mData;
    private Context mContext;

    public ItemPickerAdapter(LinkedList<ItemPickerItem> mData, Context mContext) {
        this.mData = mData;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(mContext).inflate(R.layout.item_list_items,parent,false);
        TextView txt_aName = convertView.findViewById(R.id.name);
        TextView txt_aSpeak = convertView.findViewById(R.id.id);
        txt_aName.setText(mData.get(position).name);
        txt_aSpeak.setText(mData.get(position).idEx+"");
        return convertView;
    }
}

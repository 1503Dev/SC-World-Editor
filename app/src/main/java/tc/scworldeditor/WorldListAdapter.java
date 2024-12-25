package tc.scworldeditor;

/**
 * Created by Jay on 2015/9/18 0018.
 */
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.LinkedList;
import tc.scworldeditor.R;

public class WorldListAdapter extends BaseAdapter {

    private LinkedList<WorldListItem> mData;
    private Context mContext;

    public WorldListAdapter(LinkedList<WorldListItem> mData, Context mContext) {
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
        convertView = LayoutInflater.from(mContext).inflate(R.layout.item_list_world,parent,false);
        TextView txt_aName = convertView.findViewById(R.id.worldName);
        TextView txt_aSpeak = convertView.findViewById(R.id.worldFileName);
        txt_aName.setText(mData.get(position).getName());
        txt_aSpeak.setText(mData.get(position).getFileName());
        return convertView;
    }
}

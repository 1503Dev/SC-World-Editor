package tc.crashlogger;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.graphics.Typeface;
import android.widget.ScrollView;

public class View extends Activity {
    
    public static final String TAG = "View";
    
    private String localizedMessage;
    private String message;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("运行异常");
        
        localizedMessage=getIntent().getStringExtra("LocalizedMessage");
        message=getIntent().getStringExtra("Message");
        
        ScrollView sv=new ScrollView(this);
        
        TextView tv=new TextView(this);
        tv.setTypeface(Typeface.MONOSPACE);
        tv.setPadding(8,8,8,8);
        tv.setText(localizedMessage+"\n\n"+message);
        
        sv.addView(tv);
        
        setContentView(sv);
    }
    
}

package jp.pinetail.android.drugstore_map;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public class DetailTabActivity extends TabActivity {

	private Integer index = null;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_tab);
        
        Resources res = getResources();
        
        Bundle extras=getIntent().getExtras();
        if (extras != null) {
            index = extras.getInt("rowid");
        }
        
        // 戻るボタン
        Button backButton = (Button) findViewById(R.id.btn_back);
        backButton.setOnClickListener(new OnClickListener() {
 
            public void onClick(View v) {
                finish();
            }
        });
        
        TabHost tabHost = getTabHost();
        
        TabHost.TabSpec spec;
        
        Intent intent;
/*
        // 履歴
        intent = new Intent().setClass(this, DetailActivity.class);
        intent.putExtra("rowid", index);
        spec = tabHost.newTabSpec("tab1")
                      .setIndicator(new CustomTabContentView(this, "店舗詳細", R.drawable.icon_01))
//                      .setIndicator(res.getString(R.string.label_graph), res.getDrawable(R.drawable.chart))
                      .setContent(intent);
        tabHost.addTab(spec);
*/
        
        // チャート
        intent = new Intent(); 
        intent.setAction(Intent.ACTION_VIEW); 
        intent.setData(Uri.parse("http://www.pinetail.jp/"));
        
        spec = tabHost.newTabSpec("tab2")
                      .setIndicator(new CustomTabContentView(this, "ブラウザ", R.drawable.globe))
//                      .setIndicator(res.getString(R.string.label_graph), res.getDrawable(R.drawable.chart))
                      .setContent(intent);
        tabHost.addTab(spec);
        /*
        // 給油マップ
        intent = new Intent().setClass(this, FuelMapActivity.class);
        spec = tabHost.newTabSpec("tab3")
                      .setIndicator(res.getString(R.string.label_fuel_map), res.getDrawable(R.drawable.globe))
                      .setContent(intent);
        tabHost.addTab(spec);
        */
        if (extras != null && extras.containsKey("currentTab")) {
            tabHost.setCurrentTab(extras.getInt("currentTab"));
        } else {
            tabHost.setCurrentTab(0);
        }
        
    }
    
    public class CustomTabContentView extends FrameLayout {
        LayoutInflater mInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        final static int NONE   = 0;
        final static int CENTER = 1;
        final static int LEFT   = 2;
        final static int RIGHT  = 3;
        
        public CustomTabContentView(Context context, String title, int icon) {
        	this(context, title, icon, CENTER);
        }
        
        public CustomTabContentView(Context context, String title, int icon, int position) {
        	super(context);
        	View view = mInflater.inflate(R.layout.custom_tabwidget, this);
        	((TextView) view.findViewById(R.id.textview)).setText(title);
        	((ImageView) view.findViewById(R.id.imageview)).setImageResource(icon);
        	
        	switch(position) {
        		case LEFT:
        			setPadding(0, 0, 2, 0);
        			break;
        		case RIGHT:
        			setPadding(2, 0, 0, 0);
        			break;
        		case CENTER:
        			setPadding(2, 0, 2, 0);
        			break;
    			default:
    				break;
        	}
        	
        }
        
    }
}

package jp.pinetail.android.drugstore_map;

import java.util.ArrayList;

import jp.pinetail.android.drugstore_map.core.AbstractGgmapActivity;
import jp.pinetail.android.drugstore_map.libs.DatabaseHelper;
import jp.pinetail.android.drugstore_map.libs.ShopsAdapter;
import jp.pinetail.android.drugstore_map.libs.ShopsDao;
import yanzm.products.quickaction.lib.ActionItem;
import yanzm.products.quickaction.lib.QuickAction;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ListActivity extends AbstractGgmapActivity {
    private ArrayList<Shops> list = null;
    private ShopsAdapter adapter = null;
    private DatabaseHelper dbHelper = null;
    private SQLiteDatabase db = null;
    private ShopsDao shopsDao = null;
    private static String mode = "none";
    private SharedPreferences pref = null;
    private QuickAction qa;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        
        // 初期化
        pref = PreferenceManager.getDefaultSharedPreferences(ListActivity.this);
//        mode = pref.getString("settings_sort", getResources().getString(R.string.settings_sort_default));
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();
//        Utils.logging(mode);
        shopsDao = new ShopsDao(db, ListActivity.this);
        Bundle extras=getIntent().getExtras();
        if (extras != null) {
            int top    = extras.getInt("top");
            int bottom = extras.getInt("bottom");
            int left   = extras.getInt("left");
            int right  = extras.getInt("right");
            list = shopsDao.find(pref, top, bottom, left, right);
        } else {
            list = shopsDao.findAll();
        }
        db.close();
        init();

        /*
        Spinner spinSort = (Spinner) findViewById(R.id.spin_sort);
        
        if (mode.equals("distance")) {
            spinSort.setSelection(0);
        } else if (mode.equals("price")) {
            spinSort.setSelection(1);
        }else  if (mode.equals("date")) {
            spinSort.setSelection(2);
        }
        spinSort.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                
                switch (position) {
                case 0:
                    mode = "dist";
                    break;
                case 1:
                    mode = "price";
                    break;
                case 2:
                    mode = "date";
                    break;
                }
                
                db = dbHelper.getWritableDatabase();
                shopsDao = new ShopsDao(db);
                list = shopsDao.findAll();
                db.close();
                init();
                
                // イベントトラック（並び順）
                tracker.trackEvent(
                    "List",       // Category
                    "Sort",       // Action
                    mode,         // Label
                    0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        });
        /*
        RadioButton priceButton = (RadioButton) findViewById(R.id.sort_price);
        
        priceButton.setOnClickListener(new OnClickListener() {
 
            @Override
            public void onClick(View v) {
                db = dbHelper.getWritableDatabase();
                shopsDao = new ShopsDao(db);
                mode = "price";
                list = shopsDao.findAll(mode);
                db.close();
                init();
                
                // イベントトラック（並び順）
                tracker.trackEvent(
                    "List",       // Category
                    "Sort",       // Action
                    "price",      // Label
                    0);
            }
        });

        RadioButton distanceButton = (RadioButton) findViewById(R.id.sort_distance);
        
        distanceButton.setOnClickListener(new OnClickListener() {
 
            @Override
            public void onClick(View v) {
                db = dbHelper.getWritableDatabase();
                shopsDao = new ShopsDao(db);
                mode = "dist";
                list = shopsDao.findAll(mode);
                db.close();
                init();
                
                // イベントトラック（並び順）
                tracker.trackEvent(
                    "List",       // Category
                    "Sort",       // Action
                    "dist",       // Label
                    0);

            }
        });
        
        if (mode.equals("price")) {
            priceButton.setChecked(true);
        } else {
            distanceButton.setChecked(true);
        }
        */
        
        Button backButton = (Button) findViewById(R.id.btn_back);
        backButton.setOnClickListener(new OnClickListener() {
 
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    
    protected void init() {
    	super.init();
    	
        if (list.size() > 0) {
            ListView savedList = (ListView) findViewById(R.id.savedList);
            savedList.setFastScrollEnabled(true);
            adapter = new ShopsAdapter(this, R.layout.list, list);
            savedList.setAdapter(adapter);  
        
            savedList.setOnItemClickListener(new OnItemClickListener() {
     
                @Override
                public void onItemClick(AdapterView<?> adapter,
                        View view, int position, long id) {
                    final Shops item = list.get(position);
                    
                    ActionItem item1 = new ActionItem();
                    item1.setTitle("地図");
                    item1.setIcon(getResources().getDrawable(R.drawable.map_blue));
                    item1.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            
                            // イベントトラック（地図）
                            tracker.trackEvent(
                                "List",         // Category
                                "Map",          // Action
                                item.Uid,  // Label
                                0);
                            
                            Intent intent = new Intent();
                            intent.putExtra("lat", item.Lat);
                            intent.putExtra("lon", item.Lng);
                            
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        }
                    });
                    
                    ActionItem item2 = new ActionItem();
                    item2.setTitle("詳細");
                    item2.setIcon(getResources().getDrawable(R.drawable.info));
                    item2.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            
                            // イベントトラック（詳細）
                            tracker.trackEvent(
                                "List",         // Category
                                "Detail",          // Action
                                item.Uid,  // Label
                                0);
                            
                            Intent intent1 = new Intent(ListActivity.this, DetailActivity.class);
                            intent1.putExtra("rowid", item.Rowid);
                            startActivity(intent1);
                        }
                    });
                    
                    
                    ActionItem item3 = new ActionItem();
                    item3.setTitle("ルート検索");
                    item3.setIcon(getResources().getDrawable(R.drawable.green_flag));
                    item3.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            
                            // イベントトラック（ルート検索）
                            tracker.trackEvent(
                                "List",         // Category
                                "RouteSearch",  // Action
                                item.Uid,  // Label
                                0);
                            
                            Intent intent = new Intent(); 
                            intent.setAction(Intent.ACTION_VIEW); 
                            intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
                            intent.setData(Uri.parse("http://maps.google.com/maps?myl=saddr&daddr=" + item.Address.replaceAll("<br />", " ") + "&dirflg=d")); 
                            startActivity(intent);
                        }
                    });
                    

                    qa = new QuickAction(view);
                    //onCreate()の中で作ったActionItemをセットする
                    qa.addActionItem(item1);
                    qa.addActionItem(item2);
                    qa.addActionItem(item3);
                    
                    //アニメーションを設定する
                    qa.setAnimStyle(QuickAction.ANIM_AUTO);
                    qa.show();
                }
            });
        }    
    }
    
}

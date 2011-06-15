package jp.pinetail.android.drugstore_map;

import jp.pinetail.android.drugstore_map.core.AbstractGgmapActivity;
import jp.pinetail.android.drugstore_map.libs.DatabaseHelper;
import jp.pinetail.android.drugstore_map.libs.ShopsController;
import jp.pinetail.android.drugstore_map.libs.ShopsDao;
import jp.pinetail.android.drugstore_map.libs.Util;
import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class DetailActivity extends AbstractGgmapActivity implements Runnable {
    
    private DatabaseHelper dbHelper = null;
    private SQLiteDatabase db = null;
    private ShopsDao shopsDao = null;
    private ShopsController controller = null;
    private final Handler handler = new Handler();
    private static final Integer pressed_color = Color.argb(80, 255, 255, 255);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.detail);
        
        init();

        dbHelper = new DatabaseHelper(this);
        
        Bundle extras=getIntent().getExtras();
        if (extras != null) {
            Integer index = extras.getInt("rowid");
            Util.logging(index.toString());
            db = dbHelper.getReadableDatabase();
            shopsDao = new ShopsDao(db, DetailActivity.this);
            final Shops info = shopsDao.findById(index);
            db.close();
            
            if (info == null) {
                Toast.makeText(this, "情報が取得できません", Toast.LENGTH_SHORT).show();
                finish();
            }
            
            controller = new ShopsController(handler, (Runnable) this, this, info);
            controller.setLayout();
            
            setContentView(controller.getView());
            
            // 戻るボタン
            Button backButton = (Button) findViewById(R.id.btn_back);
            backButton.setOnClickListener(new OnClickListener() {
     
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            });

            LinearLayout route = (LinearLayout) findViewById(R.id.layout_route);
            route.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                        v.setBackgroundColor(pressed_color);
                    } else if(event.getAction() == MotionEvent.ACTION_UP) {
                        v.setBackgroundColor(Color.TRANSPARENT);
                        
                        // イベントトラック（ルート検索）
                        tracker.trackEvent(
                            "Detail",      // Category
                            "RouteSearch", // Action
                            info.Uid, // Label
                            0);
                        
                        Intent intent = new Intent(); 
                        intent.setAction(Intent.ACTION_VIEW); 
                        intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
                        intent.setData(Uri.parse("http://maps.google.com/maps?myl=saddr&daddr=" + info.Address.replaceAll("<br />", " ") + "&dirflg=d")); 
                        startActivity(intent);
                    }
                    return true;
                }
            });

            // ブラウザ
            LinearLayout browser = (LinearLayout) findViewById(R.id.layout_browser);
            browser.setOnTouchListener(new View.OnTouchListener()    {
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                        v.setBackgroundColor(pressed_color);
                    } else if(event.getAction() == MotionEvent.ACTION_UP) {
                        v.setBackgroundColor(Color.TRANSPARENT);
                        
                        // イベントトラック（ブラウザ）
                        tracker.trackEvent(
                            "Detail",      // Category
                            "Browser",     // Action
                            info.Uid, // Label
                            0);
                        
                        Intent intent = new Intent(); 
                        intent.setAction(Intent.ACTION_VIEW); 
                        intent.setData(Uri.parse(info.PcUrl)); 
                        startActivity(intent);
                    }
                    return true;
                }    
            });
        }
        
    }
    
    @Override
    public void run() {
    }
    
    
}

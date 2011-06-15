package jp.pinetail.android.drugstore_map;

import java.util.ArrayList;
import java.util.List;

import jp.pinetail.android.drugstore_map.core.AbstractCoreMapActivity;
import jp.pinetail.android.drugstore_map.libs.CategoryImage;
import jp.pinetail.android.drugstore_map.libs.DatabaseHelper;
import jp.pinetail.android.drugstore_map.libs.ShopsDao;
import jp.pinetail.android.drugstore_map.libs.ShopsUpdateHelper;
import jp.pinetail.android.drugstore_map.libs.Util;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ZoomButtonsController.OnZoomListener;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class MainActivity extends AbstractCoreMapActivity {
    
    static Object lock = new Object();
    private static final int E6 = 1000000;
    private MapController mMapController = null;
    private MapView mMapView = null;
    private LocationOverlay overlay;
    private DatabaseHelper dbHelper = null;
    private SQLiteDatabase db = null;
    private ShopsDao shopsDao = null;
    private Shops point_data;
    private PinItemizedOverlay pinOverlay = null;
    private GestureDetector gestureDetector;
    public ShopsUpdateHelper helper;
    private static Thread thread = null;
    private int top;
    private int bottom;
    private int left;
    private int right;
    private CategoryImage categoryImage;
    private Handler mHandler = new Handler();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
//        requestWindowFeature(Window.FEATURE_PROGRESS);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setProgressBarIndeterminate(false);
        
        init();
        
        gestureDetector = new GestureDetector(MainActivity.this, simpleOnGestureListener);

        mMapView = (MapView) findViewById(R.id.main_map);
        mMapView.setBuiltInZoomControls(true);
        mMapView.getZoomButtonsController().setOnZoomListener(new OnZoomListener() {
            @Override
            public void onZoom(boolean zoomIn) {
                if(zoomIn){
                    mMapController.zoomIn();
                } else {
                    mMapController.zoomOut();
                }
                
                if (mMapView.getZoomLevel() < Integer.parseInt(getResources().getString(R.string.def_zoom_level))) {
                    Toast.makeText(MainActivity.this, "地図を拡大してください", Toast.LENGTH_SHORT).show();
                }

                drawShops();
            }

            @Override
            public void onVisibilityChanged(boolean visible) {
            }
        });

        mMapController = mMapView.getController();

        
        // 今回の主役。有効にすることでGPSの取得が可能に
        overlay = new LocationOverlay(getApplicationContext(), mMapView);
        overlay.enableMyLocation();
        overlay.enableCompass();

        // GPS取得が可能な状態になり、GPS初取得時の動作を決定（らしい）
        overlay.runOnFirstFix(new Runnable(){
            public void run() {

                // animateTo(GeoPoint)で指定GeoPoint位置に移動
                // この場合、画面中央がGPS取得による現在位置になる
//                mMapView.getController().animateTo(overlay.getMyLocation());
                overlay.setMyLocation(overlay.getLastFix());
            }
        });
        
        categoryImage = new CategoryImage(MainActivity.this);
        
        dbHelper = new DatabaseHelper(MainActivity.this);
        
        ImageButton btnList = (ImageButton) findViewById(R.id.btn_list);
        btnList.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListActivity.class);
                intent.putExtra("top",    top);
                intent.putExtra("bottom", bottom);
                intent.putExtra("left",   left);
                intent.putExtra("right",  right);
                startActivityForResult(intent, 2);
            }
        });

        start();
    }
    
    private void start() {
        
        new Thread(new Runnable() {
            public void run() {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                final boolean status = dbHelper.getImportStatus();
                db.close();
                
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                mHandler.post(new Runnable() {
                    public void run() {
                        
                        mMapView.invalidate();
                        
                        if (status == false) {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                            alertDialogBuilder.setMessage("データの登録に失敗しました。\n管理者までお問い合わせください。");
                            
                            // アラートダイアログの否定ボタンがクリックされた時に呼び出されるコールバックを登録します
                            alertDialogBuilder.setNegativeButton("閉じる", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    
                                }});
                            
                            // アラートダイアログのキャンセルが可能かどうかを設定します
                            alertDialogBuilder.setCancelable(true);
                            alertDialogBuilder.show();
                        }
                        
                        drawShops();
                    }
                });

            }
        }).start();
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        
        switch(event.getAction()) {
        case MotionEvent.ACTION_UP:
            if (lastEvent == "onFling" || lastEvent == "onScroll") {
                drawShops();
            }
            break;
        }
        return super.dispatchTouchEvent(event);
    }

    
    public void drawShops() {

        setProgressBarIndeterminateVisibility(true);

        Thread thread = new Thread() {
            public void run() {
                final ArrayList<PinItemizedOverlay> pins = process();
                
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setProgressBarIndeterminateVisibility(false);

                        mMapView.getOverlays().clear();

                        // Overlayとして登録
                        mMapView.getOverlays().add(overlay);
                        
                        if (mMapView.getZoomLevel() < Integer.parseInt(getResources().getString(R.string.def_zoom_level))) {
                            return;
                        }
                        mMapView.getOverlays().addAll(pins);
                        mMapView.invalidate();
                    }
                });
            }
        };
        thread.start();
        

    }
    
    synchronized private final ArrayList<PinItemizedOverlay> process() {
        ArrayList<PinItemizedOverlay> pins = new ArrayList<PinItemizedOverlay>();;

        try {
            db = dbHelper.getReadableDatabase();
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            PinItemizedOverlay pinOverlay = null;
            
            GeoPoint center = mMapView.getMapCenter();
            top    = center.getLatitudeE6()  - mMapView.getLatitudeSpan()/2;
            bottom = center.getLatitudeE6()  + mMapView.getLatitudeSpan()/2;
            left   = center.getLongitudeE6() - mMapView.getLongitudeSpan()/2;
            right  = center.getLongitudeE6() + mMapView.getLongitudeSpan()/2;
            
            ShopsDao shopsDao = new ShopsDao(db, MainActivity.this);
            ArrayList<Shops> shops = shopsDao.find(pref, top, bottom, left, right);
            Util.logging(String.valueOf(shops.size()));
            if (shops.size() > 0) {
                int i = 0;
                for (Shops shop : shops) {
                    GeoPoint geoPoint = new GeoPoint((int) (shop.Lat * E6), (int) (shop.Lng * E6));
                    
                    pinOverlay = new PinItemizedOverlay(categoryImage.getDrawable(shop.Category));
                    pinOverlay.addPoint(geoPoint);
                    pinOverlay.setShop(shop);
                    pins.add(pinOverlay);
                    i++;
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db.isOpen()) {
                db.close();
            }
        }
        return pins;
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        super.onCreateOptionsMenu( menu );
        // メニューアイテムを追加
        MenuItem item0 = menu.add( 0, 0, 0, "現在地" );
        MenuItem item1 = menu.add( 0, 1, 0, "設定" );
        MenuItem item2 = menu.add( 0, 2, 0, "更新チェック" );

        // 追加したメニューアイテムのアイコンを設定
        item0.setIcon( android.R.drawable.ic_menu_mylocation);
        item1.setIcon( android.R.drawable.ic_menu_preferences );
        item2.setIcon( android.R.drawable.ic_menu_rotate );
        return true;
    }
    
    @Override  
    public boolean onOptionsItemSelected(MenuItem item){

        switch(item.getItemId()){  
        case 0:  
            GeoPoint l = overlay.getMyLocation();
            
            if (l == null) {
                Toast.makeText(this, "現在地を特定できません", Toast.LENGTH_LONG).show();
            } else {
                // 取得した位置をマップの中心に設定
                mMapController.animateTo(l, new Runnable() {
                    
                    @Override
                    public void run() {
                        drawShops();
                    }
                });
            }
            return true;
        case 1:
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivityForResult(intent, 1);
            return true;
        case 2:
            checkUpdate(); 
        }  
        return false;
    }
    
    public void checkUpdate() {
        final ProgressDialog mProgressDialog;

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("通信中");
        mProgressDialog.setMessage("しばらくお待ちください。");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.show();

        Thread thread = new Thread() {
            public void run() {
                SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                helper = new ShopsUpdateHelper(MainActivity.this, pref);
                final boolean res = helper.checkUpdate();
                
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                        
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                        alertDialogBuilder.setTitle("更新チェック");
                        alertDialogBuilder.setMessage(helper.msg);
                        
                        if (res == true) {
                               // アラートダイアログの中立ボタンがクリックされた時に呼び出されるコールバックを登録します
                            alertDialogBuilder.setPositiveButton("更新する", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    updateRecords(helper);
                                }});
                        }

                        // アラートダイアログの否定ボタンがクリックされた時に呼び出されるコールバックを登録します
                        alertDialogBuilder.setNegativeButton("閉じる", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                
                            }});
                        
                        // アラートダイアログのキャンセルが可能かどうかを設定します
                        alertDialogBuilder.setCancelable(true);
                        alertDialogBuilder.show();
                    }
                });
            }
        };
        thread.start();

    }
    
    public boolean updateRecords(final ShopsUpdateHelper helper) {
        final ProgressDialog mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("データ更新中");
        mProgressDialog.setMessage("しばらくお待ちください。");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMax(100);
        mProgressDialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                thread.interrupt();
                thread = null;
            }
            
        });
        mProgressDialog.show();

        thread = new Thread() {
            
            public void run() {
                String tmp_msg;
                synchronized (lock) {
                    if (helper.downloadUpdateCsv(mProgressDialog) == true) {
                        
                        db = dbHelper.getWritableDatabase();
                        Util.logging(helper.getShopFileName());
                        if (isInterrupted() == false) {
                            if (dbHelper.updateShops(db, helper.getShopFileName(), mProgressDialog) == true) {
                                SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                                Editor editor = pref.edit();
                                Util.logging(helper.latest_date);
                
                                editor.putString("latest_date", helper.latest_date);
                                editor.commit();
                                tmp_msg = "データが更新されました。";
                                helper.removeFile(helper.getShopFileName());
                                helper.removeFile(helper.getShopZipFileName());
                            } else {
                                tmp_msg = "データの更新に失敗しました。";
                            }
                        } else {
                            tmp_msg = null;
                        }
                    } else {
                        tmp_msg = helper.msg;
                    }
                    
                    final String msg = tmp_msg;
    
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.dismiss();
                            if (msg != null) {
                                final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                                    .setMessage(msg)
                                    .setNegativeButton("閉じる", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {}
                                    }).show();
                            }
                        }
                    });
                }
            }
        };
        thread.start();

        return true;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case 1:
                this.drawShops();
                break;
            case 2:
                if (resultCode == RESULT_OK) {
                    Bundle extras = intent.getExtras();
                    if (extras != null && extras.containsKey("lat")) {
                        Double lan = extras.getDouble("lat");
                        Double lon = extras.getDouble("lon");
                        
                        GeoPoint point = new GeoPoint(
                                (int) ((double) lan * E6),
                                (int) ((double) lon * E6));
                        mMapController.animateTo(point);
                    }
                }
                break;
        }
    }
    
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
    
    @Override
    protected void onResume() {
        overlay.enableMyLocation();
        
        super.onResume();
        
        if(PREFERENCE_INIT == getState()) {
            checkUpdate();
            setState(PREFERENCE_BOOTED);
        }
    }
    
    @Override
    protected void onPause() {
        overlay.disableMyLocation();

        super.onPause();
    }
    
    public class PinItemizedOverlay extends ItemizedOverlay<PinOverlayItem> implements Runnable {

        private List<GeoPoint> points = new ArrayList<GeoPoint>();
        private List<Shops> shops = new ArrayList<Shops>();
        private LayoutInflater inflater;

        public PinItemizedOverlay(Drawable defaultMarker) {
            super( boundCenterBottom(defaultMarker) );
        }

        @Override
        protected PinOverlayItem createItem(int i) {
            GeoPoint point = points.get(i);
            return new PinOverlayItem(point);
        }

        @Override
        public int size() {
            return points.size();
        }

        public void addPoint(GeoPoint point) {
            points.add(point);
            populate();
        }
        
        public void clearPoint() {
            points.clear();
            populate();
        }
                        
        public void setShop(Shops point) {
            this.shops.add(point);
        }
        
        /**
         * アイテムがタップされた時の処理
         */
        @Override
        protected boolean onTap(int index) {
            point_data = shops.get(index);
            
            Intent intent1 = new Intent(MainActivity.this, DetailActivity.class);
            intent1.putExtra("rowid", point_data.Rowid);
            startActivityForResult(intent1, 1);
            return true;

        }
        
        @Override
        public void run() {
            /*
            //プログレスダイアログを閉じる
            dialog.dismiss();
            */
        }
        
        @Override
        public void draw(Canvas canvas, MapView mapView, boolean shadow) {
            if (shadow) {
                return;
            }

            super.draw(canvas, mapView, shadow);
            
            return;
        }
    }
    
    public class PinOverlayItem extends OverlayItem {

        public PinOverlayItem(GeoPoint point){
            super(point, "", "");
        }
    }
}
package jp.pinetail.android.drugstore_map.libs;

import org.apache.commons.lang.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jp.pinetail.android.drugstore_map.R;
import jp.pinetail.android.drugstore_map.Shops;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ShopsDao {
	
    private static final int E6 = 1000000;
	private static final String TABLE_NAME                = "shops";
	private static final String COLUMN_ID                 = "rowid";
	private static final String COLUMN_UID                = "uid";
	private static final String COLUMN_CATEGORY           = "category";
	private static final String COLUMN_NAME               = "name";
	private static final String COLUMN_ADDRESS            = "address";
	private static final String COLUMN_TEL                = "tel";
	private static final String COLUMN_ACCESS             = "access";
	private static final String COLUMN_BUSINESS_HOURS     = "business_hours";
	private static final String COLUMN_HOLIDAY            = "holiday";
	private static final String COLUMN_LAT                = "lat";
	private static final String COLUMN_LNG                = "lng";
	private static final String COLUMN_PC_URL             = "pc_url";
	private static final String COLUMN_MOBILE_URL         = "mobile_url";
	private static final String COLUMN_COLUMN01           = "column01";
	private static final String COLUMN_COLUMN02           = "column02";
	private static final String COLUMN_COLUMN03           = "column03";
	private static final String COLUMN_COLUMN04           = "column04";
	private static final String COLUMN_COLUMN05           = "column05";
	private static final String COLUMN_USE_FLG            = "use_flg";
	private static final String COLUMN_CREATED_AT         = "created_at";
	private static final String COLUMN_UPDATED_AT         = "updated_at";
    private static String[] CATEGORY_KEYS       = {};
    private static String[] CATEGORY_VALUES     = {};

    private static final String[] COLUMNS = 
         {COLUMN_ID, COLUMN_UID, COLUMN_CATEGORY, COLUMN_NAME, COLUMN_ADDRESS, COLUMN_TEL, COLUMN_ACCESS, COLUMN_BUSINESS_HOURS, COLUMN_HOLIDAY, 
          COLUMN_LAT, COLUMN_LNG, COLUMN_PC_URL, COLUMN_MOBILE_URL, COLUMN_COLUMN01, COLUMN_COLUMN02, COLUMN_COLUMN03, COLUMN_COLUMN04, COLUMN_COLUMN05,
          COLUMN_USE_FLG, COLUMN_CREATED_AT, COLUMN_UPDATED_AT};

    private SQLiteDatabase db;
    private HashMap<String, String> category_map = new HashMap <String, String>();

    
    public ShopsDao(SQLiteDatabase db, Context context) {
        this.db = db;
        
        CATEGORY_KEYS = context.getResources().getStringArray(R.array.list_category_key);
        CATEGORY_VALUES = context.getResources().getStringArray(R.array.list_category_name);
        
        for ( int i = 0; i < CATEGORY_KEYS.length; ++i ) {
            category_map.put(CATEGORY_KEYS[i], CATEGORY_KEYS[i]);
        }
    
    }
    
    public ArrayList<Shops> findAll() {
        ArrayList<Shops> shops = new ArrayList<Shops>();
                
        Cursor cursor = db.query(TABLE_NAME, COLUMNS, null, null, null, null, COLUMN_ID);
        
        while(cursor.moveToNext()) {
            Shops info = new Shops();
            
            info.Rowid            = cursor.getInt(0);
            info.Uid              = cursor.getString(1);
            info.Category         = cursor.getString(2);
            info.Name             = cursor.getString(3);
            info.Address          = cursor.getString(4);
            info.Tel              = cursor.getString(5);
            info.Access           = cursor.getString(6);
            info.BusinessHours    = cursor.getString(7);
            info.Holiday          = cursor.getString(8);
            info.Lat              = Double.valueOf(cursor.getString(9));
            info.Lng              = Double.valueOf(cursor.getString(10));
            info.PcUrl            = cursor.getString(11);
            info.MobileUrl        = cursor.getString(12);
            info.Column01         = cursor.getString(13);
            info.Column02         = cursor.getString(14);
            info.Column03         = cursor.getString(15);
            info.Column04         = cursor.getString(16);
            info.Column05         = cursor.getString(17);
            info.UseFlg           = cursor.getString(18);
            info.CreatedAt        = cursor.getString(19);
            info.UpdatedAt        = cursor.getString(20);
            
            shops.add(info);
            
        }
        
        return shops;
    }

    public ArrayList<Shops> find(SharedPreferences pref, int top, int bottom, int left, int right) {
        ArrayList<Shops> shops = new ArrayList<Shops>();
        
        List<String> conditions_category = new ArrayList<String>();
        List<String> conditions_wireless = new ArrayList<String>();
        
        for ( String key : category_map.keySet() ) {
            String data = category_map.get( key );
            
            if (pref.getBoolean(key, true) == true) {
                conditions_category.add("category like '%" + data + "%'");
            }
        }

        String conditions = "";
        conditions += "(" + (float)top / E6 + " <= lat and lat <=" + (float)bottom / E6 + ") and ";
        conditions += "(" + (float)left / E6 + " <= lng and lng <=" + (float)right / E6 + ") and ";
        
        if (conditions_category.size() > 0 && CATEGORY_KEYS.length != conditions_category.size()) {
            conditions += "(" + StringUtils.join(conditions_category, " or ") + ") and ";
        }
        conditions += "1 = 1";
        Util.logging("i:" + conditions);
        
        Cursor cursor = db.query(TABLE_NAME, COLUMNS, conditions, null, null, null, COLUMN_NAME);
//        Cursor cursor = db.query(TABLE_NAME, COLUMNS, null, null, null, null, COLUMN_ID);

        while(cursor.moveToNext()) {
            Shops info = new Shops();
            
            info.Rowid            = cursor.getInt(0);
            info.Uid              = cursor.getString(1);
            info.Category         = cursor.getString(2);
            info.Name             = cursor.getString(3);
            info.Address          = cursor.getString(4);
            info.Tel              = cursor.getString(5);
            info.Access           = cursor.getString(6);
            info.BusinessHours    = cursor.getString(7);
            info.Holiday          = cursor.getString(8);
            info.Lat              = Double.valueOf(cursor.getString(9));
            info.Lng              = Double.valueOf(cursor.getString(10));
            info.PcUrl            = cursor.getString(11);
            info.MobileUrl        = cursor.getString(12);
            info.Column01         = cursor.getString(13);
            info.Column02         = cursor.getString(14);
            info.Column03         = cursor.getString(15);
            info.Column04         = cursor.getString(16);
            info.Column05         = cursor.getString(17);
            info.UseFlg           = cursor.getString(18);
            info.CreatedAt        = cursor.getString(19);
            info.UpdatedAt        = cursor.getString(20);
            
            Util.logging(info.Lat.toString());
            Util.logging(info.Lng.toString());
            
            shops.add(info);
        }
        cursor.close();
        
        return shops;
    }

    public int deleteAll() {
        return db.delete(TABLE_NAME, null, null);
    }

    public Shops findById(int rowid) {
        Shops shop = new Shops();
        
        String conditions = "rowid = " + rowid;
        Util.logging("i:" + conditions);
        
        Cursor cursor = db.query(TABLE_NAME, COLUMNS, conditions, null, null, null, COLUMN_ID);

        while(cursor.moveToNext()) {
            Shops info = new Shops();
            
            info.Rowid            = cursor.getInt(0);
            info.Uid              = cursor.getString(1);
            info.Category         = cursor.getString(2);
            info.Name             = cursor.getString(3);
            info.Address          = cursor.getString(4);
            info.Tel              = cursor.getString(5);
            info.Access           = cursor.getString(6);
            info.BusinessHours    = cursor.getString(7);
            info.Holiday          = cursor.getString(8);
            info.Lat              = Double.valueOf(cursor.getString(9));
            info.Lng              = Double.valueOf(cursor.getString(10));
            info.PcUrl            = cursor.getString(11);
            info.MobileUrl        = cursor.getString(12);
            info.Column01         = cursor.getString(13);
            info.Column02         = cursor.getString(14);
            info.Column03         = cursor.getString(15);
            info.Column04         = cursor.getString(16);
            info.Column05         = cursor.getString(17);
            info.UseFlg           = cursor.getString(18);
            info.CreatedAt        = cursor.getString(19);
            info.UpdatedAt        = cursor.getString(20);
            
            return info;
        }
        
        return shop;
    }

}

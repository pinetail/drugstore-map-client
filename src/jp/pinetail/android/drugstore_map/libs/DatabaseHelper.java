package jp.pinetail.android.drugstore_map.libs;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import au.com.bytecode.opencsv.CSVReader;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "drugstore_map";
    private static final int DATABASE_VERSION = 1;
    private Context mContext;
    private static boolean import_status = true;
    
    private static final String CREATE_SHOPS_TABLE_SQL = "create table shops " +
            "(rowid integer primary key autoincrement, " +
            "uid text not null, " +
            "category text not null, " +
            "name text not null, " +
            "address text not null, " +
            "tel text null, " +
            "access text null, " +
            "business_hours text null, " +
            "holiday text null, " +
            "lat text not null, " +
            "lng text not null, " +
            "pc_url text null, " +
            "mobile_url text null, " +
            "column01 text null," +
            "column02 text null," +
            "column03 text null," +
            "column04 text null," +
            "column05 text null," +
            "use_flg text null," +
            "created_at text null," +
            "updated_at text null)";
    
    private static final String DROP_SHOPS_TABLE_SQL = "drop table if exists shops";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SHOPS_TABLE_SQL);
        
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_SHOPS_TABLE_SQL);
        onCreate(db);
    }
    
    public boolean updateShops(SQLiteDatabase db, String fname, ProgressDialog dialog) {
        
        CSVReader reader = null;
        db.beginTransaction();
        db.execSQL(DROP_SHOPS_TABLE_SQL);
        db.execSQL(CREATE_SHOPS_TABLE_SQL);
        SQLiteStatement stmt = db.compileStatement("insert into shops (uid, category, name, address, tel, access, "+ 
                "business_hours, holiday, lat, lng, pc_url, mobile_url, column01, column02, column03, column04, column05, " +
                "use_flg, created_at, updated_at) " +
                " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?);");

        try {
            InputStream input=new FileInputStream(fname);
            InputStreamReader ireader=new InputStreamReader(input, "SJIS");
            reader = new CSVReader(ireader,',','"');
            int size = Util.getRowNum(fname);
            Util.logging(String.valueOf(size));
            int num = 0;
            String[] nextLine;
            
            while ((nextLine = reader.readNext()) != null) {
                if (nextLine.length < 20) {
                    continue;
                }
                
                Util.logging("lat:"+ nextLine[8].trim() + ",lng:" + nextLine[9].trim());
                stmt.bindString(1, nextLine[0].trim()); // uid
                stmt.bindString(2, nextLine[1].trim()); // category
                stmt.bindString(3, nextLine[2].trim()); // name
                stmt.bindString(4, nextLine[3].trim()); // address
                stmt.bindString(5, nextLine[4].trim()); // tel
                stmt.bindString(6, nextLine[5].trim()); // access
                stmt.bindString(7, nextLine[6].trim()); // business_hours
                stmt.bindString(8, nextLine[7].trim()); // holiday
                stmt.bindString(9, nextLine[8].trim()); // lat
                stmt.bindString(10, nextLine[9].trim()); // lng
                stmt.bindString(11, nextLine[10].trim()); // pc_url
                stmt.bindString(12, nextLine[11].trim()); // mobile_url
                stmt.bindString(13, nextLine[12].trim()); // column01
                stmt.bindString(14, nextLine[13].trim()); // column02
                stmt.bindString(15, nextLine[14].trim()); // column03
                stmt.bindString(16, nextLine[15].trim()); // column04
                stmt.bindString(17, nextLine[16].trim()); // column05
                stmt.bindString(18, nextLine[17].trim()); // use_flg
                stmt.bindString(19, nextLine[18].trim()); // created_at
                stmt.bindString(20, nextLine[19].trim()); // updated_at
                stmt.executeInsert();
                int increment = (int) ((double) num / size * 50);
                Util.logging(String.valueOf(increment));
                dialog.setProgress(50 + increment);
                num++;
            }
            dialog.setProgress(100);

            db.setTransactionSuccessful();
            return true;

        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // TODO 自動生成された catch ブロック
                    e.printStackTrace();
                }
            }
            stmt.close();
            db.endTransaction();
        }
    
        return false;
    }
    
    public boolean getImportStatus() {
        return import_status;
    }

}

package jp.pinetail.android.drugstore_map;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Shops {
    public Integer Rowid = null;
    public String Uid = null;
    public String Category = null;
    public String Name = null;
    public String Address = null;
    public String Tel = null;
    public String Access = null;
    public String BusinessHours = null;
    public String Holiday = null;
    public Double Lat = null;
    public Double Lng = null;
    public String PcUrl = null;
    public String MobileUrl = null;
    public String Column01 = null;
    public String Column02 = null;
    public String Column03 = null;
    public String Column04 = null;
    public String Column05 = null;
    public String UseFlg = null;
    public String CreatedAt = null;
    public String UpdatedAt = null;
    
    //URL由来のストリーム
    protected InputStream is;
    
    protected ArrayList<Shops> list;
    
    //ストリームを閉じる処理を共通メソッドとして定義
    public void close() {
        if (is != null) {
            try {
                is.close();
                is = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public ArrayList<Shops> getList() {
        return this.list;
    }

    public void setData(String key, String value) {
    }
    
}


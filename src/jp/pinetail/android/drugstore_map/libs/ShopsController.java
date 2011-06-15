package jp.pinetail.android.drugstore_map.libs;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import jp.pinetail.android.drugstore_map.R;
import jp.pinetail.android.drugstore_map.Shops;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;

public class ShopsController extends Thread {
    private Handler handler;
    private final Runnable listener;
    private View scroll;
    private Context context;
    private Shops item;
    private LayoutInflater inflater;
    private Handler mHandler = new Handler();
    private CategoryImage categoryImage;
    
    public ShopsController(Handler handler, Runnable listener, Context context, Shops item) {
        this.handler   = handler;
        this.listener  = listener;
        this.context   = context;
        this.item      = item;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        categoryImage = new CategoryImage(context);
    }

    @Override
    public void run() {
        setLayout();

        handler.post(listener);
    }
    
    public void setLayout() {
        
        final View view = inflater.inflate(R.layout.detail, null);
        
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display disp = wm.getDefaultDisplay();
        view.setMinimumWidth(disp.getWidth() - 20);
//        int width = disp.getWidth();
//        int height = disp.getHeight();

        if (item != null) {
            // カテゴリ
            ImageView imgCategory = (ImageView) view.findViewById(R.id.img_category);
            imgCategory.setImageDrawable(categoryImage.getDrawable(item.Category));

//            TextView textCategory = (TextView) view.findViewById(R.id.txt_category);
//            textCategory.setText(item.Category);

        	
            // 店名
            TextView textName = (TextView) view.findViewById(R.id.txt_name);
            textName.setText(item.Name);
            
            // 住所
            TextView textAddress = (TextView) view.findViewById(R.id.txt_address);
            textAddress.setText(Html.fromHtml(item.Address));

            // TEL
            TextView textTel = (TextView) view.findViewById(R.id.txt_tel);
            textTel.setText(item.Tel);

            // 営業時間
            TextView textBusinessHours = (TextView) view.findViewById(R.id.txt_business_hours);
            textBusinessHours.setText(Html.fromHtml(item.BusinessHours));

            // 定休日
            TextView textHoliday = (TextView) view.findViewById(R.id.txt_holiday);
            textHoliday.setText(item.Holiday);

            // セール
            if (item.Column01.length() > 0) {
            	
            	TableLayout tableSale = (TableLayout) view.findViewById(R.id.table_sale);
            	

                String[] sale_names = item.Column01.split("<br />");
                String[] sale_dates = item.Column02.split("<br />");
                
                for (int i = 0; i < sale_names.length;i++) {
                    View rowView = inflater.inflate(R.layout.sale_row, null);
                    
                    TextView textSaleName = (TextView) rowView.findViewById(R.id.txt_sale_name);
                    TextView textSaleDate = (TextView) rowView.findViewById(R.id.txt_sale_date);

                    textSaleName.setText(sale_names[i]);
                    textSaleDate.setText(sale_dates[i]);
                    
                    tableSale.addView(rowView);
                }
                
            } else {
                LinearLayout layoutSale = (LinearLayout) view.findViewById(R.id.layout_sale);
                layoutSale.setVisibility(View.GONE);
            }

            /*
            final Gallery gallery  = (Gallery) view.findViewById(R.id.Gallery1);
            gallery.setSpacing(20);
            
            new Thread(new Runnable() {
                public void run() {

                	/*
                    final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.ProgressBar01);

    	            String url = "http://api.tabelog.com/Ver1/ReviewImageSearch/?Key=" +
    	            context.getResources().getString(R.string.tabelog_access_key) + "&Rcd=" + item.TabelogId;
    	            
    	            XmlParserFromUrl xml = new XmlParserFromUrl();
    	        
    	            byte[] byteArray = Util.getByteArrayFromURL(url, "GET");
    	            if (byteArray == null) {
    	                return;
    	            }
    	            String data = new String(byteArray);
    	            /*
    	            ArrayList<ShopImages> images = xml.getShopImages(data);
    	Util.logging(String.valueOf(images.size()));
    	            if (images.size() > 0) {
    	            	
    	            	final String[] mImageIds = new String[images.size()];
    	            	int i = 0;
    	                for (ShopImages image: images) {
    	                    mImageIds[i] = image.ImageUrlL;
    	                    i++;
    	                }
    	                
                        mHandler.post(new Runnable() {
                            public void run() {
                                gallery.setAdapter(new ImageAdapter(context, mImageIds));
                                progressBar.setVisibility(View.GONE);
                            }
                        });
    	            }
    	            */
            /*
    	        }
		    }).start();
		    */
        }
        scroll = view;
    }
    
    public View getView() {
        return scroll;
    }
    
    public Shops getShopInfo() {
        return item;
    }
    
    public class ImageAdapter extends BaseAdapter {

        private final int mGalleryItemBackground;
        private final Context mContext;

        private String[] mImageIds = new String[1];

        private final float mDensity;

        public ImageAdapter(Context c, String[] images) {
            mContext = c;
            // See res/values/attrs.xml for the <declare-styleable> that defines
            // Gallery1.
            TypedArray a = context.obtainStyledAttributes(R.styleable.Gallery1);
            mGalleryItemBackground = a.getResourceId(
                    R.styleable.Gallery1_android_galleryItemBackground, 0);
            a.recycle();

            mDensity = c.getResources().getDisplayMetrics().density;
            
            mImageIds = images;

        }

        public int getCount() {
            return mImageIds.length;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            final ImageView imageView;
            if (convertView == null) {
                convertView = new ImageView(mContext);

                imageView = (ImageView) convertView;
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setLayoutParams(new Gallery.LayoutParams(150, 150));
                imageView.setPadding(10, 0, 10, 0);

                // The preferred Gallery item background
                imageView.setBackgroundResource(R.drawable.border_kind_member);

//                imageView.setBackgroundResource(mGalleryItemBackground);
            } else {
                imageView = (ImageView) convertView;
            }

//            new Thread(new Runnable() {
//                public void run() {
		            URL url;
					try {
						url = new URL(mImageIds[position]);
						final Bitmap bitmap = BitmapFactory.decodeStream(url.openStream());
			            
//			            mHandler.post(new Runnable() {
//			                public void run() {
			                    
					            imageView.setImageBitmap(bitmap);
//			                }
//			            });

					} catch (MalformedURLException e) {
						// TODO 自動生成された catch ブロック
						e.printStackTrace();
					} catch (IOException e) {
						// TODO 自動生成された catch ブロック
						e.printStackTrace();
					}
					
		            
//		        }
//		    }).start();

            return imageView;
        }
    }
}

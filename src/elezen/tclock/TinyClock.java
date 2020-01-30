package elezen.tclock;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import android.widget.TextView;

public class TinyClock extends Activity {
    Handler handler=new Handler();  	
	private TextView tv=null;
	private long timeZoneOffset;
	private String[] fonts;
	private final int ORIID=-1000,SETCOLOR=-2,SET_FULL_SCREEN=-3,SUBMENU=-4,SETDATECOLOR=-5,
			ORIMENU=-100,SET_H24=-7;
	private boolean fullScreen=false,h24=false;
	private Typeface fontFace=null;
	private MenuItem mMenufullScreen=null,mMenu24;
	private float mY=0;
	private TextView mDateView;
	private SimpleDateFormat mSimpleDF;
	private long oldD=0;
	private boolean dayPatch=false;
	private String[] dayNames=null;
//	private int test=3;
	SpannableStringBuilder ssb=new SpannableStringBuilder("00:00:00");
	AbsoluteSizeSpan has=new AbsoluteSizeSpan(64,true),
			mas=new AbsoluteSizeSpan(32,true);

	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Resources rs=getResources();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tv=(TextView)findViewById(R.id.t1);
		mDateView=(TextView)findViewById(R.id.t2);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    timeZoneOffset=TimeZone.getDefault().getRawOffset();
	    mSimpleDF= new SimpleDateFormat(rs.getString(R.string.date_formate),Locale.getDefault());
	    dayPatch=rs.getBoolean(R.bool.date_patch);
	    dayNames=rs.getStringArray(R.array.dayNames);
	    //	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); 
	    
	    AssetManager assets=getAssets();
		try {
			fonts=assets.list("fonts");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        SharedPreferences sp =getSharedPreferences("Settings", MODE_PRIVATE);
        int id;
        id=sp.getInt("ORIENTATION", 0);
        setRequestedOrientation(id); 
        id=sp.getInt("FCOLOR",0xffffffff);
        tv.setTextColor(id);
        
        id=sp.getInt("DCOLOR",0xffffffff);
        mDateView.setTextColor(id);        
        id=sp.getInt("FONT",18);
        if(id>=0 && id<fonts.length){
        	fontFace = Typeface.createFromAsset(getAssets(),
                "fonts/"+fonts[id]);
        	tv.setTypeface(fontFace);
        }
        fullScreen=sp.getBoolean("FULLSCREEN", false);
        if(fullScreen)setFullScreen(fullScreen);
        h24=sp.getBoolean("H24", false);
        autoFit();

	}

	private void setFullScreen(boolean b){
		if(b)getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		handler.post(runnable); 
	}

	@Override
	protected void onPause(){
		super.onPause();
		handler.removeCallbacks(runnable);   
	}
	
	 @Override
	 public boolean onTouchEvent(MotionEvent event) {
		 if(event.getAction() == MotionEvent.ACTION_DOWN) {
			 mY = event.getY();
		 }
		 if(event.getAction() == MotionEvent.ACTION_UP) {
			 if(mY - event.getY() > 100)openOptionsMenu();
		 }
		 return super.onTouchEvent(event);
	 }
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu)  
    {  
		int i=0;
		menu.add(1,SETCOLOR,0,getResources().getString(R.string.color));
		menu.add(1,SETDATECOLOR,1,getResources().getString(R.string.date_color));
		SubMenu subMenu = menu.addSubMenu(1,SUBMENU,2,R.string.font);
		for (String s : fonts){
			subMenu.add(1,i,i,s.replace(".ttf",""));
			i++;
		}
		subMenu=menu.addSubMenu(1,ORIMENU,3,R.string.orientation);
		Resources res=getResources();
		subMenu.add(1,ORIID-0,0,res.getString(R.string.auto));
		subMenu.add(1,ORIID-1,1,res.getString(R.string.landscape));
		subMenu.add(1,ORIID-2,2,res.getString(R.string.portrait));
		mMenufullScreen=menu.add(1,SET_FULL_SCREEN,4,
				getResources().getString(fullScreen?R.string.normal_screen:R.string.full_screen));
		mMenu24=menu.add(1,SET_H24,4,
				getResources().getString(h24?R.string.h24:R.string.h12));
		
        return true;  
    }  
	private int setOrientation(int i){
		final int[] oris={ActivityInfo.SCREEN_ORIENTATION_SENSOR,
				ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
				ActivityInfo.SCREEN_ORIENTATION_PORTRAIT};
		if(i<0||i>oris.length)i=0;
		i=oris[i];
		setRequestedOrientation(i);
		return i;
	}
	private void saveSetings(String name,int value){
	    SharedPreferences sp =getSharedPreferences("Settings", MODE_PRIVATE);
	    Editor editor=sp.edit();
	    editor.putInt(name,value);
	    editor.commit();        		
	}
	private void saveSetings(String name,boolean value){
	    SharedPreferences sp =getSharedPreferences("Settings", MODE_PRIVATE);
	    Editor editor=sp.edit();
	    editor.putBoolean(name,value);
	    editor.commit();        		
	}	
	@Override  
    public boolean onOptionsItemSelected(MenuItem item)  
    {  
		ColorPickerDialog dialog;
        int id = item.getItemId();  
        if(id>=0 && id<fonts.length){
    		fontFace = Typeface.createFromAsset(getAssets(),
                    "fonts/"+fonts[id]);
    		tv.setTypeface(fontFace);
    		calTextSize();
    		saveSetings("FONT",id);
        }else if(id<=ORIID){
        	id-=ORIID;
        	id=setOrientation(-id);
        	saveSetings("ORIENTATION",id);
        }else{
        	switch(id){
        		case SETCOLOR:
                	dialog = new ColorPickerDialog(this, tv.getTextColors().getDefaultColor(), 
            				getResources().getString(R.string.color_picker), 
            				new ColorPickerDialog.OnColorChangedListener() {
            				
            				@Override
            				public void colorChanged(int color) {
            					tv.setTextColor(color);
            					saveSetings("FCOLOR",color);
            				}
            			});
            		dialog.show();        	
        			break;
        		case SET_FULL_SCREEN:
                	fullScreen=!fullScreen;
                	setFullScreen(fullScreen);
                	mMenufullScreen.setTitle(fullScreen?R.string.normal_screen:R.string.full_screen);
                	saveSetings("FULLSCREEN",fullScreen);
        			break;
        		case SET_H24:
        			h24=!h24;
                	mMenu24.setTitle(h24?R.string.h24:R.string.h12);
                	saveSetings("H24",h24);
        			break;
        		case SETDATECOLOR:
                	dialog = new ColorPickerDialog(this, mDateView.getTextColors().getDefaultColor(), 
            				getResources().getString(R.string.color_picker), 
            				new ColorPickerDialog.OnColorChangedListener() {
            				
            				@Override
            				public void colorChanged(int color) {
            					mDateView.setTextColor(color);
            					saveSetings("DCOLOR",color);
            				}
            			});
            		dialog.show();  
            		break;
        		default:
        	}
        	
        }
        return true;  
    }  
	
	@SuppressWarnings("deprecation")
	private void updateDate(long t){
		String s;
		Date d=new Date(t);
		s=mSimpleDF.format(d);
		if(dayPatch){
			s=s+dayNames[d.getDay()];
		}

		mDateView.setText(s);
		
//		else 
//			mDateView.setText(getResources().getString(R.string.date_sample));
		
	}
	
	Runnable runnable=new Runnable() {  
        @Override  
        public void run() {  
        	update();
        }  
    };  
    void update(){
    	long tm,h,m,s,d,t=System.currentTimeMillis();
    	tm=t+timeZoneOffset;
    	d=tm/(24*60*60*1000);
    	if(d != oldD){
    		oldD=d;
    		updateDate(tm);
    	}
    	if(h24){
    		h=tm/(60*60*1000) % 24;
    	}else{
            h=tm/(60*60*1000) % 12;
            if(h==0)h=12;
    	}
        m=tm/(60*1000)%60;
        s=tm/1000%60;
//        if(test>0){
 //       	h=28;m=88;s=88;oldD=0;
//        	test--;
//        }
        ssb.replace(0, 5, String.format(Locale.ENGLISH, "%2d:%02d", h,m));
        ssb.replace(5, 8, String.format(Locale.ENGLISH, ":%02d", s));
    	tv.setText(ssb);
        handler.postDelayed(runnable, 1000-t%1000);      	
    }
    private final void calTextSize(){
    	Paint p=tv.getPaint();
    	float tw,textSize,f;
    	handler.removeCallbacks(runnable);
        float w = tv.getWidth()-tv.getPaddingLeft()-tv.getPaddingRight();
        float h = mDateView.getHeight()+tv.getHeight();
        Rect rect = new Rect();
		p.setTextSize(240F);
		p.getTextBounds("88:88", 0, 5, rect);
		float h1=rect.height();
		tw=rect.width();
		p.setTextSize(120F);
		p.getTextBounds(":88", 0, 3, rect);
		tw+=rect.width();
		textSize=240F*w/tw*0.9F;
		h1=h1*w/tw;
		float hh=h*0.8F;
		if(h1>hh)textSize=textSize*hh/h1;
		Log.e("***Height1",String.valueOf(h1));
		p.setTextSize(textSize);
		Log.e("***size",String.valueOf(textSize));

		has=new AbsoluteSizeSpan((int) textSize,false);
		mas=new AbsoluteSizeSpan((int) textSize/2,false);
		ssb.clearSpans();
		ssb.setSpan(has, 0, 5, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		ssb.setSpan(mas, 5, 8, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

		// date size
		p=mDateView.getPaint();
		p.setTextSize(240F);

		String s=getResources().getString(R.string.date_sample);
		p.getTextBounds(s, 0, s.length(), rect);
		tw=rect.width();
		f=240F*w/tw*0.9F;
		h1=rect.height()*w/tw;
		hh=h*0.2F;
		if(h1>hh)f=f*hh/h1;
		p.setTextSize(f);
//		fm = p.getFontMetrics();  
//		h1 = (float)Math.ceil(fm.descent - fm.ascent);
	
		Log.e("***Height2",String.valueOf(h1));
		Log.e("***size2",String.valueOf(f));
		
		mDateView.setTextSize(TypedValue.COMPLEX_UNIT_PX, f);
		updateDate(System.currentTimeMillis());		
    	handler.post(runnable);
    }


    private final void autoFit(){
        ViewTreeObserver greenObserver = tv.getViewTreeObserver();
        greenObserver.addOnPreDrawListener(new OnPreDrawListener() {

            @Override
            public boolean onPreDraw() {
                tv.getViewTreeObserver().removeOnPreDrawListener(this);
                calTextSize();
                return true;
            }
        }); 

        

    }
    
}

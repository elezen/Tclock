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
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends Activity {
    Handler handler=new Handler();  	
	public TextView tv=null;
	private long timeZoneOffset;
	private String[] fonts;
	private final int ORIID=-1000,SETCOLOR=-2,SET_FULL_SCREEN=-3,SUBMENU=-4,SETDATECOLOR=-5,
			ORIMENU=-6;
	private boolean fullScreen=false;
	private Typeface fontFace=null;
	private MenuItem mMenufullScreen=null;
	private float mY=0;
	private TextView mDateView;
	private SimpleDateFormat mSimpleDF;
	SpannableStringBuilder ssb=new SpannableStringBuilder("00:00:00");
	AbsoluteSizeSpan has=new AbsoluteSizeSpan(64,true),
			mas=new AbsoluteSizeSpan(32,true);

	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tv=(TextView)findViewById(R.id.t1);
		mDateView=(TextView)findViewById(R.id.t2);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    timeZoneOffset=TimeZone.getDefault().getRawOffset();
	    mSimpleDF= new SimpleDateFormat(getResources().getString(R.string.date_formate),Locale.getDefault());
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
	
	private void updateDate(long t){
		mDateView.setText(mSimpleDF.format(new Date(t)));
	}
	
	Runnable runnable=new Runnable() {  
        @Override  
        public void run() {  
        	long tm,h,m,s,t=System.currentTimeMillis();
        	tm=t+timeZoneOffset;
            h=tm/(60*60*1000) % 12;
            if(h==0){
            	h=12;
            	updateDate(tm);
            }
            m=tm/(60*1000)%60;
            s=tm/1000%60;
            ssb.replace(0, 5, String.format(Locale.US, "%2d:%02d", h,m));
            ssb.replace(5, 8, String.format(Locale.US, ":%02d", s));
        	tv.setText(ssb);
            handler.postDelayed(this, 1000-t%1000);  
        }  
    };  

    private final void calTextSize(){
    	Paint p=tv.getPaint();
    	float tw,textSize,f;
    	handler.removeCallbacks(runnable);
        float w = tv.getWidth()-tv.getPaddingLeft()-tv.getPaddingRight();

		p.setTextSize(240F);
		tw=p.measureText("00:00");
		p.setTextSize(120F);
		tw+=p.measureText(":00");
		textSize=240F*w/tw-8F;
		p.setTextSize(240F);
		tw=p.measureText(getResources().getString(R.string.date_sample));
		f=240F*w/tw-8F;
		if(f>textSize/5F)f=textSize/5F;
		mDateView.setTextSize(TypedValue.COMPLEX_UNIT_PX, f);
		updateDate(System.currentTimeMillis());
		has=new AbsoluteSizeSpan((int) textSize,false);
		mas=new AbsoluteSizeSpan((int) textSize/2,false);
		ssb.clearSpans();
		ssb.setSpan(has, 0, 5, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		ssb.setSpan(mas, 5, 8, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
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

package net.whzxt.zxtexam;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

	private LinearLayout layStart,laySystem,layDetect;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layStart = (LinearLayout)findViewById(R.id.layStart);
        laySystem = (LinearLayout)findViewById(R.id.laySystem);
        layDetect = (LinearLayout)findViewById(R.id.layDetect);
        layStart.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				//startActivity(new Intent().setClass(MainActivity.this, ChooseActivity.class));
			}
		});
        laySystem.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent().setClass(MainActivity.this, SystemActivity.class));
			}
		});
        layDetect.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent().setClass(MainActivity.this, DetectActivity.class));
			}
		});
    }
    
}

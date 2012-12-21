package net.whzxt.zxtexam;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;

public class RouteEditActivity extends Activity {

	private LinearLayout layItem1_s;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route_edit);

        Bundle bundle = this.getIntent().getExtras();
        int id = bundle.getInt("routeid");
        Toast.makeText(RouteEditActivity.this, String.valueOf(id), Toast.LENGTH_SHORT).show();
		layItem1_s = (LinearLayout)findViewById(R.id.layItem1_s);
		layItem1_s.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				startActivity(new Intent().setClass(RouteEditActivity.this, ItemEditActivity.class));
			}
		});
	}

}

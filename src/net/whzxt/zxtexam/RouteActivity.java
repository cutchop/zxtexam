package net.whzxt.zxtexam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class RouteActivity extends Activity {

	private ListView listView;
	private Metadata md;
	private List<String> data;
	private Map<Integer, Integer> mapRouteID;
	private int i;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route);
		listView = (ListView) findViewById(R.id.listView1);
		md = (Metadata) getApplication();
		data = new ArrayList<String>();
		mapRouteID = new HashMap<Integer, Integer>();
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				if (arg2 == data.size() - 1) {
					bundle.putInt("routeid", -1);
				} else {
					bundle.putInt("routeid", mapRouteID.get(arg2));
				}
				intent.putExtras(bundle);
				intent.setClass(RouteActivity.this, RouteEditActivity.class);
				startActivityForResult(intent, 0);
			}
		});
		load();
	}

	private void load() {
		data.clear();
		i = 0;
		Cursor cursor = md.rawQuery("select routeid,name from " + DBer.T_ROUTE + " order by routeid");
		if (cursor.moveToFirst()) {
			do {
				data.add(cursor.getString(cursor.getColumnIndex("name")));
				mapRouteID.put(i, cursor.getInt(cursor.getColumnIndex("routeid")));
				i++;
			} while (cursor.moveToNext());
		}
		cursor.close();
		data.add("添加路线...");
		listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, data));
	}

	@Override
	public void onAttachedToWindow() {
		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
		super.onAttachedToWindow();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case RESULT_OK:
			load();
			break;
		default:
			break;
		}
	}

}

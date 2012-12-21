package net.whzxt.zxtexam;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RouteEditActivity extends Activity {

	private EditText txtName, txtTts;
	private Button btnName, btnTts;
	private LinearLayout layItems;
	private Metadata md;
	private int routeid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route_edit);
		md = (Metadata) getApplication();
		txtName = (EditText) findViewById(R.id.txtName);
		txtTts = (EditText) findViewById(R.id.txtTts);
		btnName = (Button) findViewById(R.id.btnName);
		btnTts = (Button) findViewById(R.id.btnTts);
		layItems = (LinearLayout) findViewById(R.id.layItems);

		Bundle bundle = this.getIntent().getExtras();
		routeid = bundle.getInt("routeid");

		if (routeid > -1) {
			Cursor cursor = md.rawQuery("select * from " + DBer.T_ROUTE + " where routeid=" + routeid);
			if (cursor.moveToFirst()) {
				txtName.setText(cursor.getString(cursor.getColumnIndex("name")));
				txtTts.setText(cursor.getString(cursor.getColumnIndex("tts")));
			}
			cursor.close();
		} else {
			txtName.setEnabled(true);
			btnName.setText("保存");
		}
		TextView textView = new TextView(this);
		textView.setTextAppearance(RouteEditActivity.this, android.R.attr.textAppearanceMedium);

		btnName.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (btnName.getText().equals("修改")) {
					txtName.setEnabled(true);
					btnName.setText("保存");
				} else {
					if (txtName.getText().toString().trim().equals("")) {
						Toast.makeText(RouteEditActivity.this, "请填写路线名称", Toast.LENGTH_SHORT).show();
						return;
					}
					if (routeid == -1) {
						Cursor cursor = md.rawQuery("select max(routeid) as id from " + DBer.T_ROUTE);
						if (cursor.moveToFirst()) {
							routeid = cursor.getInt(0);
						}
						cursor.close();
						if (routeid > -1) {
							routeid++;
							md.execSQL("insert into " + DBer.T_ROUTE + "(routeid,name,tts) values(" + routeid + ",'" + txtName.getText() + "','')");
							btnName.setText("修改");
							txtName.setEnabled(false);
						} else {
							Toast.makeText(RouteEditActivity.this, "路线保存失败", Toast.LENGTH_SHORT).show();
						}
					} else {
						md.execSQL("update " + DBer.T_ROUTE + " set name='" + txtName.getText() + "' where routeid=" + routeid);
						btnName.setText("修改");
						txtName.setEnabled(false);
					}
				}
			}
		});
		btnTts.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (btnTts.getText().equals("修改")) {
					if (routeid == -1) {
						Toast.makeText(RouteEditActivity.this, "请先保存路线名称", Toast.LENGTH_SHORT).show();
					} else {
						txtTts.setEnabled(true);
						btnTts.setText("保存");
					}
				} else {
					if (txtTts.getText().toString().trim().equals("")) {
						Toast.makeText(RouteEditActivity.this, "请填写语音提示文本", Toast.LENGTH_SHORT).show();
						return;
					}
					md.execSQL("update " + DBer.T_ROUTE + " set tts='" + txtTts.getText() + "' where routeid=" + routeid);
					btnTts.setText("修改");
					txtTts.setEnabled(false);
				}
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.setResult(RESULT_OK);
			this.finish();
		}
		return false;
	}

}

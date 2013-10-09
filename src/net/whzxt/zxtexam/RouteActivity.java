package net.whzxt.zxtexam;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class RouteActivity extends Activity {

	private ListView listView;
	private Metadata md;
	private List<String> data;
	private Map<Integer, Integer> mapRouteID;
	private int i;
	private String[] serverRoutes;

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
	public boolean onCreateOptionsMenu(Menu menu) {
		int groupId = 0;
		int menuItemOrder = Menu.NONE;
		menu.add(groupId, 0, menuItemOrder, "下载更多路线").setIcon(android.R.drawable.ic_menu_info_details);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			Toast.makeText(RouteActivity.this, "正在获取路线列表,请稍候...", Toast.LENGTH_LONG).show();
			new AsyncTask<Void, Void, Integer>() {
				@Override
				protected Integer doInBackground(Void... args) {
					HttpPost httpRequest = new HttpPost(getString(R.string.server) + "/examroute.ashx");
					List<NameValuePair> params = new ArrayList<NameValuePair>(1);
					params.add(new BasicNameValuePair("t", "l"));
					try {
						httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
					} catch (UnsupportedEncodingException e1) {
						e1.printStackTrace();
						return 0;
					}
					HttpResponse httpResponse = null;
					try {
						httpResponse = new DefaultHttpClient().execute(httpRequest);
					} catch (ClientProtocolException e1) {
						e1.printStackTrace();
						return 0;
					} catch (IOException e1) {
						e1.printStackTrace();
						return 0;
					}
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						try {
							String result = EntityUtils.toString(httpResponse.getEntity());
							if (result.equals("none")) {
								return -1;
							} else {
								serverRoutes = result.split("\\|");
								if (serverRoutes.length > 0) {
									return 1;
								} else {
									return -1;
								}
							}
						} catch (ParseException e) {
							e.printStackTrace();
							return 0;
						} catch (IOException e) {
							e.printStackTrace();
							return 0;
						}
					} else {
						return 0;
					}
				}

				@Override
				protected void onPostExecute(Integer result) {
					if (result == -1) {
						Toast.makeText(RouteActivity.this, "暂时没有路线可供下载", Toast.LENGTH_LONG).show();
					} else if (result == 0) {
						Toast.makeText(RouteActivity.this, "连接失败,请重试", Toast.LENGTH_LONG).show();
					} else {
						String[] strs = new String[serverRoutes.length];
						for (int i = 0; i < strs.length; i++) {
							strs[i] = serverRoutes[i].split(",")[1] + "(" + serverRoutes[i].split(",")[2] + ")";
						}
						AlertDialog alertDialog = new AlertDialog.Builder(RouteActivity.this).setTitle("请选择要下载的路线").setIcon(android.R.drawable.ic_menu_add).setItems(strs, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								downRoute(which);
							}
						}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								return;
							}
						}).create();
						alertDialog.show();
					}
				}
			}.execute();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void downRoute(final int which) {
		Toast.makeText(RouteActivity.this, "正在下载路线,请稍候...", Toast.LENGTH_LONG).show();
		new AsyncTask<Void, Void, Integer>() {
			@Override
			protected Integer doInBackground(Void... args) {
				HttpPost httpRequest = new HttpPost(getString(R.string.server) + "/examroute.ashx");
				List<NameValuePair> params = new ArrayList<NameValuePair>(2);
				params.add(new BasicNameValuePair("t", "g"));
				params.add(new BasicNameValuePair("guid", serverRoutes[which].split(",")[0]));
				try {
					httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
					return 0;
				}
				HttpResponse httpResponse = null;
				try {
					httpResponse = new DefaultHttpClient().execute(httpRequest);
				} catch (ClientProtocolException e1) {
					e1.printStackTrace();
					return 0;
				} catch (IOException e1) {
					e1.printStackTrace();
					return 0;
				}
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					try {
						String result = EntityUtils.toString(httpResponse.getEntity());
						if (result.equals("")) {
							return 0;
						} else {
							String[] items = result.split("\\|");
							if (items.length > 0) {
								int routeid = 0;
								Cursor cursor = md.rawQuery("select max(routeid) as id from " + DBer.T_ROUTE);
								if (cursor.moveToFirst()) {
									routeid = cursor.getInt(0);
								}
								cursor.close();
								if (routeid < 50) {
									routeid = 50;
								}
								routeid++;
								md.execSQL("insert into " + DBer.T_ROUTE + "(routeid,name,tts,auto) values(" + routeid + ",'" + serverRoutes[which].split(",")[1] + "','当前路线为" + serverRoutes[which].split(",")[1] + "',1)");
								for (int i = 0; i < items.length; i++) {
									md.execSQL("insert into " + DBer.T_ROUTE_ITEM + "(routeid,itemid,lon,lat,angle,xuhao) values(" + routeid + "," + items[i].split(",")[0] + "," + items[i].split(",")[1] + "," + items[i].split(",")[2] + "," + items[i].split(",")[3] + "," + items[i].split(",")[4] + ")");
								}
								return 1;
							} else {
								return 0;
							}
						}
					} catch (ParseException e) {
						e.printStackTrace();
						return 0;
					} catch (IOException e) {
						e.printStackTrace();
						return 0;
					}
				} else {
					return 0;
				}
			}

			@Override
			protected void onPostExecute(Integer result) {
				if (result == 0) {
					Toast.makeText(RouteActivity.this, "连接失败,请重试", Toast.LENGTH_LONG).show();
				} else {
					load();
					Toast.makeText(RouteActivity.this, "路线下载成功!", Toast.LENGTH_LONG).show();
				}
			}
		}.execute();
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

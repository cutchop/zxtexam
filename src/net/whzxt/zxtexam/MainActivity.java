package net.whzxt.zxtexam;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {

	private LinearLayout layStart, laySystem, layDetect;
	private String strResult;
	private ProgressDialog _updateDialog;
	private File _downLoadFile;
	private int _fileLength, _downedFileLength = 0;
	private static final int H_W_UPDATEDIALOG_MAX = 0x01;
	private static final int H_W_UPDATEDIALOG_NOW = 0x02;
	private Metadata md;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case H_W_UPDATEDIALOG_MAX:
				_updateDialog.setMax(_fileLength);
				break;
			case H_W_UPDATEDIALOG_NOW:
				int x = _downedFileLength * 100 / _fileLength;
				_updateDialog.setMessage("正在下载，已完成" + x + "%");
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		layStart = (LinearLayout) findViewById(R.id.layStart);
		laySystem = (LinearLayout) findViewById(R.id.laySystem);
		layDetect = (LinearLayout) findViewById(R.id.layDetect);
		md = (Metadata)getApplication();
		layStart.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent().setClass(MainActivity.this, ChooseActivity.class));
			}
		});
		laySystem.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final EditText txtpsd = new EditText(MainActivity.this);
				AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).setTitle("请输入密码").setIcon(android.R.drawable.ic_menu_help).setView(txtpsd).setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {						
						if (txtpsd.getText().toString().toLowerCase().equals(md.getPassword().toLowerCase())) {
							startActivity(new Intent().setClass(MainActivity.this, SystemActivity.class));
						} else {
							Toast.makeText(MainActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
						}
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						return;
					}
				}).create();
				alertDialog.show();
			}
		});
		layDetect.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent().setClass(MainActivity.this, DetectActivity.class));
			}
		});
		// 检查更新
		new AsyncTask<Void, Void, Integer>() {
			@Override
			protected Integer doInBackground(Void... args) {
				HttpPost httpRequest = new HttpPost(getString(R.string.server) + "/examversion.ashx");
				HttpResponse httpResponse = null;
				try {
					httpResponse = new DefaultHttpClient().execute(httpRequest);
				} catch (ClientProtocolException e) {
					e.printStackTrace();
					return 0;
				} catch (IOException e) {
					e.printStackTrace();
					return 0;
				}
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					try {
						strResult = EntityUtils.toString(httpResponse.getEntity());
					} catch (ParseException e) {
						e.printStackTrace();
						return 0;
					} catch (IOException e) {
						e.printStackTrace();
						return 0;
					}
					if (strResult.startsWith("s|")) {
						return 1;
					} else if (strResult.startsWith("f|")) {
						return 2;
					}
				}
				return 0;
			}

			@Override
			protected void onPostExecute(Integer result) {
				if (result == 1) {
					String[] results = strResult.split("\\|");
					if (results.length > 1) {
						if (!results[1].equals(getString(R.string.version))) {
							AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).setTitle("发现新版本,是否更新程序？").setIcon(android.R.drawable.ic_menu_help).setPositiveButton("现在更新", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									newversion();
								}
							}).setNegativeButton("下次更新", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									return;
								}
							}).create();
							alertDialog.show();
						}
					}
				}
			}
		}.execute();
	}

	private void newversion() {
		_updateDialog = new ProgressDialog(MainActivity.this);
		_updateDialog.setMessage("正在等待下载新版本...");
		_updateDialog.setIndeterminate(true);
		_updateDialog.show();
		new AsyncTask<Void, Void, Integer>() {
			@Override
			protected Integer doInBackground(Void... args) {
				try {
					URL url = new URL(getString(R.string.server) + "/zxtexam.apk");
					URLConnection connection = url.openConnection();
					connection.connect();
					InputStream inputStream = connection.getInputStream();
					String savePath = Environment.getExternalStorageDirectory() + "/download";
					File file = new File(savePath);
					if (!file.exists()) {
						file.mkdir();
					}
					String savePathString = Environment.getExternalStorageDirectory() + "/download/zxtexam.apk";
					_downLoadFile = new File(savePathString);
					if (_downLoadFile.exists()) {
						_downLoadFile.delete();
					}
					_downLoadFile.createNewFile();
					OutputStream outputStream = new FileOutputStream(_downLoadFile);
					_fileLength = connection.getContentLength();
					handler.sendEmptyMessage(H_W_UPDATEDIALOG_MAX);
					byte[] buffer = new byte[128];
					while (_downedFileLength < _fileLength) {
						int numRead = inputStream.read(buffer);
						_downedFileLength += numRead;
						outputStream.write(buffer, 0, numRead);
						handler.sendEmptyMessage(H_W_UPDATEDIALOG_NOW);
					}
					return 1;
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return 0;
			}

			@Override
			protected void onPostExecute(Integer result) {
				if (result == 1) {
					_updateDialog.setMessage("下载完成！");
					_updateDialog.dismiss();
					Intent intent = new Intent();
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setAction(android.content.Intent.ACTION_VIEW);
					String type = "application/vnd.android.package-archive";
					intent.setDataAndType(Uri.fromFile(_downLoadFile), type);
					startActivity(intent);
				}
			}
		}.execute();
	}
}

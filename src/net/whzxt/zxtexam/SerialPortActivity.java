package net.whzxt.zxtexam;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android_serialport_api.SerialPort;

public abstract class SerialPortActivity extends Activity {

	protected Metadata md;
	protected SerialPort mSerialPort;
	private BluetoothDevice mBlueDevice;
	protected BluetoothSocket mBlueSocket;
	protected OutputStream mOutputStream;
	private InputStream mInputStream;
	private ReadThread mReadThread;
	private final int DL_SEARCHING = 0x01;
	private final int DL_CONNECTING = 0x02;
	private final UUID BLUEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private List<BluetoothDevice> listBlue;
	protected Boolean isDeviceOK = false;
	protected TextToSpeech mTts;

	private class ReadThread extends Thread {
		@Override
		public void run() {
			super.run();
			byte[] buffer = new byte[32];
			int tsize = 0;
			while (!isInterrupted()) {
				int size;
				try {
					if (mInputStream == null)
						return;
					byte[] tmp = new byte[32];
					size = mInputStream.read(tmp);
					if (tsize == 0) {
						if (tmp[0] == 0x1A) {
							for (int i = 0; i < size; i++) {
								buffer[i] = tmp[i];
								tsize++;
							}
						}
					} else {
						for (int i = 0; i < size ; i++) {
							buffer[tsize] = tmp[i];
							tsize++;
							if (tsize >= 32) {
								break;
							}
						}
					}
					if (tsize >= 17) {
						for (int i = 0; i < buffer.length; i++) {
							if (buffer[i] == 0x1D) {
								tsize = i + 1;
								break;
							}
						}
						if (buffer[tsize-1] == 0x1D) {
							if (!isDeviceOK) {
								isDeviceOK = true;	
							}
							onDataReceived(buffer, tsize);
							tsize = 0;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

	protected void DisplayError(int resourceId) {
		DisplayError(getString(resourceId));
	}

	protected void DisplayError(String msg) {
		if (mTts != null) {
			mTts.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
		}
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle("错误");
		b.setMessage(msg);
		b.setPositiveButton("关闭", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				destroy();
			}
		});
		b.show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		md = (Metadata) getApplication();
		if (md.getDataResourceType() == 0) {
			// 串口
			try {
				mSerialPort = md.getSerialPort();
				mOutputStream = mSerialPort.getOutputStream();
				mInputStream = mSerialPort.getInputStream();

				/* Create a receiving thread */
				mReadThread = new ReadThread();
				mReadThread.start();
			} catch (SecurityException e) {
				DisplayError(R.string.error_security);
				e.printStackTrace();
			} catch (IOException e) {
				DisplayError(R.string.error_unknown);
				e.printStackTrace();
			} catch (InvalidParameterException e) {
				DisplayError(R.string.error_configuration);
				e.printStackTrace();
			}
		} else {
			// 蓝牙
			BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
			if (!mAdapter.isEnabled()) {
				mAdapter.enable();
			}
			// 获取已配对的设备
			Boolean find = false;
			Set<BluetoothDevice> devices = mAdapter.getBondedDevices();
			if (devices.size() > 0) {
				for (BluetoothDevice bluetoothDevice : devices) {
					if (md.getBlueAddress().equals(bluetoothDevice.getAddress())) {
						find = true;
						mBlueDevice = bluetoothDevice;
						showDialog(DL_CONNECTING);
						_clientThread.start();
						break;
					}
				}
			}
			if (!find) {
				// 搜索设备
				listBlue = new ArrayList<BluetoothDevice>();
				showDialog(DL_SEARCHING);
				IntentFilter filter = new IntentFilter();
				filter.addAction(BluetoothDevice.ACTION_FOUND);
				filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
				filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
				this.registerReceiver(mReceiver, filter);
				mAdapter.startDiscovery();
			}
		}
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				listBlue.add(device);
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) { // 搜索结束
				dismissDialog(DL_SEARCHING);
				if (listBlue.size() == 0) {
					DisplayError(R.string.error_unfind);
				} else {
					String[] names = new String[listBlue.size()];
					for (int i = 0; i < listBlue.size(); i++) {
						names[i] = listBlue.get(i).getName();
					}
					AlertDialog alertDialog = new AlertDialog.Builder(SerialPortActivity.this).setTitle("请选择要连接的设备").setIcon(android.R.drawable.ic_menu_add).setItems(names, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							try {
								if (listBlue.get(which).getBondState() == BluetoothDevice.BOND_NONE) {
									Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
									createBondMethod.invoke(listBlue.get(which));
								} else {
									mBlueDevice = listBlue.get(which);
									showDialog(DL_CONNECTING);
									_clientThread.start();
								}
							} catch (Exception e) {
								DisplayError("设备无法配对");
								e.printStackTrace();
							}
						}
					}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							DisplayError("无线设备连接失败");
						}
					}).create();
					alertDialog.show();
				}
			} else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				switch (device.getBondState()) {
				case BluetoothDevice.BOND_BONDING:
					break;
				case BluetoothDevice.BOND_BONDED:
					mBlueDevice = device;
					showDialog(DL_CONNECTING);
					_clientThread.start();
					break;
				case BluetoothDevice.BOND_NONE:
				default:
					break;
				}
			}

		}
	};

	private Thread _clientThread = new Thread(new Runnable() {
		public void run() {
			if (mBlueDevice != null) {
				try {
					mBlueSocket = mBlueDevice.createRfcommSocketToServiceRecord(BLUEUUID);
					mBlueSocket.connect();
					mInputStream = mBlueSocket.getInputStream();
					mOutputStream = mBlueSocket.getOutputStream();
					md.setBlueAddress(mBlueDevice.getAddress());
					runOnUiThread(new Runnable() {
						public void run() {
							dismissDialog(DL_CONNECTING);
							mReadThread = new ReadThread();
							mReadThread.start();
						}
					});
				} catch (IOException e) {
					runOnUiThread(new Runnable() {
						public void run() {
							dismissDialog(DL_CONNECTING);
							DisplayError("与无线设备" + mBlueDevice.getName() + "连接失败");
						}
					});
					e.printStackTrace();
				}
			}
		}
	});

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		AlertDialog dialog = null;
		switch (id) {
		case DL_SEARCHING:
			dialog = new AlertDialog.Builder(SerialPortActivity.this).setCancelable(false).setMessage("正在搜索无线设备...").setIcon(android.R.drawable.ic_dialog_info).create();
			break;
		case DL_CONNECTING:
			dialog = new AlertDialog.Builder(SerialPortActivity.this).setCancelable(false).setMessage("正在连接无线设备...").setIcon(android.R.drawable.ic_dialog_info).create();
			break;
		default:
			return null;
		}
		return dialog;
	}

	protected abstract void onDataReceived(final byte[] buffer, final int size);

	protected void destroy() {
		if (mReadThread != null) {
			mReadThread.interrupt();
		}
		md.closeSerialPort();
		mSerialPort = null;
		if (mBlueSocket != null) {
			try {
				mBlueSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Log.i("exam", "serialportactivity destroy");
	}
}

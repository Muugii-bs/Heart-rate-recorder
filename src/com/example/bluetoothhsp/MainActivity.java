package com.example.bluetoothhsp;

//import android.support.v7.app.ActionBarActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
//import android.bluetooth.BluetoothHealth;
//import android.bluetooth.BluetoothA2dp;


public class MainActivity extends Activity implements AdapterView.OnItemClickListener{
	
	private final String TAG = getClass().getSimpleName();
	private static final int REQUEST_ENABLE_BT = 1234;
	private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	private List<BluetoothDevice> mVoicerecognizing = Collections.synchronizedList(new ArrayList<BluetoothDevice>());
	private ArrayAdapter<String> mConnectedDevices;
	private ListView mList;
	private BluetoothHeadset mBluetoothHeadset;
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Bundle extras = intent.getExtras();
			int state = extras.getInt(BluetoothProfile.EXTRA_STATE);
			int prevState = extras.getInt(BluetoothProfile.EXTRA_PREVIOUS_STATE);
			BluetoothDevice device = extras.getParcelable(BluetoothDevice.EXTRA_DEVICE);
			if (action.equals(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)) {
				String stateStr = state == BluetoothHeadset.STATE_AUDIO_CONNECTED ? "AUDIO_CONNECTED"
						: state == BluetoothHeadset.STATE_AUDIO_CONNECTING ? "AUDIO_CONNECTING"
						: state == BluetoothHeadset.STATE_AUDIO_DISCONNECTED ? "AUDIO_DISCONNECTED"
						: "Unknown";
				String prevStateStr = prevState == BluetoothHeadset.STATE_AUDIO_CONNECTED ? "AUDIO_CONNECTED"
						: prevState == BluetoothHeadset.STATE_AUDIO_CONNECTING ? "AUDIO_CONNECTING"
						: prevState == BluetoothHeadset.STATE_AUDIO_DISCONNECTED ? "AUDIO_DISCONNECTED"
						: "Unknown";
				Log.d(TAG, "BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED: EXTRA_DEVICE=" + device.getName() + " EXTRA_STATE=" + stateStr + " EXTRA_PREVIOUS_STATE=" + prevStateStr);
			} else if (action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)) {
				String stateStr = state == BluetoothHeadset.STATE_CONNECTED ? "CONNECTED"
						: state == BluetoothHeadset.STATE_CONNECTING ? "CONNECTING"
						: state == BluetoothHeadset.STATE_DISCONNECTED ? "DISCONNECTED"
						: state == BluetoothHeadset.STATE_DISCONNECTING ? "DISCONNECTING"
						: "Unknown";
				String prevStateStr = prevState == BluetoothHeadset.STATE_CONNECTED ? "CONNECTED"
						: prevState == BluetoothHeadset.STATE_CONNECTING ? "CONNECTING"
						: prevState == BluetoothHeadset.STATE_DISCONNECTED ? "DISCONNECTED"
						: prevState == BluetoothHeadset.STATE_DISCONNECTING ? "DISCONNECTING"
						: "Unknown";
				//Log.d(TAG, "BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED: EXTRA_DEVICE=" + device.getName() + " EXTRA_STATE=" + stateStr + " EXTRA_PREVIOUS_STATE=" + prevStateStr);
				Toast.makeText(getApplicationContext(), "BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED: EXTRA_DEVICE=" + device.getName() + " EXTRA_STATE=" + stateStr + " EXTRA_PREVIOUS_STATE=" + prevStateStr , 
						   Toast.LENGTH_SHORT).show();
			} else if (action.equals(BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT)) {
				String cmd = extras.getString(BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD);
				int type = extras.getInt(BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD_TYPE);
				String[] args = extras.getStringArray(BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_ARGS);
				String typeStr = type == BluetoothHeadset.AT_CMD_TYPE_READ ? "AT_CMD_TYPE_READ"
					: type == BluetoothHeadset.AT_CMD_TYPE_TEST ? "AT_CMD_TYPE_TEST"
					: type == BluetoothHeadset.AT_CMD_TYPE_SET ? "AT_CMD_TYPE_SET"
					: type == BluetoothHeadset.AT_CMD_TYPE_BASIC ? "AT_CMD_TYPE_BASIC"
					: type == BluetoothHeadset.AT_CMD_TYPE_ACTION ? "AT_CMD_TYPE_ACTION"
					: "Unknown";
				String log = "BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT:";
				log += " CMD=" + cmd;
				log += " type=" + typeStr;
				log += " args=";
				for (int i = 0; i < args.length; i++) {
					log += args[i];
					if (i != args.length - 1) {
						log += ",";
					}
				}
				//Log.d(TAG, log);
				Toast.makeText(getApplicationContext(), log, 
						Toast.LENGTH_SHORT).show();
			}
		}
	};
	
	private BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
		@Override
		public void onServiceConnected(int profile, BluetoothProfile proxy) {
			String kind = null;
			if (profile == BluetoothProfile.HEADSET) {
				mBluetoothHeadset = (BluetoothHeadset) proxy;
				kind = "Headset";
			}
				
			List<BluetoothDevice> devices = proxy.getConnectedDevices();
			String[] names = new String[devices.size()];
			for (int i = 0; i < names.length; i++) {
				BluetoothDevice device = devices.get(i);
				names[i] = kind + "\n" + device.getName() + "\n" + device.getAddress();
				Toast.makeText(getApplicationContext(), kind + "\n" + device.getName() + "\n" + device.getAddress(),
						Toast.LENGTH_SHORT).show();
			}
			mConnectedDevices.addAll(names);
		}
		@Override
		public void onServiceDisconnected(int profile) {
			List<String> names = new LinkedList<String>();
			for (int i = 0; i < mConnectedDevices.getCount(); i++) {
				String name = mConnectedDevices.getItem(i);
				if (profile == BluetoothProfile.HEADSET && name.startsWith("Headset")) {
					names.add(name);
				}
			}
			for (String name : names) {
				mConnectedDevices.remove(name);
			}
			if (profile == BluetoothProfile.HEADSET) {
				mBluetoothHeadset = null;
			}
			Toast.makeText(getApplicationContext(), "Service disconnected",
					Toast.LENGTH_SHORT).show();
		}
	};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mConnectedDevices = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

		mList = (ListView) findViewById(R.id.connected);
		mList.setAdapter(mConnectedDevices);
		mList.setOnItemClickListener(this);
    }
    
	@Override
	protected void onResume() {
		super.onResume();
		if (!mBluetoothAdapter.isEnabled()) {
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intent, REQUEST_ENABLE_BT);
		}
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
		filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
		filter.addAction(BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT);
		registerReceiver(mReceiver, filter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
		closeProfiles();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.menu_get_profiles) {
			getProfiles();
		} else if (itemId == R.id.menu_close_profiles) {
			closeProfiles();
		}
		return true;
	}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		String text = ((TextView) view).getText().toString();
		String kind= text.substring(0, text.indexOf("\n"));
		String addr = text.substring(text.lastIndexOf("\n") + 1);
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(addr);
		if (kind.equals("Headset")) {
			boolean isAudioConnected = mBluetoothHeadset.isAudioConnected(device);
			if (mVoicerecognizing.contains(device)) {
				mVoicerecognizing.remove(device);
				boolean result = mBluetoothHeadset.stopVoiceRecognition(device);
				Toast.makeText(this, "isAudioConnected:" + isAudioConnected + " stopVoiceRecognition:" + result, Toast.LENGTH_LONG).show();
			} else {
				boolean result = mBluetoothHeadset.startVoiceRecognition(device);
				if (result) {
					mVoicerecognizing.add(device);
				}
				Toast.makeText(this, "isAudioConnected:" + isAudioConnected + " startVoiceRecognition:" + result, Toast.LENGTH_LONG).show();
			}
		} else {
			Toast.makeText(this, "Unsupported on this application", Toast.LENGTH_LONG).show();
		}
	}

	
	private void getProfiles() {
		mBluetoothAdapter.getProfileProxy(this, mProfileListener, BluetoothProfile.HEADSET);
	}
	
	private void closeProfiles() {
		if (mBluetoothHeadset != null) {
			mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mBluetoothHeadset);
			mBluetoothHeadset = null;
		}
		mConnectedDevices.clear();
	}
}

package x1opya.com.devicemonitoring;

import android.Manifest;
import android.app.Activity;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static x1opya.com.devicemonitoring.FieldsName.*;

public class JobConnectionChecker extends JobService {
    private static final String TAG = "JobConnectionChecker";
    private AsyncJob asinc;

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        asinc = new AsyncJob() {
            @Override
            protected void onPostExecute(String s) {
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                jobFinished(jobParameters, false);
            }
        };
        asinc.execute();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        asinc.cancel(true);
        return false;
    }

    public class AsyncJob extends AsyncTask<Void, Void, String> {

        DeviceInfoModel deviceInfo;

        @Override
        protected String doInBackground(Void... voids) {
            deviceInfo = new DeviceInfoModel();
            checkConnections();
            return "Работает...";
        }

        private void checkConnections() {
            netChecker();
            wifiChecker();
            bluetoothChecker();
            gpsChecker();
            comitData();
        }

        private void wifiChecker() {
            WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            if (wifi.isWifiEnabled()) {
                deviceInfo.isWifiEnable = true;
                if (wifi.getConnectionInfo().getSSID() != "0x") //выдает когда не подключен к сети wifi
                    deviceInfo.wifiConnectedTo = wifi.getConnectionInfo().getSSID();
                wifi.startScan();
                for (ScanResult c : wifi.getScanResults()) {
                    deviceInfo.wifiList.add(c.SSID);
                }
                Log.println(Log.ASSERT, TAG, "wifi enable " + deviceInfo.wifiConnectedTo);
                Log.println(Log.ASSERT, TAG, "count wifi  connections " + deviceInfo.wifiList.size());
            } else {
                deviceInfo.isWifiEnable = false;
                Log.println(Log.ASSERT, TAG, "wifi disable");
            }
        }

        private void netChecker() {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (!info.isConnected()) deviceInfo.isNetEnable = false;
            else {
                deviceInfo.netConnectedTo = info.getSubtypeName();
                deviceInfo.isNetEnable = true;
                Log.println(Log.ASSERT, TAG, info.getSubtypeName());
            }
        }

        private void bluetoothChecker() {
            BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
            BluetoothAdapter adapter = manager.getAdapter();
            BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
            if (adapter.isEnabled()) {
                deviceInfo.isBluetoothEnable = true;
                scanner.startScan(new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, android.bluetooth.le.ScanResult result) {
                        super.onScanResult(callbackType, result);
                    }
                });
                Log.println(Log.ASSERT, TAG, "bluetooth enabled");
                Log.println(Log.ASSERT, TAG, deviceInfo.bluetoothList.size() + "");
            } else {
                deviceInfo.isBluetoothEnable = false;
                Log.println(Log.ASSERT, TAG, "bluetooth disabled");
            }
        }

        private void gpsChecker() {
            FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(getApplicationContext());

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                deviceInfo.gps = "permission not granted";
                deviceInfo.isGpsEnable = false;
                return;
            }
            client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location!=null) {
                        deviceInfo.isGpsEnable = true;
                        deviceInfo.gps = location.getLongitude() + "|" + location.getLatitude();
                        Log.println(Log.ASSERT, TAG, "gps " + deviceInfo.gps);
                    }else{
                        deviceInfo.isGpsEnable = false;
                        Log.println(Log.ASSERT, TAG, "gps not found" );
                    }
                }
            });
        }

        private void comitData(){
            SharedPreferences s = getApplicationContext().getSharedPreferences("options",Context.MODE_PRIVATE);
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Calendar now = Calendar.getInstance();
            String strDate = formater.format(now.getTime());
            String deviceId = Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
            DatabaseReference myRef = database.getReference("Devices").child(deviceId);
            String deviceName =  Build.MODEL;
            String firstComit = s.getString("isFirst","y");
            Gson gson = new Gson();
            Map<String,Object> info = new HashMap<>();
            info.put("lastUpdate",strDate);
            info.put("deviceModel",deviceName);
            info.put(IS_NET_ENABLE,deviceInfo.isNetEnable+"");
            info.put(IS_BLUETOOTH_ENABLE,deviceInfo.isBluetoothEnable+"");
            info.put(IS_WIFI_ENABLE,deviceInfo.isWifiEnable+"");
            info.put(IS_GPS_ENABLE,deviceInfo.isGpsEnable+"");
            if(firstComit.equals("y")||!deviceInfo.netConnectedTo.equals("") ) info.put(NET_CONNECTED_TO,deviceInfo.netConnectedTo+"|"+strDate);
            if(firstComit.equals("y")||!deviceInfo.wifiConnectedTo.equals("")) info.put(WIFI_CONNECTED_TO,deviceInfo.wifiConnectedTo+"|"+strDate);
            if(firstComit.equals("y")||!deviceInfo.gps.equals("")) info.put(GPS,deviceInfo.gps+"|"+strDate);
            if(firstComit.equals("y")||deviceInfo.wifiList.size()!=0) info.put(WIFI_LIST,gson.toJson(deviceInfo.wifiList)+"|"+strDate);
            //if(deviceInfo.bluetoothList.size()!=0) info.put(BLUETOOTH_LIST,gson.toJson(deviceInfo.bluetoothList)+"|"+strDate);
            if(firstComit.equals("y")) {
                s.edit().putString("isFirst","n").apply();
                myRef.setValue(info);
            }else{
                myRef.updateChildren(info);
            }

        }
    }
}

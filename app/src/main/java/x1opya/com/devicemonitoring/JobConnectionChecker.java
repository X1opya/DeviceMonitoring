package x1opya.com.devicemonitoring;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class JobConnectionChecker extends JobService {
    private static final String TAG ="JobConnectionChecker" ;
    private AsyncJob asinc;

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        asinc = new AsyncJob(){
            @Override
            protected void onPostExecute(String s) {
                Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
                jobFinished(jobParameters,false);
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

    public class AsyncJob extends AsyncTask<Void,Void,String> {

        DeviceInfoModel deviceInfo;
        
        @Override
        protected String doInBackground(Void... voids) {
            deviceInfo = new DeviceInfoModel();
            checkConnection();
            return "Работает...";
        }

        private void checkConnection(){
            ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getActiveNetworkInfo();
            WifiManager wifi = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
            BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();

            if(info==null)  deviceInfo.isNetEnable = false;
            else {
                deviceInfo.netConnectedTo = info.getSubtypeName();
                deviceInfo.isNetEnable = true;
                Log.println(Log.ASSERT,TAG,info.getSubtypeName());
            }
            if(wifi.isWifiEnabled()){
                deviceInfo.isWifiEnable=true;
                if(wifi.getConnectionInfo().getSSID()!="0x") //выдает когда не подключен к сети wifi
                deviceInfo.wifiConnectedTo=wifi.getConnectionInfo().getSSID();
                wifi.startScan();
                for (ScanResult c:wifi.getScanResults()) {
                    deviceInfo.wifiList.add(c.SSID);
                }
                Log.println(Log.ASSERT,TAG,"wifi enable "+deviceInfo.wifiConnectedTo);
            }else{
                deviceInfo.isWifiEnable=false;
                Log.println(Log.ASSERT,TAG,"wifi disable");
            }
            if(bAdapter.isEnabled()){
                deviceInfo.isBluetoothEnable=true;
                for (BluetoothDevice d:bAdapter.getBondedDevices()) {
                    deviceInfo.bluetoothList.add(d.getName());
                }
                Log.println(Log.ASSERT,TAG,"bluetooth enabled");
                Log.println(Log.ASSERT,TAG,deviceInfo.bluetoothList.size()+"");
            }else {
                deviceInfo.isBluetoothEnable=false;
                Log.println(Log.ASSERT,TAG,"bluetooth disabled");
            }
        }
        
        
    }
}

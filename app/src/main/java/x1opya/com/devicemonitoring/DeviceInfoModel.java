package x1opya.com.devicemonitoring;

import java.util.ArrayList;
import java.util.List;

public class DeviceInfoModel {
    public boolean isNetEnable;
    public boolean isWifiEnable;
    public boolean isBluetoothEnable;
    public String wifiConnectedTo;
    public String netConnectedTo;
    public List<String> bluetoothList;
    public List<String> wifiList;
    public String gps;

    public DeviceInfoModel() {
        bluetoothList = new ArrayList<>();
        wifiList = new ArrayList<>();
        wifiConnectedTo="";
        gps="";
        netConnectedTo="";
        isNetEnable=false;
        isWifiEnable=false;
        isBluetoothEnable=false;
    }
}

package x1opya.com.devicemonitoring;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.List;

import static x1opya.com.devicemonitoring.FieldsName.*;

abstract class FirebaseReceiver extends BroadcastReceiver {
    protected FirebaseDatabase database;
    protected DatabaseReference myRef;
    protected Gson gson;
    protected String deviceName;


    protected FirebaseReceiver() {
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Devices");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        deviceName = context.getSharedPreferences("options",Context.MODE_PRIVATE).getString("name","non");
    }

    protected void commitField(String field, String data){
        myRef.child(deviceName).child(field).push().setValue(data);
    }

    protected void commitList(String field,List<String> data){
        String list = gson.toJson(data);
        myRef.child(deviceName).child(field).push().setValue(list);
    }
}

package x1opya.com.devicemonitoring;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PersistableBundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import static x1opya.com.devicemonitoring.JobConnectionChecker.*;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    Switch swich;
    JobScheduler scheduler;
    EditText etPeriodic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        swich = findViewById(R.id.switch1);
        etPeriodic = findViewById(R.id.editText);
        swich.setOnCheckedChangeListener(this);
        scheduler = (JobScheduler)getSystemService(JOB_SCHEDULER_SERVICE);
        swich.setChecked(isJobRunning());
        etPeriodic.setEnabled(!isJobRunning());
        requestPhoneState();

    }

    private void requestPhoneState(){
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        } else {
            //TODO
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 2:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //TODO
                }
                break;

            default:
                break;
        }
    }

    private boolean isJobRunning(){
        for (JobInfo job: scheduler.getAllPendingJobs()) {
            if(job.getId()==101) return true;
        }
        return false;
    }

    private void runJob(){
        ComponentName service = new ComponentName(this,JobConnectionChecker.class);
        JobInfo info ;
        int periodicTime;
        if(etPeriodic.getText().toString().matches("")) periodicTime = 5000;
        else periodicTime = Integer.parseInt(etPeriodic.getText().toString())*60*1000;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            info = new JobInfo.Builder(101, service)
                    //.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setMinimumLatency(periodicTime)
                    .setPersisted(true)
                    .setExtras(new PersistableBundle()).build();

        } else {
            info = new JobInfo.Builder(101, service)
                    .setPersisted(true)
                    .setPeriodic(periodicTime)//переодичность взять с Edit text

                    .build();
        }
        int resultCode = scheduler.schedule(info);
        if(resultCode == JobScheduler.RESULT_SUCCESS){
        }else{

        }
    }

    public void startJob() {
        runJob();
    }

    public void stopJob() {
        scheduler.cancelAll();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(b){
            startJob();
            compoundButton.setText("On");
        }else{
            stopJob();
            compoundButton.setText("Off");

        }
        etPeriodic.setEnabled(!isJobRunning());
    }
}

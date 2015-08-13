package com.aos.wangzhuo.emergencyresponder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;


public class EmergencyResponder extends ActionBarActivity {

    ReentrantLock lock;
    CheckBox locationCheckBox;
    ArrayList<String> requesters;
    ArrayAdapter<String> aa;

    public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_responder);

        lock = new ReentrantLock();
        requesters = new ArrayList<String>();
        wireUpControls();
    }

    private void wireUpControls() {
        locationCheckBox = (CheckBox) findViewById(R.id.checkboxSendLocation);
        ListView myListView = (ListView) findViewById(R.id.myListView);

        int layoutID = android.R.layout.simple_list_item_1;
        aa = new ArrayAdapter<String>(this, layoutID, requesters);
        myListView.setAdapter(aa);

        Button okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                respond(true, locationCheckBox.isChecked());
            }
        });

        Button notOkButton = (Button) findViewById(R.id.notOkButton);
        notOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                respond(false, locationCheckBox.isChecked());
            }
        });

        Button autoResponderButton = (Button) findViewById(R.id.autoResponder);
        autoResponderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAutoResponder();
            }
        });
    }

    public void respond(boolean ok, boolean includeLocation) {

    }

    private void startAutoResponder() {

    }

    BroadcastReceiver emergencyResponseRequestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(SMS_RECEIVED)) {
                String queryString = getString(R.string.querystring).toLowerCase();

                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    SmsMessage[] messages = new SmsMessage[pdus.length];
                    for (int i = 0; i < pdus.length; i++)
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);

                    for (SmsMessage message : messages) {
                        if (message.getMessageBody().toLowerCase().contains(queryString))
                            requestReceived(message.getOriginatingAddress());
                    }
                }
            }
        }
    };

    public void requestReceived(String from) {
        if (!requesters.contains(from)) {
            lock.lock();
            requesters.add(from);
            aa.notifyDataSetChanged();
            lock.unlock();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(SMS_RECEIVED);
        registerReceiver(emergencyResponseRequestReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(emergencyResponseRequestReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_emergency_responder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

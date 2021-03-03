package com.example.wissdom.nfcdemo;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.wissdom.nfc0603.R;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private int sysVersion;
    private PendingIntent pendingIntent;
    private NfcAdapter nfcAdapter;
    public static String[][] TECHLISTS;
    public static IntentFilter[] FILTERS;
    static {
        try {
            TECHLISTS = new String[][] { { IsoDep.class.getName() },{ NfcV.class.getName() }, { NfcF.class.getName() }, };
            FILTERS = new IntentFilter[] { new IntentFilter(
                    NfcAdapter.ACTION_TECH_DISCOVERED, "*/*") };
        } catch (Exception e) {
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
//区分系统版本
        sysVersion = Integer.parseInt(Build.VERSION.SDK);
        if (sysVersion < 19) {
            onNewIntent(getIntent());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
            nfcAdapter.disableReaderMode(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //获取数据
        final Parcelable p = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Log.d("NFCTAG", intent.getAction());
        if (p != null) {
            Log.d(TAG, "onNewIntent: " + p);
        }
//        board.setText((p != null) ? CardReader.load(p) : null);
        if (p == null) {
//            board.setText("the intent=="+intent);
        } else {
            //board.setText("the intent !=null");
        }
    }
    //4.4以上系统，在这个页面，多次发现标签，onresume只执行一次，4.4以下的会执行多次，但是onNewIntent()和enableReaderMode()都能够执行多次
    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null){
            Log.e(TAG, "onResume: "+nfcAdapter);
        }
        Log.e("NFC----", IsoDep.class.getName());
    }
    @Override
    protected void onStart() {
        super.onStart();
        nfcAdapter= NfcAdapter.getDefaultAdapter(this);//设备的NfcAdapter对象
        if(nfcAdapter==null){//判断设备是否支持NFC功能
            Toast.makeText(this,"设备不支持NFC功能!",Toast.LENGTH_SHORT);
            finish();
            return;
        }
        if (!nfcAdapter.isEnabled()){//判断设备NFC功能是否打开
            Toast.makeText(this,"请到系统设置中打开NFC功能!",Toast.LENGTH_SHORT);
            finish();
            return;
        }
        pendingIntent=PendingIntent.getActivity(this,0,new Intent(this,getClass()),0);//创建PendingIntent对象,当检测到一个Tag标签就会执行此Intent
    }
}

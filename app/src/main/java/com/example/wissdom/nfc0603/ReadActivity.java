package com.example.wissdom.nfc0603;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

public class ReadActivity extends Activity {

    private NfcAdapter mAdapter;
    private IntentFilter[] mFilters;
    private PendingIntent mPendingIntent;
    private String[][] mTechLists;
    private NdefMessage[] msgs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        mAdapter = NfcAdapter.getDefaultAdapter(this);

        PackageManager pm = getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_NFC)) { // 不支持
            // 创建dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提示");
            builder.setMessage("您的手机不支持NFC功能");
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
//                    SysApplication.getInstance().exit();
                    finish();
                }
            });
            builder.create().show();
        } else { // 支持NFC
            /*
             *
             * 是否已经开启应用
             */
            if (mAdapter != null && mAdapter.isEnabled()) {
                // 已开启
            } else {
                // 创建dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("提示");
                builder.setMessage("您的NFC功能未开启，是否开启NFC功能");
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // 跳转到NFC设置页面
                        try {
                            startActivity(new Intent(
                                    "android.settings.NFC_SETTINGS"));
                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        finish();
                    }
                });
                builder.create().show();

            }
        }
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        /* NFC意图过滤器 */
        mFilters = new IntentFilter[]{new IntentFilter(
                NfcAdapter.ACTION_TAG_DISCOVERED)};

        /* 支持的NFC技术列表 */
        mTechLists = new String[][]{new String[]{NfcV.class.getName()}};
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters,
                    mTechLists);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            mAdapter.disableForegroundDispatch(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    Intent intent;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        this.intent = intent;

    }

    private void setNFCMsgView(NdefMessage[] ndefMessages) {
        if (ndefMessages == null || ndefMessages.length == 0) {
            return;
        }
        List<ParsedNdefRecord> records = NdefMessageParser.parse(ndefMessages[0]);
        final int size = records.size();
        for (int i = 0; i < size; i++) {
            ParsedNdefRecord record = records.get(i);
            String viewText = record.getViewText();
            Intent intent = new Intent(this, ShowActivity.class);
            intent.putExtra("data", viewText);
            startActivity(intent);
        }
    }

    public void scanNFC(View view) {
        if (null!=this.intent){
            msgs = NfcUtil.getNdefMsg(intent);
            if (msgs == null) {
                Toast.makeText(this, "非NFC启动", Toast.LENGTH_SHORT).show();
            } else {
                setNFCMsgView(msgs);
            }
        }

    }
}

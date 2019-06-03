package com.example.wissdom.nfc0603;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Parcelable;
import android.util.Log;

import java.util.Arrays;

/**
 * TODO:功能说明 Nfc工具类
 *
 * @author: chenqiuyang
 * @date: 2018-07-12 11:33
 */
public class NfcUtil {
    private static final String TAG = "NfcUtil";

    /**
     * 解析 ndefRecord 文本数据
     *
     * @param ndefRecord
     * @return
     */
    public static String parse(NdefRecord ndefRecord) {
        // verify tnf   得到TNF的值
        if (ndefRecord.getTnf() != NdefRecord.TNF_WELL_KNOWN) {
            return null;
        }

        // 得到字节数组进行判断
        if (!Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
            return null;
        }

        try {
            // 获得一个字节流
            byte[] payload = ndefRecord.getPayload();
            // payload[0]取第一个字节。 0x80：十六进制（最高位是1剩下全是0）
            String textEncoding = ((payload[0] & 0x80) == 0) ? "UTF-8"
                    : "UTF-16";
            // 获得语言编码长度
            int languageCodeLength = payload[0] & 0x3f;
            // 获得语言编码
            String languageCode = new String(payload, 1, languageCodeLength,
                    "US-ASCII");
            //
            String text = new String(payload, languageCodeLength + 1,
                    payload.length - languageCodeLength - 1, textEncoding);

            return text;
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }


    //初次判断是什么类型的NFC卡
    public static NdefMessage[] getNdefMsg(Intent intent) {
        if (intent == null) {
            return null;
        }

        //nfc卡支持的格式
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        byte[] id1 = tag.getId();
        // 字符序列转换为16进制字符串
        String strId = bytesToHexString(id1);
        Log.e("strId", strId + "");

        String[] temp = tag.getTechList();
        for (String s : temp) {
            Log.i(TAG, "resolveIntent tag: " + s);
        }


        String action = intent.getAction();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action) ||
                NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) ||
                NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            Parcelable[] rawMessage = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] ndefMessages;

            // 判断是哪种类型的数据 默认为NDEF格式
            if (rawMessage != null) {
                Log.i(TAG, "getNdefMsg: ndef格式 ");
                ndefMessages = new NdefMessage[rawMessage.length];
                for (int i = 0; i < rawMessage.length; i++) {
                    ndefMessages[i] = (NdefMessage) rawMessage[i];
                    //toString== NdefRecord tnf=1 type=54 id=3734323130
                    // payload=02656E352C3433334D0D0A312C36302C31302C36300D0A333034372C300D0A31302E3
//          12C35352E300D0A32372E312C35352E300D0A32372E322C35352E300D0A32372E332C3
//          5352E300D0A32372E342C35352E300D0A32372E352C35352E300D0A32372E362C35352E300D0A3237
//          2E372C35352E300D0A32372E382C35352E300D0A32372E392C35352E300D0A
                    //toString== NdefRecord tnf=1 type=54 id=313830313230 payload=02656E352C322E34470D0A312C332C312C330D0A333532362C300D0A32372E382C34302E380D0A32372E372C34312E310D0A32372E372C34322E310D0A32372E382C34342E300D0A32372E382C34352E350D0A32372E372C34372E300D0A32372E382C34322E330D0A32372E372C34302E310D0A32372E362C34302E340D0A32372E382C34312E370D0A
                    Log.e(TAG, "getNdefMsg:toString== " + ndefMessages[i].getRecords()[i].toString());
                }
            } else {
                //未知类型
                Log.i(TAG, "getNdefMsg: 未知类型");
                byte[] empty = new byte[0];
                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                Parcelable parcelable = intent
                        .getParcelableExtra(NfcAdapter.EXTRA_TAG);
                byte[] payload = dumpTagData(parcelable).getBytes();
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN,
                        empty, id, payload);
                NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
                ndefMessages = new NdefMessage[]{msg};
            }


            return ndefMessages;
        }

        return null;
    }

    private static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();

        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.toUpperCase(Character.forDigit(
                    (src[i] >>> 4) & 0x0F, 16));
            buffer[1] = Character.toUpperCase(Character.forDigit(src[i] & 0x0F,
                    16));
            System.out.println(buffer);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }

    //一般公家卡，扫描的信息
    private static String dumpTagData(Parcelable p) {
        StringBuilder sb = new StringBuilder();
        Tag tag = (Tag) p;
        byte[] id = tag.getId();
        sb.append("Tag ID (hex): ").append(getHex(id)).append("\n");
        sb.append("Tag ID (dec): ").append(getDec(id)).append("\n");
        sb.append("ID (reversed): ").append(getReversed(id)).append("\n");

        String prefix = "android.nfc.tech.";
        sb.append("Technologies: ");
        for (String tech : tag.getTechList()) {
            sb.append(tech.substring(prefix.length()));
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        for (String tech : tag.getTechList()) {
            if (tech.equals(MifareClassic.class.getName())) {
                sb.append('\n');
                MifareClassic mifareTag = MifareClassic.get(tag);
                String type = "Unknown";
                switch (mifareTag.getType()) {
                    case MifareClassic.TYPE_CLASSIC:
                        type = "Classic";
                        break;
                    case MifareClassic.TYPE_PLUS:
                        type = "Plus";
                        break;
                    case MifareClassic.TYPE_PRO:
                        type = "Pro";
                        break;
                    default:
                        break;
                }
                sb.append("Mifare Classic type: ");
                sb.append(type);
                sb.append('\n');

                sb.append("Mifare size: ");
                sb.append(mifareTag.getSize() + " bytes");
                sb.append('\n');

                sb.append("Mifare sectors: ");
                sb.append(mifareTag.getSectorCount());
                sb.append('\n');

                sb.append("Mifare blocks: ");
                sb.append(mifareTag.getBlockCount());
            }

            if (tech.equals(MifareUltralight.class.getName())) {
                sb.append('\n');
                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
                String type = "Unknown";
                switch (mifareUlTag.getType()) {
                    case MifareUltralight.TYPE_ULTRALIGHT:
                        type = "Ultralight";
                        break;
                    case MifareUltralight.TYPE_ULTRALIGHT_C:
                        type = "Ultralight C";
                        break;
                    default:
                        break;
                }
                sb.append("Mifare Ultralight type: ");
                sb.append(type);
            }
        }
        return sb.toString();
    }


    private static String getHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private static long getDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = 0; i < bytes.length; ++i) {
            long value = bytes[i] & 0xffL;
            result += value * factor;
            factor *= 256L;
        }
        return result;
    }

    private static long getReversed(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = bytes.length - 1; i >= 0; --i) {
            long value = bytes[i] & 0xffL;
            result += value * factor;
            factor *= 256L;
        }
        return result;
    }

}

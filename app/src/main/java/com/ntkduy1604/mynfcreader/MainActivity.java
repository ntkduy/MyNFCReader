package com.ntkduy1604.mynfcreader;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView mTextView;     //Global mTextView
    private NfcAdapter mAdapter;    //Global mAdapter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //TO-DO:
        //Inquiry whether device supports NFC?
        mAdapter = NfcAdapter.getDefaultAdapter(this);
        mTextView = (TextView) findViewById(R.id.explanation);
        //Device doesn't support NFC
        if (mAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        //Device supports NFC, but check whether NFC is turned on
        if (!mAdapter.isEnabled()) {
            mTextView.setText(R.string.nfc_disabled);
        } else {
            mTextView.setText(R.string.nfc_enable);
        }
        //Main task
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        // TODO: handle Intent
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            } else {
                Parcelable tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                dumpTagData(tag);
            }
        }
    }

    private void dumpTagData(Parcelable p) {
        Tag tag = (Tag) p;
        byte[] id           = tag.getId();
        String[] nfcTech = tag.getTechList();

        /**
         * Get the ID of the NFC tag
         */
        TextView nfcTagId = (TextView) findViewById(R.id.tag_id_hex);
        nfcTagId.setText(getHex(id));

        /**
         * Get the technologies of the NFC tag
         */

        TextView nfcTechnologies = (TextView) findViewById(R.id.nfc_technologies);
        nfcTechnologies.setText(getTechnology(nfcTech));

        /**
         * Get Mifare data of the NFC tag
         */

        String[] nfcMifare = tag.getTechList();
        MifareClassic mifareTag = MifareClassic.get(tag);
        getMifareData(nfcMifare, mifareTag);
    }

    private String getHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private String getTechnology(String[] nfcTech) {
        StringBuilder sb = new StringBuilder();
        String prefix = "android.nfc.tech.";
        for (String tech : nfcTech) {
            sb.append(tech.substring(prefix.length()));
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }

    private void getMifareData(String[] mifareData, MifareClassic mifareTag) {
        for (String tech : mifareData) {
            if (tech.equals(MifareClassic.class.getName())) {
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
                }
                /**
                 * Set MIFARE type
                 */
                TextView nfcMifareType = (TextView) findViewById(R.id.nfc_mifire_type);
                nfcMifareType.setText(type);
                /**
                 * Set MIFARE size
                 */
                TextView nfcMifareSize = (TextView) findViewById(R.id.nfc_mifire_size);
                type = mifareTag.getSize() + " bytes";
                nfcMifareSize.setText(type);
                /**
                 * Set MIFARE sector
                 */
                TextView nfcMifareSector = (TextView) findViewById(R.id.nfc_mifire_sector);
                nfcMifareSector.setText(String.valueOf(mifareTag.getSectorCount()));
                /**
                 * Set MIFARE block
                 */
                TextView nfcMifareBlock = (TextView) findViewById(R.id.nfc_mifire_block);
                nfcMifareBlock.setText(String.valueOf(mifareTag.getBlockCount()));
            }
        }
    }
}

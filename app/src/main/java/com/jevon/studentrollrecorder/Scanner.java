package com.jevon.studentrollrecorder;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.ArrayList;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/*This activity shows shows the actual view finder for scanning barcodes and returns the results to the calling activity*/
public class Scanner extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    private Switch sw_flash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        mScannerView = (ZXingScannerView) findViewById(R.id.scannerView);
        ArrayList<BarcodeFormat> formats = new ArrayList<>();
        formats.add(BarcodeFormat.CODE_39); //set the scanner to read only the format of the student Id
        mScannerView.setFormats(formats);

        sw_flash = (Switch) findViewById(R.id.sw_flash);
        sw_flash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mScannerView.setFlash(true);
                } else {
                    mScannerView.setFlash(false);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
        if(mScannerView.getFlash())
            mScannerView.setFlash(false);
    }

    @Override
    public void handleResult(Result result) {
        //we can create an intent for any activity in case we decide to let multiple activities start the scanner
        ComponentName cn = getCallingActivity();
        if(cn != null){
            Intent i = new Intent(Scanner.this, cn.getClass());
            i.putExtra("results", result.getText());
            setResult(RESULT_OK, i);
        }
        finish();
    }
}

package com.horanet.BarbeBLE;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 2;
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (savedInstanceState == null) {
            initBT();
        }

    }


    //-----------------------------------------------------
    // DIALOGALERT
    //-----------------------------------------------------

    public void DialogAlert(int Message, String Title)
    {
        // Username or password false, display and an error
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage(Message);
        dlgAlert.setTitle(Title);
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();

        dlgAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) { }
        });
    }




    //-----------------------------------------------------
    // VERIFICATION OF COMPATIBILITY AND BLUETOOTH
    //-----------------------------------------------------


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        switch (requestCode) {

            case PERMISSION_REQUEST_COARSE_LOCATION:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    DialogAlert(R.string.bt_not_permit_coarse, "Error");
                } else {
                    NextActivity();
                }
                break;

        }
    }


    private void initBT() {

        BluetoothManager bluetoothService = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE));

        if (bluetoothService != null) {

            mBluetoothAdapter = bluetoothService.getAdapter();

            // Is Bluetooth supported on this device?
            if (mBluetoothAdapter != null) {

                // Is Bluetooth turned on?
                if (mBluetoothAdapter.isEnabled()) {

                    // Are Bluetooth Advertisements supported on this device? NOTE : some device return false but this is working
                    if (!mBluetoothAdapter.isMultipleAdvertisementSupported()) {
                        //DialogAlert(R.string.bt_ads_not_supported, "Error");
                    }

                    // see https://stackoverflow.com/a/37015725/1869297
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                        } else {
                            // Everything is supported and enabled.
                            NextActivity();
                        }

                    } else {
                        // Everything is supported and enabled.
                        NextActivity();
                    }

                } else {

                    // Prompt user to turn on Bluetooth (logic continues in onActivityResult()).
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            } else {

                // Bluetooth is not supported.
                DialogAlert(R.string.bt_not_supported, "Error");
            }

        }
    }

    public void NextActivity()
    {
        Intent myIntent = new Intent(MainActivity.this, PeripheralRoleActivity.class);
        MainActivity.this.startActivity(myIntent);
    }


}

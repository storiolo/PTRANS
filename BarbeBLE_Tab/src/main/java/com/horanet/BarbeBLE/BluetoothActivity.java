package com.horanet.BarbeBLE;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public abstract class BluetoothActivity extends AppCompatActivity {


    private Toolbar mToolbar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peripheral_role);

    }




    protected void showMsgText(int stringId) {
        showMsgText(getString(stringId));
    }

    protected void showMsgText(String string) {
        // Username or password false, display and an error
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage(string);
        dlgAlert.setTitle("");
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();

        dlgAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) { }
        });
    }

    protected BluetoothAdapter getBluetoothAdapter() {

        BluetoothAdapter bluetoothAdapter;
        BluetoothManager bluetoothService = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE));

        if (bluetoothService != null) {

            bluetoothAdapter = bluetoothService.getAdapter();

            // Is Bluetooth supported on this device?
            if (bluetoothAdapter != null) {

                // Is Bluetooth turned on?
                if (bluetoothAdapter.isEnabled()) {
                    /*
                    all the other Bluetooth initial checks already verified in MainActivity
                     */
                    return bluetoothAdapter;
                }
            }
        }

        return null;
    }


}


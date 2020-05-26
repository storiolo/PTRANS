package com.horanet.BarbeBLE;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;

import java.util.Arrays;
import java.util.HashSet;

import static com.horanet.BarbeBLE.Constants.HORANET_ID_UUID;
import static com.horanet.BarbeBLE.Constants.HORANET_UUID;


public class PeripheralRoleActivity extends BluetoothActivity implements View.OnClickListener {

    private BluetoothGattService mSampleService;
    private BluetoothGattCharacteristic mSampleCharacteristic;

    private BluetoothManager mBluetoothManager;
    private BluetoothGattServer mGattServer;
    private HashSet<BluetoothDevice> mBluetoothDevices;

    private Button mSaveButton;
    private Switch mEnableAdvertisementSwitch;

    private EditText mID, mPassword;
    private boolean isConnected;

    public static final String TAG = "BluetoothLE";

    private int mInterval = 1000;
    private Handler mHandler;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mEnableAdvertisementSwitch = (Switch) findViewById(R.id.advertise_switch);
        mID = (EditText) findViewById(R.id.setID);
        mPassword = (EditText) findViewById(R.id.setPassword);
        mSaveButton = (Button) findViewById(R.id.button_save);


        //get All values
        SharedPreferences settings = this.getSharedPreferences("BarbeBLE", 0);
        final String ID = settings.getString("ID", "");
        final String Password = settings.getString("pass", "");
        mID.setText(ID);
        mPassword.setText(Password);

        mSaveButton.setOnClickListener(this);
        mEnableAdvertisementSwitch.setOnClickListener(this);

        setGattServer();
        setBluetoothService();

        isConnected = false;
        mHandler = new Handler();
        startRepeatingTask();
    }


    private void checkConnect(){
        if(isConnected) {
            showMsgText("Vous etes connecté");
            isConnected = false;
        }
    }



    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                checkConnect(); //this function can change value of mInterval.
            } finally {
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }





    @Override
    public void onClick(View view) {

        switch(view.getId()) {

            case R.id.advertise_switch:
                Switch switchToggle = (Switch) view;
                if (switchToggle.isChecked()) {
                    startAdvertising();
                } else {
                    stopAdvertising();
                }
                break;


            case R.id.button_save:
                SharedPreferences settings = (PeripheralRoleActivity.this).getSharedPreferences("BarbeBLE", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("ID", mID.getText().toString());
                editor.putString("pass", mPassword.getText().toString());
                editor.apply();
                showMsgText("Settings Saved");
                setCharacteristic();
                notifyCharacteristicChanged();
                break;

        }
    }



    //Starts BLE Advertising by starting
    private void startAdvertising() {
        startService(getServiceIntent(this));
    }


    //Stops BLE Advertising by stopping
    private void stopAdvertising() {
        stopService(getServiceIntent(this));
        mEnableAdvertisementSwitch.setChecked(false);
    }

    private void setGattServer() {

        mBluetoothDevices = new HashSet<>();
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        if (mBluetoothManager != null) {
            mGattServer = mBluetoothManager.openGattServer(this, mGattServerCallback);
        } else {
            showMsgText(R.string.error_unknown);
        }
    }

    private void setBluetoothService() {

        // create the Service
        mSampleService = new BluetoothGattService(HORANET_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);

        /*
        create the Characteristic.
        we need to grant to the Client permission to read (for when the user clicks the "Request Characteristic" button).
        no need for notify permission as this is an action the Server initiate.

        https://developer.android.com/reference/android/bluetooth/BluetoothGattCharacteristic
         */
        mSampleCharacteristic = new BluetoothGattCharacteristic(HORANET_ID_UUID,
                          BluetoothGattCharacteristic.PROPERTY_NOTIFY | BluetoothGattCharacteristic.PROPERTY_READ  | BluetoothGattCharacteristic.PERMISSION_WRITE,
                         BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);


        setCharacteristic();

        // add the Characteristic to the Service
        mSampleService.addCharacteristic(mSampleCharacteristic);

        // add the Service to the Server/Peripheral
        if (mGattServer != null) {
            mGattServer.addService(mSampleService);
        }
    }


    private void setCharacteristic() {
        SharedPreferences settings = this.getSharedPreferences("BarbeBLE", 0);
        final String ID = settings.getString("ID", "");
        final String Password = settings.getString("pass", "");

        mSampleCharacteristic.setValue(ID+Password);
    }

    /*
    send to the client the value of the Characteristic,
    as the user requested to notify.
     */
    private void notifyCharacteristicChanged() {
        /*
        done when the user clicks the notify button in the app.
        indicate - true for indication (acknowledge) and false for notification (un-acknowledge).
         */
        boolean indicate = (mSampleCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) == BluetoothGattCharacteristic.PROPERTY_INDICATE;

        for (BluetoothDevice device : mBluetoothDevices) {
            if (mGattServer != null) {
                mGattServer.notifyCharacteristicChanged(device, mSampleCharacteristic, indicate);
            }
        }
    }


    private Intent getServiceIntent(Context context) {
        return new Intent(context, PeripheralAdvertiseService.class);
    }


    private final BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {

        @Override
        public void onConnectionStateChange(BluetoothDevice device, final int status, int newState) {

            super.onConnectionStateChange(device, status, newState);

            String msg;

            if (status == BluetoothGatt.GATT_SUCCESS) {

                if (newState == BluetoothGatt.STATE_CONNECTED) {

                    mBluetoothDevices.add(device);

                    msg = "Connected to device: " + device.getAddress();
                    Log.v(PeripheralRoleActivity.TAG, msg);
                    showMsgText(msg);
                    notifyCharacteristicChanged();

                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {

                    mBluetoothDevices.remove(device);

                    msg = "Disconnected from device";
                    Log.v(PeripheralRoleActivity.TAG, msg);
                    showMsgText(msg);
                }

            } else {
                mBluetoothDevices.remove(device);

                msg = getString(R.string.status_error_when_connecting) + ": " + status;
                Log.e(PeripheralRoleActivity.TAG, msg);
                showMsgText(msg);

            }
        }


        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
            Log.v(PeripheralRoleActivity.TAG, "Notification sent. Status: " + status);
        }


        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {

            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);

            if (mGattServer == null) {
                return;
            }

            isConnected = true;
            Log.v(PeripheralRoleActivity.TAG, "Characteristic read request");

            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
        }

    };








}

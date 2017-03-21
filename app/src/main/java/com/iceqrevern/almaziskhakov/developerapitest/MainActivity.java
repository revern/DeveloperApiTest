package com.iceqrevern.almaziskhakov.developerapitest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {

    private static final String PURCHASE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmMU" +
        "gdzE4h1AMqkEtGzs4M01X4gB/aosbekVOQ6i4Mq9/y5lUGx6qNJekQ20Oe+1Fi6RjBy4gzp+Le+X+7rt7PzwjW" +
        "hfsnJMqpFJ5DdJTtUeBqTp406vZavRe2+tUwJvTrymo9RRQzTPDX/9P3AA/DKlm7Xnmhp4VpTIwbcRK2tUT2dY" +
        "YOS+nWc2QYWWCcFOKam6o84D0/O1wAW1ePHK/++W2fjVVogmo3MuddzvfHZs0RdsHxT1+aq7hib3ATXf4GjQz/" +
        "zx9MRNvi6pMe8WoWOkVebpD86A2053BXMtzv20g4sl7ossQjwYmRy8Ni/Cgy+PDfBJet9xN4isysXYMSwIDAQAB";

    TextView uiTapMe;

    private BillingProcessor     bp;
    private IInAppBillingService mBillingService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBillingService = IInAppBillingService.Stub.asInterface(service);
            checkSubscriptionFromBillingService();
            Log.d("work_check", "billing service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBillingService = null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uiTapMe = (TextView) findViewById(R.id.tap_me);
        uiTapMe.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onClickTapMe();
            }
        });

        boolean isAvailable = BillingProcessor.isIabServiceAvailable(this);
        Log.d("work_check", "try create bp");
        if (isAvailable) {
            bp = new BillingProcessor(this, PURCHASE_KEY, this);
            Log.d("work_check", "bp created");
        } else {
            // TODO device what to when user has no google play services
        }
        initPayments();
    }

    private void onClickTapMe() {
        subscribe();
    }

    public boolean subscribe() {
        Log.d("work_check", "subscribe");
        return bp.subscribe(this, "dev_api_sub");
    }

    private boolean checkSubscriptionFromBillingService() {
        Log.d("work_check", "checkSubs");
        try {
            Bundle skuDetails = mBillingService.getPurchases(3, getPackageName(),
                "subs", null);
            int response = skuDetails.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> purchaseDataList =
                    skuDetails.getStringArrayList("INAPP_PURCHASE_DATA_LIST");

                if (purchaseDataList != null && purchaseDataList.size() > 0) {
                    Log.d("subs_api", purchaseDataList.toString());
                } else {
                    Log.d("subs_api", "no subscriptions");
                }

            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void initPayments() {
        Intent serviceIntent =
            new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");

        if (!getPackageManager().queryIntentServices(serviceIntent, 0).isEmpty()) {
            bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
            Log.d("work_check", "bind payment service");
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override public void onProductPurchased(String productId, TransactionDetails details) {
        Toast.makeText(this, "PRODUCT PURCHASED: " + productId, Toast.LENGTH_LONG).show();

    }

    @Override public void onPurchaseHistoryRestored() {

    }

    @Override public void onBillingError(int errorCode, Throwable error) {

    }

    @Override public void onBillingInitialized() {

    }
}

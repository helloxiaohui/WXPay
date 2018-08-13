package net.sourceforge.simcpux;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.ypcang.thirdbuy.R;

public class PayActivity4 extends Activity {

    private IWXAPI api;
    private String TAG = "PayActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_test);

        findViewById(R.id.btnStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PayReq req = new PayReq();
                EditText btnAPPID = findViewById(R.id.btnAPPID);
                btnAPPID.setText("13");
                req.appId = btnAPPID.getText().toString();

                EditText btnPartnerId = findViewById(R.id.btnPartnerId);
                req.partnerId = btnPartnerId.getText().toString();
/////////
                EditText btnPrepayId = findViewById(R.id.btnPrepayId);
                req.prepayId = btnPrepayId.getText().toString();

                EditText btnNonceStr = findViewById(R.id.btnNonceStr);
                req.nonceStr = btnNonceStr.getText().toString();

                EditText btnTimeStamp = findViewById(R.id.btnTimeStamp);
                req.timeStamp = btnTimeStamp.getText().toString();

                EditText btnPackageValue = findViewById(R.id.btnPackageValue);
                req.packageValue = btnPackageValue.getText().toString();

                EditText btnSign = findViewById(R.id.btnSign);
                req.sign = btnSign.getText().toString();
                api = WXAPIFactory.createWXAPI(PayActivity4.this, req.appId);
                api.sendReq(req);
            }
        });


    }

}

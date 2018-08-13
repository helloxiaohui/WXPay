package net.sourceforge.simcpux.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import net.sourceforge.simcpux.Constants;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MyGetAccessTokenTask extends AsyncTask<Void, Void, Map<String, String>> {
    private static final String TAG = "GetAccessTokenTask";
    private Context context;
    private ProgressDialog dialog;

    public MyGetAccessTokenTask(Context context, TaskResult taskResult) {
        this.context = context;
        this.taskResult = taskResult;
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(context, "提示", "正在获取access token...");
    }

    @Override
    protected Map<String, String> doInBackground(Void... voids) {
        String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
        Log.e(TAG, "get access token, url = " + url);
        String entity = genProductArgs(context);
        byte[] buf = WechatUtil.httpPost(url, entity);
        if (buf == null || buf.length == 0) {
            return new HashMap<>();
        }
        String content = new String(buf);
        Map<String, String> xml = decodeXml(content);
        //result.parseFrom(content);
        return xml;
    }

    public Map<String, String> decodeXml(String content) {

        try {
            Map<String, String> xml = new HashMap<>();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(content));
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {

                String nodeName = parser.getName();
                switch (event) {
                    case XmlPullParser.START_DOCUMENT:

                        break;
                    case XmlPullParser.START_TAG:

                        if ("xml".equals(nodeName) == false) {
                            //实例化student对象
                            xml.put(nodeName, parser.nextText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                event = parser.next();
            }

            return xml;
        } catch (Exception e) {
            Log.e("orion", e.toString());
        }
        return null;

    }

    @Override
    protected void onPostExecute(Map<String, String> result) {
        if (dialog != null) {
            dialog.dismiss();
        }
        genPayReq(result);
       /* Log.d(TAG, "RetCode=" + result.localRetCode + ", errCode=" + result.errCode + ", errMsg=" + result.errMsg);
        if (result.localRetCode == LocalRetCode.ERR_OK) {
            Toast.makeText(context, "获取access token成功, accessToken = " + result.accessToken, Toast.LENGTH_LONG).show();
            GetPrepayIdTask getPrepayId = new GetPrepayIdTask(context, result.accessToken);
            getPrepayId.execute(goods_info[0], goods_info[1], goods_info[2]);
        } else {
            Toast.makeText(context, "获取access token失败，原因: " + result.localRetCode.name(), Toast.LENGTH_LONG).show();
        }*/
    }

    private IWXAPI mWeixinApi;
    private String SignString = "";

    private void genPayReq(Map<String, String> resultunifiedorder) {
        String prepay_idStr = resultunifiedorder.get("prepay_id");
        if (taskResult != null) {
            if (prepay_idStr == null || prepay_idStr.isEmpty()) {
                prepay_idStr = "";
            }
            taskResult.onResult(prepay_idStr);
        } else {
            PayReq req = new PayReq();
            req.appId = Constants.APP_ID;
            req.partnerId = Constants.MCH_ID;
            req.prepayId = prepay_idStr;
            //req.packageValue = "prepay_id=" + resultunifiedorder.get("prepay_id");
            req.packageValue = "Sign=WXPay";
            req.nonceStr = genNonceStr();
            req.timeStamp = String.valueOf(genTimeStamp());
            List<NameValuePair> signParams = new LinkedList<NameValuePair>();
            signParams.add(new BasicNameValuePair("appid", req.appId));
            signParams.add(new BasicNameValuePair("noncestr", req.nonceStr));
            signParams.add(new BasicNameValuePair("package", req.packageValue));
            signParams.add(new BasicNameValuePair("partnerid", req.partnerId));
            signParams.add(new BasicNameValuePair("prepayid", req.prepayId));
            signParams.add(new BasicNameValuePair("timestamp", req.timeStamp));
            req.sign = WechatUtil2.genPackageSign2(signParams);

            // sb.append("sign\n"+req.sign+"\n\n");
            Log.e("orion", signParams.toString());
            sendPayReq(req);
        }
    }

    private void sendPayReq(PayReq req) {
        mWeixinApi = WXAPIFactory.createWXAPI(context, Constants.APP_ID);
        // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
        mWeixinApi.registerApp(Constants.APP_ID);
        Log.d(TAG, "mWeixinApi.sendReq");
        mWeixinApi.sendReq(req);

    }

    private long genTimeStamp() {
        return System.currentTimeMillis() / 1000;
    }

    //获取到当前的ip：
    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
        }
        return null;

    }

    private String getWifiIp(Context mContext) {
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = intToIp(ipAddress);
        return ip;
    }

    private String intToIp(int i) {

        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    //随机订单号生成 test 你们可根据自己生成随机数
    private String genOutTradNo() {
        Random random = new Random();
        return MD5.getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
    }

    //签名工具
    private String genAppSign(List<NameValuePair> params) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < params.size(); i++) {
            sb.append(params.get(i).getName());
            sb.append('=');
            sb.append(params.get(i).getValue());
            sb.append('&');
        }
        sb.append("key=");
        sb.append(Constants.APP_KEY);

        sb.append("sign str\n" + sb.toString() + "\n\n");
        String appSign = MD5.getMessageDigest(sb.toString().getBytes());
        Log.e("orion", appSign);
        return appSign;
    }

    private String genProductArgs(Context mContext) {
        String ip = getWifiIp(mContext);
        if (ip == "" && ip == "") {
            ip = getLocalIpAddress();
        }
        try {
            String nonceStr = genNonceStr();
            List<NameValuePair> packageParams = new LinkedList<>();
            packageParams.add(new BasicNameValuePair("appid", Constants.APP_ID));
            packageParams.add(new BasicNameValuePair("body", "APP支付测试"));
            packageParams.add(new BasicNameValuePair("mch_id", Constants.MCH_ID));
            packageParams.add(new BasicNameValuePair("nonce_str", nonceStr));
            packageParams.add(new BasicNameValuePair("notify_url", Constants.NOTIFY_URL));
            packageParams.add(new BasicNameValuePair("out_trade_no", genOutTradNo()));
            packageParams.add(new BasicNameValuePair("spbill_create_ip", ip));
            packageParams.add(new BasicNameValuePair("total_fee", "1"));
            packageParams.add(new BasicNameValuePair("trade_type", "APP"));
            String sign = WechatUtil2.genPackageSign2(packageParams);
            SignString = sign;
            packageParams.add(new BasicNameValuePair("sign", sign));
            String xmlstring = toXml(packageParams);
            return xmlstring;

        } catch (Exception e) {
            Log.e("TAG", "fail, ex = " + e.getMessage());
            return null;
        }
    }

    private String genNonceStr() {
        Random random = new Random();
        return MD5.getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
    }

    private String toXml(List<NameValuePair> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("<xml>");
        for (int i = 0; i < params.size(); i++) {
            sb.append("<" + params.get(i).getName() + ">");
            sb.append(params.get(i).getValue());
            sb.append("</" + params.get(i).getName() + ">");
        }
        sb.append("</xml>");
        Log.e("orion", sb.toString());
        return sb.toString();
    }

    public TaskResult taskResult = null;

    public TaskResult getTaskResult() {
        return taskResult;
    }

    public void setTaskResult(TaskResult taskResult) {
        this.taskResult = taskResult;
    }

    public interface TaskResult {
        void onResult(String prepay_id);
    }
}

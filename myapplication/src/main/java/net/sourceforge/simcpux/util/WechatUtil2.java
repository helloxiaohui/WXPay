package net.sourceforge.simcpux.util;

import net.sourceforge.simcpux.Constants;

import org.apache.http.NameValuePair;

import java.security.MessageDigest;
import java.util.List;

public class WechatUtil2 {

    private static final String TAG = "WechatUtil";


    /**
     * 调起微信APP支付，签名
     */
    public static String genPackageSign2(List<NameValuePair> params) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < params.size(); i++) {    //将参数拼接成键值对样式的字符串
            sb.append(params.get(i).getName());
            sb.append('=');
            sb.append(params.get(i).getValue());
            sb.append('&');
        }
        sb.append("key=");
        sb.append(Constants.PARTNER_KEY); //拼接key

        //进行MD5加密，并转为大写
        String packageSign = MD5.getMessageDigest(sb.toString().getBytes()).toUpperCase();
        return packageSign;    //返回签名
    }

    /**
     * MD5加密算法
     */
    public final static String getMessageDigest(byte[] buffer) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(buffer);
            byte[] md = mdTemp.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }

}

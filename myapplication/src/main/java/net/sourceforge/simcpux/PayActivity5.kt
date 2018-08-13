package net.sourceforge.simcpux


import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.ypcang.thirdbuy.R
import kotlinx.android.synthetic.main.activity_pay_test5.*
import net.sourceforge.simcpux.util.MyGetAccessTokenTask
import net.sourceforge.simcpux.util.WechatUtil2
import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair
import java.util.*

class PayActivity5 : AppCompatActivity() {

    private var api: IWXAPI? = null
    private val TAG = "PayActivity"
    private lateinit var mContext: Context
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay_test5)
        mContext = this


        //应用ID
        btnAPPID.setText(Constants.APP_ID)

        //商户ID
        btnPartnerId.setText(Constants.MCH_ID)

        findViewById<View>(R.id.btnStart).setOnClickListener {
            val req = PayReq()

            //应用ID
            req.appId = btnAPPID.text.toString()

            //商户ID
            req.partnerId = btnPartnerId.text.toString()

            //预支付ID
            req.prepayId = btnPrepayId.text.toString()

            //随机字符串
            req.nonceStr = btnNonceStr.text.toString()

            //时间戳
            req.timeStamp = btnTimeStamp.text.toString()

            //默认扩展字段
            req.packageValue = btnPackageValue.text.toString()

            //签名
            req.sign = btnSign.text.toString()
            sendPayReq(req)
        }
        btnCreatePayID.setOnClickListener {
            //生成预支付ID:

            MyGetAccessTokenTask(mContext, MyGetAccessTokenTask.TaskResult { result ->
                btnPrepayId.setText(result)
                prepay_idString=result
            }).execute()
        }

        btnCreatePayRandom.setOnClickListener({
            //随机字符串
            btnNonceStr.setText(genNonceStr())
        })
        btnCreatePaySign.setOnClickListener {

            if (prepay_idString == ""|| btnPrepayId.text.toString() == "") {
                Toast.makeText(mContext, "请先获取预支付ID->prepay_id", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //生成支付签名
            val req = PayReq()
            req.appId = Constants.APP_ID
            req.partnerId = Constants.MCH_ID
            req.prepayId = prepay_idString
            //req.packageValue = "prepay_id=" + resultunifiedorder.get("prepay_id");
            req.packageValue = "Sign=WXPay"
            req.nonceStr = btnNonceStr.text.toString()
            req.timeStamp = btnTimeStamp.text.toString()
            val signParams = LinkedList<NameValuePair>()
            signParams.add(BasicNameValuePair("appid", req.appId))
            signParams.add(BasicNameValuePair("noncestr", req.nonceStr))
            signParams.add(BasicNameValuePair("package", req.packageValue))
            signParams.add(BasicNameValuePair("partnerid", req.partnerId))
            signParams.add(BasicNameValuePair("prepayid", req.prepayId))
            signParams.add(BasicNameValuePair("timestamp", req.timeStamp))
            req.sign = WechatUtil2.genPackageSign2(signParams)

            // sb.append("sign\n"+req.sign+"\n\n");
            Log.e("orion", signParams.toString())
            //sendPayReq(req)
            btnSign.setText(req.sign)
        }

        btnCreatePayTime.setOnClickListener {
            //生成预支付的时间戳
            btnTimeStamp.setText(genTimeStamp().toString())
        }

    }

    private lateinit var mWeixinApi: IWXAPI
    private var prepay_idString = ""

    private fun sendPayReq(req: PayReq) {
        mWeixinApi = WXAPIFactory.createWXAPI(mContext, Constants.APP_ID)
        // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
        mWeixinApi.registerApp(Constants.APP_ID)
        Log.d(TAG, "mWeixinApi.sendReq")
        mWeixinApi.sendReq(req)

    }

    private fun genNonceStr(): String {
        val random = Random()
        return MD5.getMessageDigest(random.nextInt(10000).toString().toByteArray())
    }

    private fun genTimeStamp(): Long {
        return System.currentTimeMillis() / 1000
    }


}

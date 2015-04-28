package com.pool

import android.app.Activity
import android.content.{Context, ComponentName, ServiceConnection, Intent}
import android.graphics.Color
import android.os.{IBinder, Bundle}
import android.os.{IBinder, Bundle}
import android.content.{Context, ComponentName, ServiceConnection, Intent}
import android.telephony.SmsMessage.MessageClass
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Toast, TextView, Button}
import com.pool
import com.pool.networking.Message

/**
 * Created by Lawrence on 4/25/15.
 */
class RequestDetail extends Activity {

  var mIsBound : Boolean = false
  var mBoundService : NetworkService = null
  var mConnection : ServiceConnection = new ServiceConnection() {
    def onServiceConnected(className : ComponentName, service : IBinder) : Unit = {
      mBoundService = service.asInstanceOf[NetworkService#LocalBinder].getService()
    }
    def onServiceDisconnected(className : ComponentName): Unit = {
      mBoundService = null
    }
  }

  def doBindService(): Unit = {
    bindService(new Intent(this, classOf[NetworkService]), mConnection, Context.BIND_AUTO_CREATE)
    mIsBound = true
  }

  override def onCreate(savedInstanceState:Bundle) : Unit = {
    super.onCreate(savedInstanceState)
    doBindService()
    setContentView(R.layout.order)
    getWindow().getDecorView().setBackgroundColor(Color.rgb(0,73,105))

    var descrpText = findViewById(R.id.orderDetails).asInstanceOf[TextView]

    var requestText = getIntent().getExtras().getString("content")

    for(i <- 0 to mBoundService.mp.receivedRequests.size() - 1){
      if (mBoundService.mp.receivedRequests.get(mBoundService.mp.receivedRequests.size() - 1 - i).text == requestText){
        descrpText.setText(mBoundService.mp.receivedRequests.get(mBoundService.mp.receivedRequests.size() - 1 - i).text + mBoundService.mp.receivedRequests.get(mBoundService.mp.receivedRequests.size() - 1 - i).mType)
      }
    }
  }

  override def onDestroy(): Unit = {
    super.onDestroy()
    unbindService(mConnection)
    mIsBound = false
  }


}

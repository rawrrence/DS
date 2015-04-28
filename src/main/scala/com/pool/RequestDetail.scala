package com.pool

import android.app.Activity
import android.content.{ComponentName, Context, Intent, ServiceConnection}
import android.graphics.Color
import android.os.{Bundle, IBinder}
import android.util.Log
import android.widget.TextView

/**
 * Created by Lawrence on 4/25/15.
 */
class RequestDetail extends Activity {

  var mIsBound : Boolean = false
  var mBoundService : NetworkService = null
  var mConnection : ServiceConnection = new ServiceConnection() {
    def onServiceConnected(className : ComponentName, service : IBinder) : Unit = {
      mBoundService = service.asInstanceOf[NetworkService#LocalBinder].getService()

      var descrpText = findViewById(R.id.orderDetails).asInstanceOf[TextView]

      var requestText = getIntent().getExtras().getString("content")
      Log.w("Pool", requestText)
      for(i <- 0 to mBoundService.mp.receivedRequests.size() - 1){
        Log.w("Pool",mBoundService.mp.receivedRequests.get(mBoundService.mp.receivedRequests.size() - 1 - i).text)
        if (mBoundService.mp.receivedRequests.get(mBoundService.mp.receivedRequests.size() - 1 - i).text == requestText){
          descrpText.setText(mBoundService.mp.receivedRequests.get(mBoundService.mp.receivedRequests.size() - 1 - i).text + mBoundService.mp.receivedRequests.get(mBoundService.mp.receivedRequests.size() - 1 - i).mType)
        }
      }
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

  }

  override def onDestroy(): Unit = {
    super.onDestroy()
    unbindService(mConnection)
    mIsBound = false
  }


}

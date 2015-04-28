package com.pool

import android.app.Activity
import android.content.{Context, ComponentName, ServiceConnection, Intent}
import android.graphics.Color
import android.os.{IBinder, Bundle}
import android.telephony.SmsMessage.MessageClass
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Toast, TextView, Button}
import com.pool
import com.pool.networking.Message

/**
 * Created by Lawrence on 4/25/15.
 */
class Request extends Activity {

  var sendButton : Button = null
  var descrpText : TextView = null

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
    setContentView(R.layout.request)
    getWindow().getDecorView().setBackgroundColor(Color.rgb(0,73,105))

    sendButton = findViewById(R.id.request_send_button).asInstanceOf[Button]
    descrpText = findViewById(R.id.request_descrp_text).asInstanceOf[TextView]

    sendButton.setOnClickListener(sendClicked)

    doBindService()
  }

  override def onDestroy(): Unit = {
    super.onDestroy()
    unbindService(mConnection)
    mIsBound = false
  }

  var sendClicked : OnClickListener = new OnClickListener() {
    override def onClick(v : View): Unit = {
      val descrp: String = descrpText.getText.toString

      // TODO: send the request using the message passer from NetworkService
      var msg : Message = new Message(mBoundService.mp.self.id, -1, descrp,"REQUEST")

      mBoundService.mp.broadcast(msg)
      val toast = Toast.makeText(Request.this, "Request has been sent!", Toast.LENGTH_SHORT)
      toast.show()
      finish()
    }
  }

}

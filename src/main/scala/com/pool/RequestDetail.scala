package com.pool

import android.app.Activity
import android.content.{ComponentName, Context, Intent, ServiceConnection}
import android.graphics.Color
import android.os.{Bundle, IBinder}
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.{EditText, Button, TextView}
import com.pool.networking.Message

/**
 * Created by Lawrence on 4/25/15.
 */
class RequestDetail extends Activity {

  var replyButton : Button = null
  var request : Message = null
  var replyText : EditText = null

  var mIsBound : Boolean = false
  var mBoundService : NetworkService = null
  var mConnection : ServiceConnection = new ServiceConnection() {
    def onServiceConnected(className : ComponentName, service : IBinder) : Unit = {
      mBoundService = service.asInstanceOf[NetworkService#LocalBinder].getService()

      var descrpText = findViewById(R.id.detail_body_text).asInstanceOf[TextView]
      var titleText = findViewById(R.id.detail_title_text).asInstanceOf[TextView]
      replyText = findViewById(R.id.reply_text).asInstanceOf[EditText]
      replyButton = findViewById(R.id.reply_button).asInstanceOf[Button]
      replyButton.setOnClickListener(replyClicked)


      var requestText = getIntent().getExtras().getString("content")
      Log.w("Pool", requestText)
      for(i <- 0 to mBoundService.mp.receivedRequests.size() - 1){
        Log.w("Pool",mBoundService.mp.receivedRequests.get(mBoundService.mp.receivedRequests.size() - 1 - i).title)
        if (mBoundService.mp.receivedRequests.get(mBoundService.mp.receivedRequests.size() - 1 - i).title == requestText) {
          val request = mBoundService.mp.receivedRequests.get(mBoundService.mp.receivedRequests.size() - 1 - i)
          Log.w("Pool", request.src.toString)
          descrpText.setText(request.body)
          titleText.setText(request.title)
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

  var replyClicked : OnClickListener = new OnClickListener {
    override def onClick(v: View): Unit = {
      val reply = replyText.getText.toString
      var msg : Message = new Message(mBoundService.mp.self.id, request.src,"",reply,"REPLY")
      mBoundService.mp.send(msg, request.src)
      finish()
    }
  }

}

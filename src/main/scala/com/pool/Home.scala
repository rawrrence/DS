package com.pool

import _root_.android.app.Activity
import _root_.android.graphics.Color
import android.os.{IBinder, Bundle}
import android.content.{Context, ComponentName, ServiceConnection, Intent}
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, MultiAutoCompleteTextView}

class Home extends Activity {
  var requestButton : Button = null
  var workButton : Button = null
  var showButton : Button = null

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

  def doStartAndBindService(): Unit = {
    startService(new Intent(this, classOf[NetworkService]))
    bindService(new Intent(this, classOf[NetworkService]), mConnection, Context.BIND_AUTO_CREATE)
    mIsBound = true
  }

  override def onCreate(savedInstanceState:Bundle) : Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.home)
    getWindow().getDecorView().setBackgroundColor(Color.rgb(61,164,255))

    requestButton = findViewById(R.id.home_request_button).asInstanceOf[Button]
    workButton = findViewById(R.id.home_work_button).asInstanceOf[Button]
    showButton = findViewById(R.id.home_show_requests_button).asInstanceOf[Button]


    requestButton.setOnClickListener(requestClicked)
    workButton.setOnClickListener(workClicked)
    showButton.setOnClickListener(showClicked)

    // Create and start the service
    //doStartAndBindService()
  }

  override def onDestroy(): Unit = {
    super.onDestroy()
    unbindService(mConnection)
    mIsBound = false
  }

  var requestClicked : OnClickListener = new OnClickListener() {
    override def onClick(v : View): Unit = {
      val intent : Intent = new Intent(getApplicationContext(), classOf[Request])
      startActivity(intent)
    }
  }

  var workClicked : OnClickListener = new OnClickListener() {
    override def onClick(v : View): Unit = {
      val intent : Intent = new Intent(getApplicationContext(), classOf[ListLayoutActivity])
      startActivity(intent)
    }
  }


  var showClicked : OnClickListener = new OnClickListener {
    override def onClick(v: View): Unit = {
      mBoundService.showMessages()
    }
  }

}

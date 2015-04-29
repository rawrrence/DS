package com.pool

import _root_.android.app.Activity
import _root_.android.graphics.Color
import android.os.{IBinder, Bundle}
import android.content.{Context, ComponentName, ServiceConnection, Intent}
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, MultiAutoCompleteTextView}
import com.pool.networking.LocationService

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
    //check gps
    LocationService.checkGps(this)

    //network service
    val serviceIntent : Intent = new Intent(this, classOf[NetworkService])
    startService(serviceIntent)
    bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE)
    mIsBound = true
  }

  override def onCreate(savedInstanceState:Bundle) : Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.home)
    getWindow().getDecorView().setBackgroundColor(Color.rgb(0,73,105))

    requestButton = findViewById(R.id.home_request_button).asInstanceOf[Button]
    workButton = findViewById(R.id.home_work_button).asInstanceOf[Button]


    requestButton.setOnClickListener(requestClicked)
    workButton.setOnClickListener(workClicked)

    // Create and start the service
    doStartAndBindService()
  }

  override def onResume(): Unit = {
    super.onResume()
  }

  override def onDestroy(): Unit = {
    super.onDestroy()
    unbindService(mConnection)
    stopService(new Intent(this, classOf[NetworkService]))
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
      val intent : Intent = new Intent(getApplicationContext(), classOf[ListRequests])
      startActivity(intent)
    }
  }


  var showClicked : OnClickListener = new OnClickListener {
    override def onClick(v: View): Unit = {
      mBoundService.showMessages()
    }
  }

}

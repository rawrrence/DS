package com.pool

import android.app.{AlertDialog, Activity}
import _root_.android.graphics.Color
import android.os.{IBinder, Bundle}
import android.content._
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, MultiAutoCompleteTextView}
import com.pool.networking.LocationService

class Home extends Activity {
  var requestButton : Button = null
  var workButton : Button = null
  var myRequestsButton : Button = null

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
    bindService(serviceIntent, mConnection, Context.BIND_IMPORTANT)
    mIsBound = true
  }

  override def onCreate(savedInstanceState:Bundle) : Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.home)
    getWindow().getDecorView().setBackgroundColor(Color.rgb(0,73,105))

    requestButton = findViewById(R.id.home_request_button).asInstanceOf[Button]
    workButton = findViewById(R.id.home_work_button).asInstanceOf[Button]
    myRequestsButton = findViewById(R.id.my_requests).asInstanceOf[Button]


    requestButton.setOnClickListener(requestClicked)
    workButton.setOnClickListener(workClicked)
    myRequestsButton.setOnClickListener(myRequestsClicked)

    // Create and start the service
    doStartAndBindService()
  }

  override def onResume(): Unit = {
    def buildAlertMessageNoGps(con : Context, phone: String, name: String) : Unit = {
      val builder : AlertDialog.Builder = new AlertDialog.Builder(con)
      builder.setMessage("You have a new job! Contact the requester with the information below.")
        .setCancelable(true)
        .setPositiveButton(name, new DialogInterface.OnClickListener() {
        def onClick(dialog : DialogInterface, id : Int) : Unit = {
          dialog.cancel()
        }
      })
        .setNegativeButton(phone, new DialogInterface.OnClickListener() {
        def onClick(dialog : DialogInterface, id : Int) : Unit = {
          dialog.cancel()
        }
      })
      val alert : AlertDialog = builder.create()
      alert.show()
    }

    if (mBoundService != null) {
      for(i <- 0 to mBoundService.mp.receivedReplies.size() - 1){
        if (mBoundService.mp.receivedReplies.get(mBoundService.mp.receivedReplies.size() - 1 - i).body == mBoundService.mp.self.id.toString){
          val phone : String = mBoundService.mp.receivedReplies.get(mBoundService.mp.receivedReplies.size() - 1 - i).phone
//          var name : String = mBoundService.mp.config.nodes.get(mBoundService.mp.receivedReplies.get(mBoundService.mp.receivedReplies.size() - 1 - i).src).name
          var name : String = mBoundService.mp.config.nodes.get(mBoundService.mp.receivedReplies.get(mBoundService.mp.receivedReplies.size()-1-i).src
).name
          buildAlertMessageNoGps(this, phone, name)
          mBoundService.mp.receivedReplies.remove(mBoundService.mp.receivedReplies.size() - 1 - i)
        }
      }
    }

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


  var myRequestsClicked : OnClickListener = new OnClickListener {
    override def onClick(v: View): Unit = {
      val intent : Intent = new Intent(getApplicationContext(), classOf[ListMyRequests])
      startActivity(intent)
    }
  }

}

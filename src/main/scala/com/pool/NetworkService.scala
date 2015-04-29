package com.pool

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Binder
import android.util.Log
import android.widget.Toast
import com.pool.networking.{LocationService, MessagePasser}

/**
 * Created by Lawrence on 4/25/15.
 */
class NetworkService extends Service {
  val mBinder : IBinder = new LocalBinder()
  var mp : MessagePasser = null
  var locationService : LocationService = null

  class LocalBinder extends Binder {
    def getService() : NetworkService = {
      return NetworkService.this
    }
  }

  override def onCreate(): Unit = {
    //handles initialization in onStartCommand
  }

  override def onBind(intent : Intent) : IBinder = {
    return mBinder
  }

  override def onDestroy(): Unit = {
    mp.exit()
  }

  override def onStartCommand(intent : Intent, flags : Int, startId : Int) : Int = {
    Log.w("Pool", "Service is starting")

    locationService = new LocationService(this.getApplicationContext)
    mp = new MessagePasser(locationService, "name", "10001", "pool842.ddns.net", "10001")

    return Service.START_STICKY
  }

  def showMessages(): Unit = {
    var toast : Toast = null
    if (mp.receivedRequests.size > 0) {
      var text : String = mp.receivedRequests.poll.text
      toast = Toast.makeText(this, text, Toast.LENGTH_SHORT)
    } else {
      toast = Toast.makeText(this, "No msg", Toast.LENGTH_SHORT)
    }
    toast.show()
  }
}

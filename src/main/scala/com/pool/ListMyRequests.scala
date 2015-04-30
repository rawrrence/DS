package com.pool

import android.app.ListActivity
import android.content.{ComponentName, Context, Intent, ServiceConnection}
import android.graphics.Color
import android.os.{Bundle, IBinder}
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.{AdapterView, ArrayAdapter, TextView, Toast}


class ListMyRequests extends ListActivity {

  var mIsBound : Boolean = false
  var mBoundService : NetworkService = null
  var mConnection : ServiceConnection = new ServiceConnection() {
    def onServiceConnected(className : ComponentName, service : IBinder) : Unit = {
      mBoundService = service.asInstanceOf[NetworkService#LocalBinder].getService()

      var requestsArr = Array("The requests you've received are shown below")

      for(i <- 0 to mBoundService.mp.receivedRequests.size() - 1){
        requestsArr = requestsArr :+ mBoundService.mp.receivedRequests.get(mBoundService.mp.receivedRequests.size() - 1 - i).title
      }

      setListAdapter(new ArrayAdapter[String](ListMyRequests.this, R.layout.list_requests, requestsArr))
      val lv = getListView()
      lv.setTextFilterEnabled(true)

      lv.setOnItemClickListener(new OnItemClickListener() {
        override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long) {
          Toast.makeText(
            getApplicationContext(),
            "Go to detailed view",
            Toast.LENGTH_SHORT
          ).show()

          val intent : Intent = new Intent(getApplicationContext(), classOf[RequestDetail])
          intent.putExtra("content", view.asInstanceOf[TextView].getText())
          startActivity(intent)
        }
      })
    }
    def onServiceDisconnected(className : ComponentName): Unit = {
      mBoundService = null
    }
  }

  def doBindService(): Unit = {
    bindService(new Intent(this, classOf[NetworkService]), mConnection, Context.BIND_AUTO_CREATE)
    mIsBound = true
  }


  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    getWindow().getDecorView().setBackgroundColor(Color.rgb(0,73,105))
    doBindService()

  }

  override def onDestroy(): Unit = {
    super.onDestroy()
    unbindService(mConnection)
    mIsBound = false
  }
}

package com.pool


import android.app.ListActivity
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.os.{IBinder, Bundle}
import android.content.{Context, ComponentName, ServiceConnection, Intent}
import android.widget.{AdapterView, ArrayAdapter, TextView, Toast}
import com.pool.networking.Message


class ListLayoutActivity extends ListActivity {

  var mIsBound : Boolean = false
  var mBoundService : NetworkService = null
  var mConnection : ServiceConnection = new ServiceConnection() {
    def onServiceConnected(className : ComponentName, service : IBinder) : Unit = {
      mBoundService = service.asInstanceOf[NetworkService#LocalBinder].getService()

      var requestsArr = Array("The requests you've received are shown below")

      for(i <- 0 to mBoundService.mp.receivedRequests.size() - 1){
        requestsArr = requestsArr :+ mBoundService.mp.receivedRequests.get(mBoundService.mp.receivedRequests.size() - 1 - i).text
      }

      setListAdapter(new ArrayAdapter[String](ListLayoutActivity.this, R.layout.list_item, requestsArr))
      val lv = getListView()
      lv.setTextFilterEnabled(true)

      lv.setOnItemClickListener(new OnItemClickListener() {
        override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long) {
          Toast.makeText(
            getApplicationContext(),
            view.asInstanceOf[TextView].getText(),
            Toast.LENGTH_SHORT
          ).show()
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

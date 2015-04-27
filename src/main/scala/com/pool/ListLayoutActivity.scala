package com.pool


import android.app.ListActivity
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

    var m1 : Message = new Message(1, 1, "request from Jack")
    var m2 : Message = new Message(2, 2, "request from Rose")
    var m3 : Message = new Message(3, 3, "request from Eggplant")

    var mArr = Array(m1.text, m2.text, m3.text)

    setListAdapter(new ArrayAdapter[String](this, R.layout.list_item, mArr))

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

    doBindService()
  }

  override def onDestroy(): Unit = {
    super.onDestroy()
    unbindService(mConnection)
    mIsBound = false
  }
}

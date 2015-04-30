package com.pool


import android.app.ListActivity
import android.graphics.{Bitmap, BitmapFactory, Color}
import android.os.{ Bundle, IBinder}
import android.view.View.OnClickListener
import android.view.{LayoutInflater, ViewGroup, View}
import android.widget.AdapterView.OnItemClickListener
import android.content.{Context, ComponentName, ServiceConnection, Intent}
import android.widget._
import com.pool.networking.Message


class ListReplys extends ListActivity {

  var mIsBound : Boolean = false
  var mBoundService : NetworkService = null
  var mConnection : ServiceConnection = new ServiceConnection() {



    def onServiceConnected(className : ComponentName, service : IBinder) : Unit = {
      mBoundService = service.asInstanceOf[NetworkService#LocalBinder].getService()

      val m1 = new Message(-1, -1, "cleaning service needed", "for my bathroom")
      val m2 = new Message(-2, -1, "demo replacement need","can someone help me demo my ds project tmr")
      var originRequest : Message = null

      var requestText = getIntent().getExtras().getString("content")

      for(i <- 0 to mBoundService.mp.sentRequests.size() - 1){
        if (mBoundService.mp.sentRequests.get(mBoundService.mp.sentRequests.size() - 1 - i).title == requestText){
          originRequest = mBoundService.mp.sentRequests.get(mBoundService.mp.sentRequests.size() - 1 - i)
        }
      }

      var arr = Array(originRequest)

      for(i <- 0 to mBoundService.mp.receivedReplies.size() - 1){
        if (mBoundService.mp.receivedReplies.get(mBoundService.mp.receivedReplies.size() - 1 - i).seqNum == arr(0).seqNum){
          arr = arr :+ mBoundService.mp.receivedReplies.get(mBoundService.mp.receivedReplies.size() - 1 - i)
        }
      }

      setListAdapter(new ChatsAdapter(ListReplys.this, arr))
      val lv = getListView()
      lv.setTextFilterEnabled(true)

      lv.setOnItemClickListener(new OnItemClickListener() {
        override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long) {
          Toast.makeText(
            getApplicationContext(),
            "Go to check reply",
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


  class ChatsAdapter(
                      val context: Context,
                      val chats: Array[Message]
                      )extends BaseAdapter{

    private  val inflater = LayoutInflater.from(context)
    private  val layout = R.layout.reply

    override def getCount():Int = chats.size
    override def getItem(arg0: Int):Message = null
    override def getItemId(arg0: Int):Long = 0

    override def getView(position: Int, convertView: View, parent:ViewGroup): View = {
      // Get the data item for this position
      val chat = chats(position)
      // Check if an existing view is being reused, otherwise inflate the view
      var view = convertView
      if (view == null) {
        view = inflater.inflate(this.layout, null)
      }
      // Lookup view for data population
      val Viewtitle = view.findViewById(R.id.reply_title).asInstanceOf[TextView]
      val reply_title = view.findViewById(R.id.reply_title_text).asInstanceOf[TextView]
      val reply_body = view.findViewById(R.id.reply_body_text).asInstanceOf[TextView]
      val reject= view.findViewById(R.id.reply_reject_button).asInstanceOf[Button]
      var accept =  view.findViewById(R.id.reply_accept_button).asInstanceOf[Button]
      // Populate the data into the template view using the data object
      reply_title.setText(chat.title)
      reply_body.setText(chat.body)

      if (position == 0){
        reject.setEnabled(false)
        accept.setEnabled(false)
        Viewtitle.setText("Origin message")
      }


      // dummy click listener for button
      var sendClicked : OnClickListener = new OnClickListener() {
        override def onClick(v : View): Unit = {
          mBoundService.mp.acceptReply(chat)
          val toast = Toast.makeText(ListReplys.this, accept.getText, Toast.LENGTH_SHORT)
          toast.show()
          finish()
        }
      }
      accept.setOnClickListener(sendClicked)

      // Return the completed view to render on screen
      view
    }
  }

}


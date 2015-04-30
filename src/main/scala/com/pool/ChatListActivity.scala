package com.pool


import android.app.ListActivity
import android.graphics.{Bitmap, BitmapFactory, Color}
import android.os.{ Bundle, IBinder}
import android.view.{LayoutInflater, ViewGroup, View}
import android.widget.AdapterView.OnItemClickListener
import android.content.{Context, ComponentName, ServiceConnection, Intent}
import android.widget._
import com.pool.networking.Message


class ChatListActivity extends ListActivity {

  var mIsBound : Boolean = false
  var mBoundService : NetworkService = null
  var mConnection : ServiceConnection = new ServiceConnection() {



    def onServiceConnected(className : ComponentName, service : IBinder) : Unit = {
      mBoundService = service.asInstanceOf[NetworkService#LocalBinder].getService()

      val m1 = new Message(-1, -1, "below are the job you need")
      val m2 = new Message(-2, -1, "a job")
      var requestsArr = Array(m1, m2)

//      if (mBoundService.receivedRequests != null){
//        for(i <- 0 to mBoundService.mp.receivedRequests.size() - 1){
//          requestsArr = requestsArr :+ mBoundService.mp.receivedRequests.get(mBoundService.mp.receivedRequests.size() - 1 - i)
//        }
//      }


      setListAdapter(new ChatsAdapter(ChatListActivity.this, requestsArr))
      val lv = getListView()
      lv.setTextFilterEnabled(true)

      lv.setOnItemClickListener(new OnItemClickListener() {
        override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long) {
          Toast.makeText(
            getApplicationContext(),
            "Go to detailed view",
            Toast.LENGTH_SHORT
          ).show()

//          val intent : Intent = new Intent(getApplicationContext(), classOf[RequestDetail])
//          intent.putExtra("content", view.asInstanceOf[TextView].getText())
//          startActivity(intent)
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
//    def this(context: Context, chats : util.ArrayList<Message>) {
//      this.context = context
//      this.chats = chats
//    }

      private  val inflater = LayoutInflater.from(context)
      private  val layout = R.layout.chat_item

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
      val name = view.findViewById(R.id.srcName).asInstanceOf[TextView]
      val content = view.findViewById(R.id.chatContent).asInstanceOf[TextView]
      var image =  view.findViewById(R.id.imageView).asInstanceOf[ImageView]
      // Populate the data into the template view using the data object
      name.setText(chat.src.toString)
      content.setText(chat.text)
       if(chat.src != -1){
         var bImage : Bitmap = BitmapFactory.decodeResource(ChatListActivity.this.getResources(), R.drawable.pool_logo)
         image.setImageBitmap(bImage)
       }
      // Return the completed view to render on screen
       view
    }
  }

}


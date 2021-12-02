package bits.pilani.hyd.wanderingpages

import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import androidx.recyclerview.widget.RecyclerView
import android.os.Bundle
import android.text.InputFilter
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import android.text.InputFilter.LengthFilter
import android.widget.*
import androidx.appcompat.widget.Toolbar
import bits.pilani.hyd.wanderingpages.Adapter.MessageAdapter
import bits.pilani.hyd.wanderingpages.Model.Chat
import bits.pilani.hyd.wanderingpages.Model.User
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseError
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class MessageActivity : AppCompatActivity() {
    var firebaseUser: FirebaseUser? = null
    var reference: DatabaseReference? = null
    var messageAdapter: MessageAdapter? = null
    var userID: String? = ""
    var bookName: String? = ""
    var userName: String? = ""
    var currentDate = ""
    var currentTime = ""
    var Sdate = ""
    var Stime = ""
    var dateTime = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { //add this code
            //startActivity ( new Intent ( getApplicationContext (),MainActivity.class ).setFlags ( Intent.FLAG_ACTIVITY_CLEAR_TOP ) );
            finish()
        }
        findViewById<RecyclerView>(R.id.recycler_view).setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        findViewById<RecyclerView>(R.id.recycler_view).layoutManager = linearLayoutManager
        var intent = intent
        userID = intent.getStringExtra("userID")
        bookName = intent.getStringExtra("bookName")
        userName = intent.getStringExtra("userName")
        firebaseUser = FirebaseAuth.getInstance().currentUser
        if (bookName != "") {
            findViewById<EditText>(R.id.text_send).setText("Hi $userName, I am interested in book $bookName")
        }

        findViewById<EditText>(R.id.text_send).filters = arrayOf<InputFilter>(LengthFilter(
            DEFAULT_MSG_LENGTH_LIMIT
        ))

        findViewById<ImageButton>(R.id.btn_send).setOnClickListener {
            val msg = findViewById<EditText>(R.id.text_send).text.toString().trim { it <= ' ' }
            getDateTime()
            dateTime = "$Sdate // $Stime"
            if (msg != "") {
                sendMessage(firebaseUser!!.uid, userID, msg, dateTime)
            } else {
                Toast.makeText(
                    this@MessageActivity,
                    "You can't send empty message",
                    Toast.LENGTH_SHORT
                ).show()
            }
            findViewById<EditText>(R.id.text_send).setText("")
        }
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userID!!)
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(
                    User::class.java
                )

                findViewById<TextView>(R.id.username).text = Objects.requireNonNull(user)!!.username
                if (user!!.imageURL == "default") {
                    findViewById<CircleImageView>(R.id.profile_image).setImageResource(R.drawable.userphoto)
                } else {
                    Glide.with(applicationContext).load(user.imageURL)
                        .placeholder(R.drawable.new_loader)
                        .into(findViewById(R.id.profile_image))
                }
                readMessage(firebaseUser!!.uid, userID, user.imageURL)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun sendMessage(sender: String, receiver: String?, message: String, dateTime: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val hashMap = HashMap<String, Any?>()
        hashMap["sender"] = sender
        hashMap["receiver"] = receiver
        hashMap["message"] = message
        hashMap["dateTime"] = dateTime
        reference.child("Chats").push().setValue(hashMap)

        //add user to chat fragment
        val chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
            .child(firebaseUser!!.uid)
            .child(userID!!)
        chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatRef.child("id").setValue(userID)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        val chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
            .child(userID!!)
            .child(firebaseUser!!.uid)
        chatRefReceiver.child("id").setValue(firebaseUser!!.uid)
    }

    private fun readMessage(myID: String, userID: String?, imageURL: String) {
        val mChats = mutableListOf<Chat>()
        reference = FirebaseDatabase.getInstance().getReference("Chats")
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mChats.clear()
                for (dataSnapshot in snapshot.children) {
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if (chat!!.sender == myID && chat.receiver == userID ||
                        chat.receiver == myID && chat.sender == userID
                    ) {
                        mChats.add(chat)
                    }
                    messageAdapter = MessageAdapter(this@MessageActivity, mChats, imageURL)
                    findViewById<RecyclerView>(R.id.recycler_view).adapter = messageAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun is_online(is_online: String) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser!!.uid)
        val hashMap = HashMap<String, Any>()
        hashMap["is_online"] = is_online
        reference!!.updateChildren(hashMap)
    }

    override fun onResume() {
        super.onResume()
        is_online("online")
    }

    override fun onPause() {
        super.onPause()
        is_online("offline")
    }

    fun getDateTime() {
        //time and date
        currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val date = currentDate.split("-").toTypedArray()
        val day = date[0]
        val temp = date[1]
        val year = date[2]
        var month: String = getMonth(temp)
        Sdate = "$day $month $year"
        currentTime = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())
        val arr = currentTime.split(":").toTypedArray()
        val am_pm = arr[2].split(" ").toTypedArray()
        Stime = arr[0] + ":" + arr[1] + " " + am_pm[1].uppercase(Locale.getDefault())
    }

    fun getMonth(temp: String): String {
        var month = ""
        if (temp == "01") {
            month = "JAN"
        } else if (temp == "02") {
            month = "FEB"
        } else if (temp == "03") {
            month = "MAR"
        } else if (temp == "04") {
            month = "APL"
        } else if (temp == "05") {
            month = "MAY"
        } else if (temp == "06") {
            month = "JUN"
        } else if (temp == "07") {
            month = "JULY"
        } else if (temp == "08") {
            month = "AUG"
        } else if (temp == "09") {
            month = "SEPT"
        } else if (temp == "10") {
            month = "OCT"
        } else if (temp == "11") {
            month = "NOV"
        } else if (temp == "12") {
            month = "DEC"
        }
        return month
    }

    companion object {
        private const val DEFAULT_MSG_LENGTH_LIMIT = 500
    }
}
package bits.pilani.hyd.wanderingpages

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseError
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.fragment.app.FragmentPagerAdapter
import kotlin.Throws
import android.location.Geocoder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import bits.pilani.hyd.wanderingpages.Fragments.ChatsFragment
import bits.pilani.hyd.wanderingpages.Fragments.HomeFragment
import bits.pilani.hyd.wanderingpages.Fragments.ProfileFragment
import bits.pilani.hyd.wanderingpages.Model.User
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException
import java.lang.Exception
import java.util.*

class MainActivity : AppCompatActivity() {
    var firebaseUser: FirebaseUser? = null
    var reference: DatabaseReference? = null
    private var gpsTracker: GpsTracker? = null
    private var latitude = 0.0
    private var longitude = 0.0
    private var address = ""
    private var city = ""
    var Uname = ""
    var ProfileUrl = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    101
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        findViewById<FloatingActionButton>(R.id.addPost).setOnClickListener {
            val intent = Intent(applicationContext, AddBookActivity::class.java)
            intent.putExtra("address", address)
            intent.putExtra("city", city)
            intent.putExtra("latitude", latitude.toString())
            intent.putExtra("longitude", longitude.toString())
            startActivity(intent)
        }
        firebaseUser = FirebaseAuth.getInstance().currentUser
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser!!.uid)
        updateLocationData(address, city, latitude.toString(), longitude.toString())
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: User? = snapshot.getValue(
                    User::class.java
                )
                Log.d("MainActivity", user.toString())
                findViewById<TextView>(R.id.username).text = "${user!!.username} / $address , $city"
                Log.d("MainActivity", findViewById<TextView>(R.id.username).text.toString())
                Uname = user!!.username
                ProfileUrl = user.imageURL
                if (user.imageURL == "default") {
//                    profile_image.setImageResource(R.drawable.userphoto)
                    findViewById<CircleImageView>(R.id.profile_image).setImageResource(R.drawable.userphoto)
                } else {
                    Glide.with(applicationContext).load(user.imageURL)
                        .placeholder(R.drawable.new_loader)
                        .into(findViewById(R.id.profile_image))
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val viewPager = findViewById<ViewPager>(R.id.viewpager)
        val viewPagerAdapter = ViewPagerAdapter(
            supportFragmentManager
        )
        viewPagerAdapter.addFragment(HomeFragment(), "Home")
        viewPagerAdapter.addFragment(ChatsFragment(), "Chats")
        //viewPagerAdapter.addFragment ( new UsersFragment (),"Users" );
        viewPagerAdapter.addFragment(ProfileFragment(), "Profile")
        viewPager.adapter = viewPagerAdapter
        tabLayout.setupWithViewPager(viewPager)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                FirebaseAuth.getInstance().signOut()
                //add this flag to save app from crash
                is_online("offline")
                startActivity(
                    Intent(
                        applicationContext,
                        StartActivity::class.java
                    ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
                return true
            }
            R.id.refresh -> {
                //finish()
                startActivity(intent)
                return true
            }
        }
        return false
    }

    internal inner class ViewPagerAdapter(fm: FragmentManager?) : FragmentPagerAdapter(
        fm!!
    ) {
        private val fragments: ArrayList<Fragment> = ArrayList()
        private val titles: ArrayList<String> = ArrayList()
        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }

    }

    private fun is_online(is_online: String) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser!!.uid)
        val hashMap = HashMap<String, Any>()
        hashMap["is_online"] = is_online
        reference!!.updateChildren(hashMap)
    }

    private fun updateLocationData(
        address: String,
        city: String,
        latitude: String,
        longitude: String
    ) {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser!!.uid)
        val hashMap = HashMap<String, Any>()
        hashMap["address"] = address
        hashMap["city"] = city
        hashMap["latitude"] = latitude
        hashMap["longitude"] = longitude
        reference!!.updateChildren(hashMap)
        UserData.USERNAME = Uname
        UserData.PROFILEURL = ProfileUrl
        UserData.LATITUDE = latitude
        UserData.LONGITUDE = longitude
        UserData.ADDRESS = address
        UserData.CITY = city
    }

    override fun onResume() {
        super.onResume()
//        latitudeLongitude
        getlatitudeLongitude()
        try {
            getLocation(latitude, longitude)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        is_online("online")
        updateLocationData(address, city, latitude.toString(), longitude.toString())
        UserData.USERNAME = Uname
        UserData.PROFILEURL = ProfileUrl
        UserData.LATITUDE = latitude.toString()
        UserData.LONGITUDE = longitude.toString()
        UserData.ADDRESS = address
        UserData.CITY = city
    }

    override fun onPause() {
        super.onPause()
        is_online("offline")
    }

    override fun onStart() {
        super.onStart()
        getlatitudeLongitude()
        try {
            getLocation(latitude, longitude)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        updateLocationData(address, city, latitude.toString(), longitude.toString())
        UserData.USERNAME = Uname
        UserData.PROFILEURL = ProfileUrl
        UserData.LATITUDE = latitude.toString()
        UserData.LONGITUDE = longitude.toString()
        UserData.ADDRESS = address
        UserData.CITY = city
    }

    //Toast.makeText ( getApplicationContext (), "LAT : "+latitude +"\n "+"LNG : "+longitude, Toast.LENGTH_SHORT ).show ();
    private fun getlatitudeLongitude() {
        gpsTracker = GpsTracker(this@MainActivity)
        if (gpsTracker!!.canGetLocation()) {
            latitude = gpsTracker!!.getLatitude()
            longitude = gpsTracker!!.getLongitude()
            //Toast.makeText ( getApplicationContext (), "LAT : "+latitude +"\n "+"LNG : "+longitude, Toast.LENGTH_SHORT ).show ();
        } else {
            gpsTracker!!.showSettingsAlert()
        }
    }

    @Throws(IOException::class)
    private fun getLocation(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        Log.d("MainActivity", addresses.toString())
        if (addresses.isNotEmpty()) {
            val state = addresses[0].adminArea
            val country = addresses[0].countryName
            val postalCode = addresses[0].postalCode
            val knownName = addresses[0].featureName
//            address = addresses[0].subLocality
            city = addresses[0].locality
        }
        //Toast.makeText ( this, "Location : "+address+" "+city, Toast.LENGTH_SHORT ).show ();
    }

    object UserData {
        @JvmField
        var USERNAME = ""
        var PROFILEURL = ""
        @JvmField
        var LATITUDE = ""
        @JvmField
        var LONGITUDE = ""
        var ADDRESS = ""
        var CITY = ""
    }
}
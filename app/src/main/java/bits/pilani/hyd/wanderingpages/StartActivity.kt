package bits.pilani.hyd.wanderingpages

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class StartActivity : AppCompatActivity() {
    var auth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        findViewById<Button>(R.id.login).setOnClickListener {
            startActivity(
                Intent(
                    this@StartActivity,
                    LoginActivity::class.java
                )
            )
        }

        findViewById<Button>(R.id.register).setOnClickListener {
            startActivity(
                Intent(
                    this@StartActivity,
                    RegisterActivity::class.java
                )
            )
        }
    }

    override fun onStart() {
        super.onStart()
        auth = FirebaseAuth.getInstance()
        if (auth!!.currentUser != null) {
            startActivity(Intent(this@StartActivity, MainActivity::class.java))
            finish()
        }
    }
}
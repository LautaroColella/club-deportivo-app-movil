package ro.lauta

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.PopupMenu
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialSplitButton

class LoginFromZeroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_from_zero)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        btnLogin.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }

        val splitButton = findViewById<MaterialSplitButton>(R.id.splitbutton)
        val trailingButton = findViewById<Button>(R.id.expand_more_or_less_filled)
        val leadingButton = splitButton.getChildAt(0) as Button

        trailingButton.setOnClickListener {
            val popupMenu = PopupMenu(this, trailingButton)
            popupMenu.menuInflater.inflate(R.menu.lang_dropdown_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                leadingButton.text = menuItem.title
                true
            }

            popupMenu.show()
        }
    }
}

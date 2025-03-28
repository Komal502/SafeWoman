import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.safewoman.EmergencyContactsDBHelper
import com.example.safewoman.R

class AddEmergencyContactActivity : AppCompatActivity() {

    private lateinit var editTextName: EditText
    private lateinit var editTextPhone: EditText
    private lateinit var editTextRelation: EditText
    private lateinit var buttonSave: Button
    private lateinit var dbHelper: EmergencyContactsDBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_emergency_contact)

        // Initialize views
        editTextName = findViewById(R.id.editTextName)
        editTextPhone = findViewById(R.id.editTextPhone)
        editTextRelation = findViewById(R.id.editTextRelation)
        buttonSave = findViewById(R.id.buttonSave)

        dbHelper = EmergencyContactsDBHelper(this) // Database helper

        // Handle save button click
        buttonSave.setOnClickListener {
            val name = editTextName.text.toString().trim()
            val phone = editTextPhone.text.toString().trim()
            val relation = editTextRelation.text.toString().trim()

            // Validate input
            if (name.isEmpty() || phone.isEmpty() || relation.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Add contact to the database
            dbHelper.addEmergencyContact(name, phone, relation)

            // Show success message
            Toast.makeText(this, "Emergency contact added successfully", Toast.LENGTH_SHORT).show()

            // Clear input fields
            editTextName.text.clear()
            editTextPhone.text.clear()
            editTextRelation.text.clear()
        }
    }
}

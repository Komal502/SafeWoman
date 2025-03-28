package com.example.safewoman
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class EmergencyContactsDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "emergency_contacts.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_CONTACTS = "emergency_contacts"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_PHONE = "phone_number"
        const val COLUMN_RELATION = "relation"
    }

    // Create table query
    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_CONTACTS_TABLE = "CREATE TABLE $TABLE_CONTACTS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_NAME TEXT, " +
                "$COLUMN_PHONE TEXT, " +
                "$COLUMN_RELATION TEXT)"
        db.execSQL(CREATE_CONTACTS_TABLE)
    }

    // Upgrade database
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CONTACTS")
        onCreate(db)
    }

    // Insert a new emergency contact
    fun addEmergencyContact(name: String, phone: String, relation: String) {
        val db = this.writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_PHONE, phone)
            put(COLUMN_RELATION, relation)
        }

        // Insert the contact into the database
        db.insert(TABLE_CONTACTS, null, values)
        db.close() // Close the database connection
    }
}

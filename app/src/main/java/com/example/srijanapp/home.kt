package com.example.srijanapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_main.*

class home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        val i= intent.getStringExtra("text")

        setContentView(R.layout.activity_home)
       Toast.makeText(applicationContext,"this is toast message"+i,Toast.LENGTH_SHORT).show()
        if (i != null) {
            hello.text ="Hello "+ i.split('@')[0]
        }

    }
}
package com.example.myapplicationpls

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import android.content.Context
import android.view.MenuItem
import android.widget.Toast


class MenuPageCompat : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pagemenu)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        findViewById<View>(R.id.imageMenu).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)

            val navigationView: NavigationView = findViewById(R.id.nav_view)
            navigationView.itemIconTintList = null

            val navController: NavController =
                Navigation.findNavController(this, R.id.nav_host_fragment)
            NavigationUI.setupWithNavController(navigationView, navController)

        }

    }
    fun close(item: MenuItem) {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        drawerLayout.closeDrawer(GravityCompat.START)
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()

        val preferences = getSharedPreferences("checkbox", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        val isChecked = false // Replace this with your actual isChecked value

        editor.putBoolean("isChecked", isChecked)
        editor.apply()
        val intent = Intent(this, loginCompat::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

}
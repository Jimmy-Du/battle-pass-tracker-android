package com.jdu.battlepasstracker.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jdu.battlepasstracker.R
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element


class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // creates element to display current version
        val versionElement = Element()
        versionElement.title = "Version 1.2.0"

        // creates element to display creator info
        val createdByElement = Element()
        createdByElement.title = "Created by: Jimmy Du"

        // creates an about page to display information about the application
        val aboutPage: View = AboutPage(this)
            .setImage(R.mipmap.ic_launcher_round)
            .setDescription(getString(R.string.description_summary))
            .addItem(versionElement)
            .addItem(createdByElement)
            .addGitHub("Jimmy-Du", "Visit me on GitHub!")
            .create()

        setContentView(aboutPage)

        // enable the back button at top of app to allow user to return to preference screen
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
        return if (id == android.R.id.home) {
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}
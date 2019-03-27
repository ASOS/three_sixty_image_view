/*
 * Copyright ASOS (c) 2019.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.asos.threesixty

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class SampleActivity : AppCompatActivity() {

    private enum class ImplementationType {
        SIMPLE,
        FRESCO,
        GLIDE
    }

    private var implementationType: ImplementationType = ImplementationType.SIMPLE


    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                implementationType = ImplementationType.SIMPLE
            }
            R.id.navigation_dashboard -> {
                implementationType = ImplementationType.FRESCO
            }
            R.id.navigation_notifications -> {
                implementationType = ImplementationType.GLIDE
            }
        }
        return@OnNavigationItemSelectedListener loadFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fresco.initialize(applicationContext)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        loadFragment()
    }

    private fun loadFragment(): Boolean {
        val fragment: Fragment? = when (implementationType) {
            ImplementationType.SIMPLE -> SimpleDemoFragment.newInstance()
            ImplementationType.FRESCO -> FrescoDemoFragment.newInstance()
            ImplementationType.GLIDE -> GlideDemoFragment.newInstance()
        }

        if (fragment != null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
            return true
        }
        return false
    }

}

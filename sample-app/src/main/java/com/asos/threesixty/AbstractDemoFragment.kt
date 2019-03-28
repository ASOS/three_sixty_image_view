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
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.asos.threesixtyimageview.model.image.ImageBinder
import com.asos.threesixtyimageview.model.image.ThreeSixtyImageGallerySource
import com.asos.threesixtyimageview.presenter.*
import com.asos.threesixtyimageview.ui.OnFrameClickListener
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.demo_fragment.*


/**
 * Base class common to all demo Fragments that extends using their own ImageBinder and ImageView types
 */
abstract class AbstractDemoFragment<T : ImageView> : Fragment() {

    private lateinit var source: ThreeSixtyImageGallerySource<T>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.demo_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        source = ThreeSixtyImageGallerySource(
                context = view.context,
                imageBinder = createImageBinder(),
                width = getScreenWidth()
        ).apply {
            thumbnailProportionOfImageSize = 3
            imageUrls = getSampleImageUrls()
            three_sixty_viewer.setImageSource(this)
            loadImages()
        }

        // Register for clicks on the 360 View. Note that when flinging, first tap stops rotation, next tap calls this listener.
        three_sixty_viewer.onFrameClickListener = object : OnFrameClickListener {
            override fun onFrameClick(frameView: View) {
                Snackbar.make(
                        view,
                        "Clicked on Image ${source.currentIndex + 1} of ${source.imageUrls.size}: ${source.currentImageUrl}",
                        Snackbar.LENGTH_LONG
                ).show()
            }
        }

        three_sixty_viewer.setFlingStrategy(CustomFlingStrategy())
    }

    private fun getScreenWidth(): Int = activity?.windowManager?.defaultDisplay?.let {
        val displayMetrics = DisplayMetrics()
        it.getMetrics(displayMetrics)
        displayMetrics.widthPixels
    } ?: 0

    protected abstract fun createImageBinder(): ImageBinder<T>

}

class CustomFlingStrategy(private val flingMinimumVelocityPercentage:Int = 5,
                          private val flingMinNumberOfSpins:Int = 1,
                          private val flingMaxNumberOfExtraSpins:Int = 4,
                          private val flingBaseDelayTimeMillis:Int = 25,
                          private val flingDelaySlowdownFactor:Double = 0.1) : FlingStrategy {
    override fun getSpinDelayTime(imageIndex: Int, velocityPercentage: Int): Long = getFlingBaseDelay(velocityPercentage) + getSlowdownFactor(imageIndex)

    override fun getNumberOfSpins(velocityPercentage: Int): Int = flingMinNumberOfSpins + ((flingMaxNumberOfExtraSpins * velocityPercentage) / 100)

    override fun getFlingMinimumVelocityPercentage(): Int = flingMinimumVelocityPercentage

    private fun getFlingBaseDelay(velocityPercentage: Int): Long = flingBaseDelayTimeMillis - getSpeedImprovementFromVelocity(velocityPercentage)

    private fun getSlowdownFactor(imageIndex: Int): Long = (flingDelaySlowdownFactor * imageIndex).toLong()

    private fun getSpeedImprovementFromVelocity(velocityPercentage: Int): Long = (20L * velocityPercentage) / 100L
}
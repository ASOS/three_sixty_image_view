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
import kotlinx.android.synthetic.main.demo_fragment.*


/**
 * Base class common to all demo Fragments that extends using their own ImageBinder and ImageView types
 */
abstract class AbstractDemoFragment<T:ImageView> : Fragment() {

    private lateinit var source: ThreeSixtyImageGallerySource<T>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.demo_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        source = ThreeSixtyImageGallerySource(context = view.context, imageBinder = createImageBinder(), width = getScreenWidth())
            .apply {
                thumbnailProportionOfImageSize = 3
            }
        with(source) {
            imageUrls = getSampleImageUrls()
            three_sixty_viewer.setImageSource(this)
            loadImages()
        }
    }

    private fun getScreenWidth(): Int = activity?.windowManager?.defaultDisplay?.let {
        val displayMetrics = DisplayMetrics()
        it.getMetrics(displayMetrics)
        displayMetrics.widthPixels
    }?:0

    protected abstract fun createImageBinder():ImageBinder<T>

}
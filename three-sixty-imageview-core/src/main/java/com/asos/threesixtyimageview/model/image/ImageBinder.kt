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

package com.asos.threesixtyimageview.model.image

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import com.asos.threesixtyimageview.model.ImageLoadedListener
import com.asos.threesixtyimageview.model.ThumbnailLoadedListener

/**
 * Performs image binding logic.
 */
interface ImageBinder<T : ImageView> {

    /**
     * Create an instance of the desired implementation of [ImageView]
     * and specify its properties here.
     */
    fun createImageView(context: Context): T

    /**
     * Load a low-res version of the image that'll eventually be displayed in full.
     */
    fun loadThumbnailImage(context: Context, imageUrl: String, imageWidth: Int, index: Int, imageLoadedListener: ThumbnailLoadedListener)

    /**
     * Load the full res image to be displayed in the gallery.
     */
    fun loadFullSizeImage(context: Context, imageUrl: String, imageWidth: Int, index: Int, imageLoadedListener: ImageLoadedListener)

    /**
     * Let the image meet the chosen [ImageView] implementation]!
     */
    fun bindImage(imageUrl: String, imageView: T, screenWidth: Int, thumbnailWidth: Int, bitmap: Bitmap?)

    /**
     * Cancels all requests to load thumbnails or images
     */
    fun cancelAllImageRequests()

}
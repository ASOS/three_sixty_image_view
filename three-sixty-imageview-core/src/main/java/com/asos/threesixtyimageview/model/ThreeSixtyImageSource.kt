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

package com.asos.threesixtyimageview.model

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView

interface ThreeSixtyImageSource {
    fun getImageViewFactory(context: Context): () -> ImageView
    fun bindNextImage(direction: ThreeSixtyImagesDirection, imageView: ImageView)
    fun addLoadingStateListener(listener: ThreeSixtyImagesLoadingStateListener)
    fun removeLoadingStateListener(listener: ThreeSixtyImagesLoadingStateListener)
    fun getCurrentImagesCount(): Int
    fun loadImages()
    fun cancelImageLoading()
    fun clear()
}

interface ThreeSixtyImagesLoadingStateListener {
    fun onLoadingStateChanged(state: ThreeSixtyImagesLoadingState)
}

interface ImageLoadedListener {

    fun onImageLoadedSuccessfully(bitmap: Bitmap?, index: Int)

    fun onImageFailed()
}

interface ThumbnailLoadedListener {
    fun onThumbnailLoadedSuccessfully(bitmap: Bitmap?, index: Int)

    fun onThumbnailFailed()
}

enum class ThreeSixtyImagesDirection {
    DIRECTION_LEFT,
    DIRECTION_RIGHT,
    DIRECTION_NONE
}

enum class ThreeSixtyImagesLoadingState {
    NOT_LOADED,
    LOADING,
    LOADED,
    ERROR
}

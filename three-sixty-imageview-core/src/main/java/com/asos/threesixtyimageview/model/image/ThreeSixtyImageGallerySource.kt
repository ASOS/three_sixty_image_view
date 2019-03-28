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
import com.asos.threesixtyimageview.model.AbstractThreeSixtyImageSource
import com.asos.threesixtyimageview.model.ImageLoadedListener
import com.asos.threesixtyimageview.model.ThreeSixtyImagesLoadingState.*
import com.asos.threesixtyimageview.model.ThumbnailLoadedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

private const val DEFAULT_THUMBNAIL_PROPORTION = 4

class ThreeSixtyImageGallerySource<T : ImageView>(private val context: Context,
                                                  private val imageBinder: ImageBinder<T>,
                                                  width: Int)
    : AbstractThreeSixtyImageSource<T>(), ImageLoadedListener, ThumbnailLoadedListener, CoroutineScope {


    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default

    var imageUrls: List<String> = emptyList()
        set(value) {
            if (field == value && loadingState == LOADED) {
                return
            }
            field = value
            reset(field)
        }

    var thumbnailProportionOfImageSize = DEFAULT_THUMBNAIL_PROPORTION

    private val fullWidth: Int = width
    private val thumbnailWidth: Int
        get() = fullWidth / thumbnailProportionOfImageSize

    private var thumbnailSizeImageCount = 0
    private var imageBitmapsArray: Array<Bitmap?> = emptyArray()

    val currentImageUrl: String? get() = imageUrls.getOrNull(currentIndex)

    override fun loadImages() {
        if (loadingState == LOADED) {
            // If images are already loaded, no need to load them again
            fireLoadingStateChanged(loadingState)
            return
        }
        fireLoadingStateChanged(LOADING)
        fetchImages(false)
    }

    override fun createImageView(): T = imageBinder.createImageView(context)

    override fun bindImageAtIndex(index: Int, imageView: T) {
        imageBinder.bindImage(imageUrls[index], imageView, fullWidth, thumbnailWidth, imageBitmapsArray[index])
    }

    override fun getCurrentImagesCount(): Int = imageBitmapsArray.size

    override fun clear() {
        super.clear()
        reset()
        job.cancel()
    }

    override fun cancelImageLoading() {
        if (loadingState == LOADING) {
            reset()
        }
    }

    private fun fetchImages(withFullWidth: Boolean) {
        imageUrls.forEachIndexed { index, imageUrl ->
            // We are launching each fetch in a different thread to make sure error on one can stop the others (LAA-7440)
            launch {
                // We stop loading if we we have been reset to NOT_LOADED or ERROR state
                if (loadingState == NOT_LOADED || loadingState == ERROR) {
                    return@launch
                }

                if (withFullWidth) {
                    imageBinder.loadFullSizeImage(this@ThreeSixtyImageGallerySource.context,
                            imageUrl,
                            fullWidth,
                            index,
                            this@ThreeSixtyImageGallerySource)
                } else {
                    imageBinder.loadThumbnailImage(this@ThreeSixtyImageGallerySource.context,
                            imageUrl,
                            thumbnailWidth,
                            index,
                            this@ThreeSixtyImageGallerySource)
                }
            }
        }
    }

    override fun onImageLoadedSuccessfully(bitmap: Bitmap?, index: Int) {
        // Do nothing
    }

    override fun onImageFailed() {
        // Do nothing
    }

    override fun onThumbnailLoadedSuccessfully(bitmap: Bitmap?, index: Int) {
        saveThumbnail(bitmap, index)
    }

    override fun onThumbnailFailed() {
        reset()
        fireLoadingStateChanged(ERROR)
    }

    private fun saveThumbnail(bitmap: Bitmap?, index: Int) {
        bitmap?.let {
            if (bitmapPresentForIndex(it, index)) {
                // If we already have a bitmap for this index, we need to recycle it before replacing it
                imageBitmapsArray[index]?.recycle()
                // We save a new copy Fresco's bitmap here as we do not want Fresco to recycle these while we're using
                imageBitmapsArray[index] = Bitmap.createBitmap(it)

                if (++thumbnailSizeImageCount == imageBitmapsArray.size) {
                    fireLoadingStateChanged(LOADED)
                    fetchImages(true)
                }
            }
        }
    }

    private fun bitmapPresentForIndex(bitmap: Bitmap, index: Int) =
            !bitmap.isRecycled && index >= 0 && index < imageBitmapsArray.size

    @Synchronized
    private fun reset(imageUrls: List<String> = emptyList()) {
        imageBinder.cancelAllImageRequests()
        fireLoadingStateChanged(NOT_LOADED)
        imageBitmapsArray.forEach { it?.recycle() }
        imageBitmapsArray = Array(imageUrls.size) { null }
        thumbnailSizeImageCount = 0
    }
}
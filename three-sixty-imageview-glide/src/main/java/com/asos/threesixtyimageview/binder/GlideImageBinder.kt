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

package com.asos.threesixtyimageview.binder

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import com.asos.threesixtyimageview.model.ImageLoadedListener
import com.asos.threesixtyimageview.model.ThumbnailLoadedListener
import com.asos.threesixtyimageview.model.image.ImageBinder
import com.asos.threesixtyimageview.model.image.ImageUrlResolver
import com.asos.threesixtyimageview.ui.getDefaultImageViewFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target

/**
 * Image binding logic for implementations of the ThreeSixty gallery
 * that depends on Fresco to retrieve the images.
 */
class GlideImageBinder(private val imageUrlResolver: ImageUrlResolver) : ImageBinder<ImageView> {

    private val cancelActionsByTargets: MutableMap<Target<Bitmap>, (target:Target<Bitmap>) -> Unit> = mutableMapOf()

    override fun createImageView(context: Context): ImageView = getDefaultImageViewFactory(context).invoke()

    override fun loadThumbnailImage(
        context: Context,
        imageUrl: String,
        imageWidth: Int,
        index: Int,
        imageLoadedListener: ThumbnailLoadedListener) {
        val options = RequestOptions().priority(Priority.HIGH)
        loadImage(context = context,
            imageUrl = imageUrlResolver.resolve(imageUrl, imageWidth),
            options = options,
            onSuccess = { imageLoadedListener.onThumbnailLoadedSuccessfully(it, index) },
            onError = { imageLoadedListener.onThumbnailFailed() })

    }

    override fun loadFullSizeImage(
        context: Context,
        imageUrl: String,
        imageWidth: Int,
        index: Int,
        imageLoadedListener: ImageLoadedListener) {
        val options = RequestOptions().priority(Priority.HIGH)
        loadImage(context = context,
            imageUrl = imageUrlResolver.resolve(imageUrl, imageWidth),
            options = options,
            onSuccess = { imageLoadedListener.onImageLoadedSuccessfully(it, index) },
            onError = { imageLoadedListener.onImageFailed() })

    }

    override fun bindImage( imageUrl: String, imageView: ImageView, screenWidth: Int, thumbnailWidth: Int, bitmap: Bitmap?) {
        bitmap?.let { imageView.setImageBitmap(it) }
    }

    @Synchronized
    override fun cancelAllImageRequests() {
        cancelActionsByTargets.entries.forEach { (target, cancelAction) ->
            target.request?.takeIf { it.isRunning }?.let {
                cancelAction(target)
            }
        }
        cancelActionsByTargets.clear()
    }

    private fun loadImage(context: Context, imageUrl: String, options: RequestOptions, onSuccess: (bitmap: Bitmap) -> Unit, onError: () -> Unit) {
        val requestManager = Glide.with(context)
        val target: Target<Bitmap> = requestManager
            .asBitmap()
            .load(imageUrl)
            .apply(options)
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    onError()
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    return if (resource != null) {
                        onSuccess(resource)
                        true
                    } else {
                        onError()
                        false
                    }
                }
            })
            .preload()
        cancelActionsByTargets[target] = { requestManager.clear(it) }
    }

}

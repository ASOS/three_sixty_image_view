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

package com.asos.threesixtyimageview.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.widget.ImageView
import com.asos.threesixtyimageview.model.ImageLoadedListener
import com.asos.threesixtyimageview.model.ThumbnailLoadedListener
import com.asos.threesixtyimageview.model.image.ImageBinder
import com.asos.threesixtyimageview.model.image.ImageUrlResolver
import java.net.URL


class SimpleImageBinder(private val imageUrlResolver: ImageUrlResolver) : ImageBinder<ImageView> {

    private val downloadTasks: MutableList<DownloadImageTask?> = mutableListOf()

    override fun createImageView(context: Context): ImageView = getDefaultImageViewFactory(context).invoke()

    override fun bindImage(imageUrl: String, imageView: ImageView, screenWidth: Int, thumbnailWidth: Int, bitmap: Bitmap?) {
        bitmap?.let {
            imageView.setImageBitmap(it)
        }
    }

    override fun loadThumbnailImage(context: Context, imageUrl: String, imageWidth: Int, index: Int, imageLoadedListener: ThumbnailLoadedListener) {
        loadImage(imageUrl,
                imageWidth,
                { imageLoadedListener.onThumbnailLoadedSuccessfully(it, index) },
                { imageLoadedListener.onThumbnailFailed() })
    }

    override fun loadFullSizeImage(context: Context, imageUrl: String, imageWidth: Int, index: Int, imageLoadedListener: ImageLoadedListener) {
        loadImage(imageUrl,
                imageWidth,
                { imageLoadedListener.onImageLoadedSuccessfully(it, index) },
                { imageLoadedListener.onImageFailed() })
    }


    @Synchronized
    override fun cancelAllImageRequests() {
        downloadTasks.forEach {
            it?.cancel(true)
        }
        downloadTasks.clear()
    }

    @Synchronized
    private fun loadImage(imageUrl: String, imageWidth: Int, onSuccess: (bitmap: Bitmap) -> Unit, onError: () -> Unit) {
        val downloadImageTask = DownloadImageTask(onSuccess, onError)
        downloadTasks.add(downloadImageTask)
        downloadImageTask.execute(imageUrlResolver.resolve(imageUrl, imageWidth))
    }

}

class DownloadImageTask(private val onSuccess: (bitmap: Bitmap) -> Unit,
                        private val onError: () -> Unit) : AsyncTask<String, Void, Bitmap>() {

    override fun doInBackground(vararg urls: String): Bitmap? {
        return try {
            val inputStream = URL(urls[0]).openStream()
            BitmapFactory.decodeStream(inputStream)
        } catch (t: Throwable) {
            null
        }
    }

    override fun onPostExecute(result: Bitmap?) {
        result?.let { onSuccess(it) } ?: onError
    }
}
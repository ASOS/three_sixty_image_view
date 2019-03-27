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
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.widget.FrameLayout
import com.asos.threesixtyimageview.model.ImageLoadedListener
import com.asos.threesixtyimageview.model.ThumbnailLoadedListener
import com.asos.threesixtyimageview.model.image.ImageBinder
import com.asos.threesixtyimageview.model.image.ImageUrlResolver
import com.facebook.common.executors.UiThreadImmediateExecutorService
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.core.ImagePipeline
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import java.util.concurrent.Executor

/**
 * Image binding logic for implementations of the ThreeSixty gallery
 * that depends on Fresco to retrieve the images.
 */
class FrescoImageBinder(private val imageUrlResolver: ImageUrlResolver) : ImageBinder<SimpleDraweeView> {

    private val executor: Executor = UiThreadImmediateExecutorService.getInstance()
    private val imagePipeline: ImagePipeline by lazy { Fresco.getImagePipeline() }
    private val pendingImageDataSources: MutableList<DataSource<CloseableReference<CloseableImage>>?> = mutableListOf()

    override fun createImageView(context: Context): SimpleDraweeView =
            SimpleDraweeView(context).apply {
                val builder = GenericDraweeHierarchyBuilder(context.resources)
                hierarchy = builder
                        .setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER)
                        .setFadeDuration(0)
                        .build()
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

                // Needed to use this SimpleDraweeView in an Activity transition
                // See https://github.com/facebook/fresco/issues/1445 and LAA-7271 / LAA-7388
                setLegacyVisibilityHandlingEnabled(true)
            }

    override fun loadThumbnailImage(context: Context, imageUrl: String, imageWidth: Int, index: Int, imageLoadedListener: ThumbnailLoadedListener) {
        loadImage(context, imageUrl, imageWidth, object : BaseBitmapDataSubscriber() {
            override fun onNewResultImpl(bitmap: Bitmap?) {
                imageLoadedListener.onThumbnailLoadedSuccessfully(bitmap, index)
            }

            override fun onNewResult(dataSource: DataSource<CloseableReference<CloseableImage>>?) {
                super.onNewResult(dataSource)
                removeDataSource(dataSource)
            }

            override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>?) {
                removeDataSource(dataSource)
                imageLoadedListener.onThumbnailFailed()
            }
        })
    }

    override fun loadFullSizeImage(context: Context, imageUrl: String, imageWidth: Int, index: Int, imageLoadedListener: ImageLoadedListener) {
        loadImage(context, imageUrl, imageWidth,
                object : BaseBitmapDataSubscriber() {
                    override fun onNewResultImpl(bitmap: Bitmap?) {
                        imageLoadedListener.onImageLoadedSuccessfully(bitmap, index)
                    }

                    override fun onNewResult(dataSource: DataSource<CloseableReference<CloseableImage>>?) {
                        super.onNewResult(dataSource)
                        removeDataSource(dataSource)
                    }

                    override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>?) {
                        removeDataSource(dataSource)
                        imageLoadedListener.onImageFailed()
                    }
                })
    }

    @Synchronized
    override fun cancelAllImageRequests() {
        pendingImageDataSources.forEach { it?.close() }
        pendingImageDataSources.clear()
    }

    @Synchronized
    private fun loadImage(context: Context, imageUrl: String, imageWidth: Int, dataSubscriber: BaseBitmapDataSubscriber) {
        val dataSource = fetchDecodedImageFromPipeline(buildFrescoImageRequest(imageUrlResolver.resolve(imageUrl, imageWidth)), context)
        pendingImageDataSources.add(dataSource)
        dataSource.subscribe(dataSubscriber, executor)
    }

    private fun fetchDecodedImageFromPipeline(request: ImageRequest?, context: Context) =
            imagePipeline.fetchDecodedImage(request, context)

    override fun bindImage(imageUrl: String, imageView: SimpleDraweeView, screenWidth: Int, thumbnailWidth: Int, bitmap: Bitmap?) {
        val imageRequestAtIndex = buildFrescoImageRequest(imageUrlResolver.resolve(imageUrl, screenWidth))
        imagePipeline.prefetchToBitmapCache(imageRequestAtIndex, executor)
        imageView.controller = Fresco.newDraweeControllerBuilder()
                .setLowResImageRequest(buildFrescoImageRequest(imageUrlResolver.resolve(imageUrl, thumbnailWidth)))
                .setImageRequest(imageRequestAtIndex)
                .setOldController(imageView.controller)
                .setAutoPlayAnimations(false)
                .build()
        imageView.hierarchy.setPlaceholderImage(BitmapDrawable(imageView.context.resources, bitmap), ScalingUtils.ScaleType.FIT_CENTER)
    }

    private fun buildFrescoImageRequest(imageUrl: String) =
            ImageRequestBuilder.newBuilderWithSource(Uri.parse(imageUrl)).build()

    @Synchronized
    private fun removeDataSource(dataSource: DataSource<CloseableReference<CloseableImage>>?) {
        pendingImageDataSources.remove(dataSource)
    }
}
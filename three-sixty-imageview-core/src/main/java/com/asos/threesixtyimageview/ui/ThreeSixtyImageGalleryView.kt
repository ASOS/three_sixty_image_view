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
import androidx.core.view.GestureDetectorCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import android.widget.ImageView
import com.asos.threesixtyimageview.R
import com.asos.threesixtyimageview.model.ThreeSixtyImageSource
import com.asos.threesixtyimageview.model.ThreeSixtyImagesDirection
import com.asos.threesixtyimageview.presenter.FlingStrategy
import com.asos.threesixtyimageview.presenter.ThreeSixtyImageView
import com.asos.threesixtyimageview.presenter.ThreeSixtyImagesPresenter
import kotlinx.android.synthetic.main.layout_three_sixty_gallery_view.view.*
import kotlinx.coroutines.*

/**
 * 16ms is the default delay between two frames in the Android system.
 */
private const val DEFAULT_DELAY_BETWEEN_FRAMES_MS = 16

/**
 * A View that displays a gallery of sequential images.
 *
 * Uses the provided [ThreeSixtyImageSource] (via [setImageSource]) to retrieve images for display.
 */
class ThreeSixtyImageGalleryView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        FrameLayout(context, attrs, defStyleAttr), ThreeSixtyImageView, CoroutineScope {

    private lateinit var job: Job
    override val coroutineContext
        get() = job + Dispatchers.Main

    private val presenter: ThreeSixtyImagesPresenter by lazy { ThreeSixtyImagesPresenter(this) }
    private val gestureDetector: GestureDetectorCompat by lazy { GestureDetectorCompat(context, gestureListener) }
    private val gestureListener: ThreeSixtyImageViewGestureListener by lazy {
        val viewConfiguration = ViewConfiguration.get(context)
        val minimumTouchDistance = width / 100
        ThreeSixtyImageViewGestureListener(viewConfiguration.scaledMaximumFlingVelocity, minimumTouchDistance)
                .apply { threeSixtyImagesInteractionListener = presenter }
    }

    var onFrameClickListener: OnFrameClickListener? = null
    var onThreeSixtyLoadListener: OnThreeSixtyLoadListener? = null
    var onImageUpdateListener: OnImageUpdateListener? = null

    private var loadingView: View

    init {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.layout_three_sixty_gallery_view, this, true)

        loadingView = createLoadingView(context, attrs, defStyleAttr, inflater)
        addView(loadingView)

        setUpTouchInteractions()
    }

    private fun createLoadingView(context: Context, attrs: AttributeSet?, defStyleAttr: Int, inflater: LayoutInflater): View {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ThreeSixtyImageGalleryView, defStyleAttr, 0)
        try {
            val loadingViewLayoutId = typedArray.getResourceId(R.styleable.ThreeSixtyImageGalleryView_loadingView, R.layout.default_loading_view)

            return inflater.inflate(loadingViewLayoutId, this, false).apply {
                // Override the ID to avoid side-effects on the layout that includes ThreeSixtyImageViewer
                id = View.generateViewId()
            }
        } finally {
            typedArray.recycle()
        }
    }

    private fun setUpTouchInteractions() {
        setOnTouchListener { _, event ->
            parent.requestDisallowInterceptTouchEvent(true)
            gestureDetector.onTouchEvent(event)
            return@setOnTouchListener true
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        job = Job()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        presenter.clear()
        job.cancel()
    }

    override fun setLoading(loading: Boolean) {
        loadingView.visibility = if (loading) View.VISIBLE else View.GONE
    }

    override fun updateImage(direction: ThreeSixtyImagesDirection) {
        val imageView: ImageView = imageSwitcher.nextView as ImageView
        presenter.imageSource?.bindNextImage(direction, imageView)
        imageSwitcher.showNext()
        onImageUpdateListener?.onImageUpdate(direction)
    }

    override fun onFrameClick() {
        onFrameClickListener?.onFrameClick(imageSwitcher.currentView)
    }

    override fun onLoadComplete() {
        onThreeSixtyLoadListener?.onLoadComplete()
    }

    override fun onLoadError() {
        onThreeSixtyLoadListener?.onLoadError()
    }

    @JvmOverloads
    fun rotate(direction: ThreeSixtyImagesDirection, frameCount: Int = 1, speedFactor: Float = 1f, onCompletion: () -> Unit = {}) {
        val delayBetweenFramesMs = (DEFAULT_DELAY_BETWEEN_FRAMES_MS / speedFactor).toLong()
        launch {
            repeat(frameCount) {
                launch(Dispatchers.Main) { updateImage(direction) }
                delay(delayBetweenFramesMs)
            }
            launch(Dispatchers.Main) { onCompletion.invoke() }
        }
    }

    fun setImageSource(threeSixtyImageSource: ThreeSixtyImageSource) {
        presenter.imageSource = threeSixtyImageSource
        imageSwitcher.setFactory(threeSixtyImageSource.getImageViewFactory(context))
    }

    fun setFlingStrategy(flingStrategy: FlingStrategy) {
        presenter.flingStrategy = flingStrategy
    }

    fun hasImageSource(): Boolean = presenter.imageSource != null
    val currentDirection: ThreeSixtyImagesDirection
        get() = gestureListener.direction
}
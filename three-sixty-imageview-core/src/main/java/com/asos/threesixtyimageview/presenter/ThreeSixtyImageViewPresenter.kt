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

package com.asos.threesixtyimageview.presenter

import com.asos.threesixtyimageview.model.ThreeSixtyImageSource
import com.asos.threesixtyimageview.model.ThreeSixtyImagesDirection
import com.asos.threesixtyimageview.model.ThreeSixtyImagesDirection.DIRECTION_NONE
import com.asos.threesixtyimageview.model.ThreeSixtyImagesLoadingState
import com.asos.threesixtyimageview.model.ThreeSixtyImagesLoadingState.*
import com.asos.threesixtyimageview.model.ThreeSixtyImagesLoadingStateListener
import com.asos.threesixtyimageview.ui.ThreeSixtyImagesInteractionListener
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

internal class ThreeSixtyImagesPresenter(private val view: ThreeSixtyImageView,
                                         private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main) : ThreeSixtyImagesInteractionListener,
        ThreeSixtyImagesLoadingStateListener, CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + uiDispatcher

    var imageSource: ThreeSixtyImageSource? = null
        set(value) {
            field?.removeLoadingStateListener(this)
            field = value
            field?.addLoadingStateListener(this)
        }
    
    var flingStrategy: FlingStrategy = DefaultFlingStrategy()

    private var flinging: Boolean = false


    override suspend fun onFlingStart(direction: ThreeSixtyImagesDirection, velocityPercentage: Int) {
        if (direction == DIRECTION_NONE || velocityPercentage < flingStrategy.getFlingMinimumVelocityPercentage()) {
            return
        }
        flinging = true
        imageSource?.let {
            (1..flingStrategy.getNumberOfSpins(velocityPercentage) * it.getCurrentImagesCount()).forEach {
                if (flinging) {
                    updateImage(direction)
                    delay(flingStrategy.getSpinDelayTime(it, velocityPercentage))
                } else {
                    return
                }
            }
        }
    }

    override fun onFlingStop() {
        flinging = false
    }

    override fun onDrag(direction: ThreeSixtyImagesDirection) {
        updateImage(direction)
    }

    override fun onLoadingStateChanged(state: ThreeSixtyImagesLoadingState) {
        launch(uiDispatcher) {
            when (state) {
                NOT_LOADED, LOADING -> view.setLoading(true)
                LOADED -> {
                    view.setLoading(false)
                    updateImage()
                    view.onLoadComplete()
                }
                ERROR -> {
                    view.setLoading(false)
                    view.onLoadError()
                }
            }
        }
    }

    override fun onFrameClick() {
        launch(uiDispatcher) {
            view.onFrameClick()
        }
    }

    fun clear() {
        imageSource?.clear()
        job.cancel()
    }

    private fun updateImage(direction: ThreeSixtyImagesDirection = DIRECTION_NONE) {
        launch(uiDispatcher) { view.updateImage(direction) }
    }
}

internal interface ThreeSixtyImageView {
    fun updateImage(direction: ThreeSixtyImagesDirection = DIRECTION_NONE)
    fun setLoading(loading: Boolean)
    fun onFrameClick()
    fun onLoadComplete()
    fun onLoadError()
}

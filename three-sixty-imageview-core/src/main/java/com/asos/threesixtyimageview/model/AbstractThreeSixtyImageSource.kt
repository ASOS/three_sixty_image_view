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
import androidx.annotation.VisibleForTesting
import android.widget.ImageView
import com.asos.threesixtyimageview.model.ThreeSixtyImagesLoadingState.NOT_LOADED

abstract class AbstractThreeSixtyImageSource<T : ImageView> : ThreeSixtyImageSource {

    private val listeners: MutableList<ThreeSixtyImagesLoadingStateListener> = mutableListOf()
    var currentIndex: Int = 0
        private set

    @VisibleForTesting
    var loadingState: ThreeSixtyImagesLoadingState = NOT_LOADED

    abstract override fun loadImages()

    abstract override fun cancelImageLoading()

    protected abstract fun bindImageAtIndex(index: Int, imageView: T)

    final override fun getImageViewFactory(context: Context): () -> ImageView = {
        createImageView()
    }

    final override fun bindNextImage(direction: ThreeSixtyImagesDirection, imageView: ImageView) {
        when (direction) {
            ThreeSixtyImagesDirection.DIRECTION_LEFT -> {
                if (currentIndex == 0) {
                    currentIndex = getLastImageIndex()
                } else {
                    currentIndex--
                }
            }
            ThreeSixtyImagesDirection.DIRECTION_RIGHT -> {
                // If index reaches maximum reset it
                if (currentIndex == getLastImageIndex()) {
                    currentIndex = 0
                } else {
                    currentIndex++
                }
            }
            else -> {
                // Stay on the same index
            }
        }

        if (currentIndex < 0 || currentIndex > getLastImageIndex()) {
            return
        }

        @Suppress("UNCHECKED_CAST")
        bindImageAtIndex(currentIndex, imageView as T)
    }

    override fun addLoadingStateListener(listener: ThreeSixtyImagesLoadingStateListener) {
        listeners.add(listener)
        // Send latest loading state to new listener
        listener.onLoadingStateChanged(loadingState)
    }

    override fun removeLoadingStateListener(listener: ThreeSixtyImagesLoadingStateListener) {
        listeners.remove(listener)
    }

    override fun clear() {
        listeners.clear()
    }

    protected fun fireLoadingStateChanged(state: ThreeSixtyImagesLoadingState) {
        loadingState = state
        listeners.forEach {
            it.onLoadingStateChanged(state)
        }
    }

    protected abstract fun createImageView(): T

    private fun getLastImageIndex(): Int = getCurrentImagesCount() - 1

}


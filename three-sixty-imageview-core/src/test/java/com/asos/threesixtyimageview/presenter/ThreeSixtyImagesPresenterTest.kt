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

import android.graphics.drawable.Drawable
import com.asos.threesixtyimageview.model.ThreeSixtyImageSource
import com.asos.threesixtyimageview.model.ThreeSixtyImagesDirection
import com.asos.threesixtyimageview.model.ThreeSixtyImagesLoadingState
import kotlinx.coroutines.Dispatchers.Unconfined
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

private const val numberOfImages = 42

@RunWith(MockitoJUnitRunner::class)
class ThreeSixtyImagesPresenterTest {

    @Mock
    private lateinit var view: ThreeSixtyImageView

    @Mock
    lateinit var threeSixtyImageSource: ThreeSixtyImageSource

    @Mock
    lateinit var drawable: Drawable

    private lateinit var underTest: ThreeSixtyImagesPresenter

    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        underTest = ThreeSixtyImagesPresenter(view, Unconfined)

        BDDMockito.given(threeSixtyImageSource.getCurrentImagesCount()).willReturn(numberOfImages)
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(view, threeSixtyImageSource)
    }

    @Test
    fun settingImageSource_addsTheListener() {
        underTest.imageSource = threeSixtyImageSource

        verify(threeSixtyImageSource).addLoadingStateListener(underTest)
    }

    @Test
    fun onFlingStart_left() {
        verifyFling(ThreeSixtyImagesDirection.DIRECTION_LEFT)
    }

    @Test
    fun onFlingStart_right() {
        verifyFling(ThreeSixtyImagesDirection.DIRECTION_RIGHT)
    }

    @Test
    fun onFlingStart_noDirection_doesNothing() {
        val direction = ThreeSixtyImagesDirection.DIRECTION_NONE

        runBlocking {
            underTest.onFlingStart(direction, 100)
        }

        // Does nothing (which tearDown() will assert)
    }

    @Test
    fun onFlingStart_noImagesSource_doesNothing() {
        val direction = ThreeSixtyImagesDirection.DIRECTION_RIGHT

        runBlocking {
            underTest.onFlingStart(direction, 100)
        }

        // Does nothing (which tearDown() will assert)
    }

    @Test
    fun onFlingStop_willInterruptFling() {
        underTest.imageSource = threeSixtyImageSource
        verify(threeSixtyImageSource).addLoadingStateListener(underTest)
        runBlocking {
            val test = launch {
                underTest.onFlingStart(ThreeSixtyImagesDirection.DIRECTION_RIGHT, 50)
            }
            delay(10)

            underTest.onFlingStop()

            test.join()
        }
        verify(threeSixtyImageSource).getCurrentImagesCount()
        val maxNumberOfImagesShown = numberOfImages / 3
        verify(view, atMost(maxNumberOfImagesShown)).updateImage(ThreeSixtyImagesDirection.DIRECTION_RIGHT)

    }

    @Test
    fun onDrag() {
        val direction = ThreeSixtyImagesDirection.DIRECTION_LEFT
        underTest.imageSource = threeSixtyImageSource
        verify(threeSixtyImageSource).addLoadingStateListener(underTest)

        underTest.onDrag(direction)

        verify(view).updateImage(direction)
    }

    @Test
    fun clear_withImageSource_removesTheListener() {
        underTest.imageSource = threeSixtyImageSource
        verify(threeSixtyImageSource).addLoadingStateListener(underTest)

        underTest.clear()

        verify(threeSixtyImageSource).clear()
    }

    @Test
    fun clear_withNoImageSource_doesNothing() {
        underTest.clear()

        // Does nothing (which tearDown() will assert)
    }

    @Test
    fun onFrameClick_tellTheView() {
        underTest.onFrameClick()
        verify(view).onFrameClick()
    }

    @Test
    fun onLoadingStateChanged_NOT_LOADED_displaysLoading() {
        underTest.onLoadingStateChanged(ThreeSixtyImagesLoadingState.NOT_LOADED)

        verify(view).setLoading(true)
    }

    @Test
    fun onLoadingStateChanged_LOADING_displaysLoading() {
        underTest.onLoadingStateChanged(ThreeSixtyImagesLoadingState.LOADING)

        verify(view).setLoading(true)
    }

    @Test
    fun onLoadingStateChanged_LOADED_updatesImage() {
        underTest.onLoadingStateChanged(ThreeSixtyImagesLoadingState.LOADED)

        verify(view).setLoading(false)
        verify(view).updateImage(ThreeSixtyImagesDirection.DIRECTION_NONE)
        verify(view).onLoadComplete()
    }

    @Test
    fun onLoadingStateChanged_ERROR_notifiesView() {
        underTest.onLoadingStateChanged(ThreeSixtyImagesLoadingState.ERROR)

        verify(view).setLoading(false)
        verify(view).onLoadError()
    }

    private fun verifyFling(direction: ThreeSixtyImagesDirection) {
        underTest.imageSource = threeSixtyImageSource
        verify(threeSixtyImageSource).addLoadingStateListener(underTest)

        runBlocking {
            underTest.onFlingStart(direction, 100)
        }

        verify(threeSixtyImageSource).getCurrentImagesCount()
        verify(view, atLeast(numberOfImages)).updateImage(direction)
    }
}
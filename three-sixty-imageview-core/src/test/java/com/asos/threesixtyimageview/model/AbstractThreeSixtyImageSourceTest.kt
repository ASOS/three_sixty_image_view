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

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.asos.threesixtyimageview.model.ThreeSixtyImagesDirection.*
import com.asos.threesixtyimageview.model.ThreeSixtyImagesLoadingState.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.*
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AbstractThreeSixtyImageSourceTest {

    @Mock
    private lateinit var imageAccessVerifier: ImageAccessVerifierForTest
    @Mock
    private lateinit var loadingStateListener: ThreeSixtyImagesLoadingStateListener
    @Mock
    private lateinit var imageView: ImageView

    private lateinit var underTest: AbstractThreeSixtyImageSource<ImageView>

    @Before
    fun setUp() {

        underTest = object : AbstractThreeSixtyImageSource<ImageView>() {
            override fun createImageView(): ImageView {
                return imageView
            }

            override fun loadImages() {
                fireLoadingStateChanged(ThreeSixtyImagesLoadingState.LOADED)
            }

            override fun cancelImageLoading() {
                fireLoadingStateChanged(ThreeSixtyImagesLoadingState.NOT_LOADED)
            }

            override fun bindImageAtIndex(index:Int, imageView: ImageView) {
                imageAccessVerifier.bindImageAtIndex(index, imageView)
            }

            override fun getCurrentImagesCount(): Int {
                return imageAccessVerifier.getCurrentImagesCount()
            }
        }
    }


    @After
    fun tearDown() {
        Mockito.verifyNoMoreInteractions(imageAccessVerifier, loadingStateListener)
    }

    @Test
    fun getNextImage_withDirectionNone_willReturnFirstImage() {
        given(imageAccessVerifier.getCurrentImagesCount()).willReturn(2)

        underTest.bindNextImage(DIRECTION_NONE, imageView)

        verify(imageAccessVerifier, atLeastOnce()).getCurrentImagesCount()
        verify(imageAccessVerifier).bindImageAtIndex(0, imageView)
    }

    @Test
    fun getNextImage_withDirectionRight_willReturnSecondImage() {
        given(imageAccessVerifier.getCurrentImagesCount()).willReturn(2)

        underTest.bindNextImage(DIRECTION_RIGHT, imageView)

        verify(imageAccessVerifier, atLeastOnce()).getCurrentImagesCount()
        verify(imageAccessVerifier).bindImageAtIndex(1, imageView)
    }

    @Test
    fun getNextImage_withDirectionLeft_willReturnLastImage() {
        given(imageAccessVerifier.getCurrentImagesCount()).willReturn(3)

        underTest.bindNextImage(DIRECTION_LEFT, imageView)

        verify(imageAccessVerifier, atLeastOnce()).getCurrentImagesCount()
        verify(imageAccessVerifier).bindImageAtIndex(2, imageView)
    }

    @Test
    fun addLoadingStateListener_sendLastLoadingStateToListener() {
        val listener = mock(ThreeSixtyImagesLoadingStateListener::class.java)

        underTest.addLoadingStateListener(listener)

        verify(listener).onLoadingStateChanged(NOT_LOADED)
    }

    @Test
    fun loadingStateListeners_added_will_receiveEvents() {
        val listener1 = mock(ThreeSixtyImagesLoadingStateListener::class.java)
        val listener2 = mock(ThreeSixtyImagesLoadingStateListener::class.java)
        givenOnImageLoadedStateListenerIsAdded(listener1)
        givenOnImageLoadedStateListenerIsAdded(listener2)

        whenOnLoadingStateChangedEventIsTriggered()

        verify(listener1).onLoadingStateChanged(LOADED)
        verify(listener2).onLoadingStateChanged(LOADED)
    }

    @Test
    fun clear_removesListener() {
        val listener1 = mock(ThreeSixtyImagesLoadingStateListener::class.java)
        val listener2 = mock(ThreeSixtyImagesLoadingStateListener::class.java)
        givenOnImageLoadedStateListenerIsAdded(listener1)
        givenOnImageLoadedStateListenerIsAdded(listener2)

        underTest.clear()

        whenOnLoadingStateChangedEventIsTriggered()

        // Nothing happens, listeners are not called
        Mockito.verifyNoMoreInteractions(listener1, listener2)
    }

    private fun whenOnLoadingStateChangedEventIsTriggered() {
        // Our implementation in setup will fire the event to listeners (needed to use the protected fire method)
        underTest.loadImages()
    }

    private fun givenOnImageLoadedStateListenerIsAdded(listener1: ThreeSixtyImagesLoadingStateListener) {
        underTest.addLoadingStateListener(listener1)
        verify(listener1).onLoadingStateChanged(NOT_LOADED)
    }

}

interface ImageAccessVerifierForTest {
    fun bindImageAtIndex(index: Int, imageView: ImageView): Drawable
    fun getCurrentImagesCount(): Int
}

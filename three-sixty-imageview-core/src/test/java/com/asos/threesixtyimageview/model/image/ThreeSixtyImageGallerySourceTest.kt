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
import android.widget.ImageView
import com.asos.threesixtyimageview.model.ThreeSixtyImagesLoadingState
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

private const val URL1 = "Url1"
private const val URL2 = "Url2"
private const val URL3 = "Url3"

@RunWith(MockitoJUnitRunner::class)
class ThreeSixtyImageGallerySourceTest {

    @Mock
    private lateinit var context:Context

    @Mock
    private lateinit var imageBinder:ImageBinder<ImageView>

    private lateinit var underTest: ThreeSixtyImageGallerySource<ImageView>

    @Before
    fun setUp() {
        underTest = ThreeSixtyImageGallerySource(context, imageBinder, 500)
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(context, imageBinder)
    }

    @Test
    fun onLoadImage_cancelsPreviousOnGoingRequests() {
        underTest.imageUrls = listOf(URL1, URL2, URL3)

        verify(imageBinder).cancelAllImageRequests()
    }

    @Test
    fun clear_cancelsOnGoingRequests() {
        underTest.clear()

        verify(imageBinder).cancelAllImageRequests()
    }

    @Test
    fun cancelImageLoading_cancelsOnGoingRequests_whenStateIsLoading() {
        underTest.loadingState = ThreeSixtyImagesLoadingState.LOADING

        underTest.cancelImageLoading()

        verify(imageBinder).cancelAllImageRequests()
    }

    @Test
    fun cancelImageLoading_doesNothing_whenStateIsNotLoading() {
        underTest.loadingState = ThreeSixtyImagesLoadingState.LOADED

        underTest.cancelImageLoading()

        verifyNoMoreInteractions(imageBinder)
    }

    @Test
    fun onThumbnailFailed_cancelsOnGoingRequests() {
        underTest.imageUrls = listOf(URL1, URL2, URL3)
        reset(imageBinder)

        underTest.onThumbnailFailed()

        verify(imageBinder).cancelAllImageRequests()
    }
}
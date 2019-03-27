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

import android.view.GestureDetector
import android.view.MotionEvent
import com.asos.threesixtyimageview.model.ThreeSixtyImagesDirection
import com.asos.threesixtyimageview.model.ThreeSixtyImagesDirection.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

internal class ThreeSixtyImageViewGestureListener(private val maximumFlingVelocity: Int, private val minimumTouchDistance: Int) : GestureDetector.SimpleOnGestureListener() {

    var threeSixtyImagesInteractionListener: ThreeSixtyImagesInteractionListener? = null

    var direction: ThreeSixtyImagesDirection = DIRECTION_NONE
        private set

    private var flingInterruptedByUser = false
    private var nonConsumedScrollX: Float = 0f

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        if (!flingInterruptedByUser) {
            threeSixtyImagesInteractionListener?.onFrameClick()
            return true
        }
        flingInterruptedByUser = false
        return false
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        val velocity: Int = ((velocityX.absoluteValue / maximumFlingVelocity) * 100).toInt()
        direction = getDirectionFromDistanceX(velocityX * -1)
        GlobalScope.launch {
            threeSixtyImagesInteractionListener?.onFlingStart(direction, velocity)
            stopFling()
        }

        return true
    }

    override fun onDown(event: MotionEvent): Boolean {
        flingInterruptedByUser = false
        if (direction != DIRECTION_NONE) {
            stopFling()
            flingInterruptedByUser = true
            return true
        }
        return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        nonConsumedScrollX += distanceX

        return if (nonConsumedScrollX.absoluteValue < minimumTouchDistance) {
            false

        } else {
            direction = getDirectionFromDistanceX(nonConsumedScrollX)
            nonConsumedScrollX = 0f

            threeSixtyImagesInteractionListener?.onDrag(direction)
            direction = DIRECTION_NONE

            true
        }
    }


    private fun stopFling() {
        threeSixtyImagesInteractionListener?.onFlingStop()
        direction = DIRECTION_NONE
    }

    private fun getDirectionFromDistanceX(distanceX: Float) =
            if (distanceX > 0) {
                DIRECTION_LEFT
            } else {
                DIRECTION_RIGHT
            }

}

internal interface ThreeSixtyImagesInteractionListener {
    suspend fun onFlingStart(direction: ThreeSixtyImagesDirection, velocityPercentage: Int)
    fun onFlingStop()
    fun onDrag(direction: ThreeSixtyImagesDirection)
    fun onFrameClick()
}
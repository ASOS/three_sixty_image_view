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

private const val FLING_MINIMUM_VELOCITY_PERCENTAGE = 5
private const val FLING_MIN_NUMBER_OF_SPINS = 1
private const val FLING_MAX_NUMBER_OF_EXTRA_SPINS = 4
private const val FLING_BASE_DELAY_TIME_MILLIS = 25
private const val FLING_DELAY_SLOWDOWN_FACTOR = 0.1

class DefaultFlingStrategy : FlingStrategy {
    override fun getSpinDelayTime(imageIndex: Int, velocityPercentage: Int): Long = getFlingBaseDelay(velocityPercentage) + getSlowdownFactor(imageIndex)

    override fun getNumberOfSpins(velocityPercentage: Int): Int = FLING_MIN_NUMBER_OF_SPINS + ((FLING_MAX_NUMBER_OF_EXTRA_SPINS * velocityPercentage) / 100)

    override fun getFlingMinimumVelocityPercentage(): Int = FLING_MINIMUM_VELOCITY_PERCENTAGE

    private fun getFlingBaseDelay(velocityPercentage: Int): Long = FLING_BASE_DELAY_TIME_MILLIS - getSpeedImprovementFromVelocity(velocityPercentage)

    private fun getSlowdownFactor(imageIndex: Int): Long = (FLING_DELAY_SLOWDOWN_FACTOR * imageIndex).toLong()

    private fun getSpeedImprovementFromVelocity(velocityPercentage: Int): Long = (20L * velocityPercentage) / 100L
}
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

/**
 * A means for transforming a given image URL into another based on attributes derived
 * from the URL itself. A good example of this would be transforming a thumbnail URL
 * to that of a full sized image.
 */
interface ImageUrlResolver {

    fun resolve(imageUrl: String, screenWidth: Int): String
}
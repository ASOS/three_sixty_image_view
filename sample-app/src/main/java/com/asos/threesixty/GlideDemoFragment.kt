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

package com.asos.threesixty

import android.widget.ImageView
import com.asos.threesixtyimageview.binder.GlideImageBinder
import com.asos.threesixtyimageview.model.image.ImageBinder


/**
 * Simple way to demo the 360 Image viewer with a simple custom ImageBinder and android ImageViews.
 */
class GlideDemoFragment : AbstractDemoFragment<ImageView>() {

    companion object {
        fun newInstance() = GlideDemoFragment()
    }

    override fun createImageBinder(): ImageBinder<ImageView> = GlideImageBinder(SampleImageResolver())

}

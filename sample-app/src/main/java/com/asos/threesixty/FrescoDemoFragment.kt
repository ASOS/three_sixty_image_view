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

import com.asos.threesixtyimageview.binder.FrescoImageBinder
import com.asos.threesixtyimageview.model.image.ImageBinder
import com.facebook.drawee.view.SimpleDraweeView


/**
 * Simple way to demo the 360 Image viewer with a FrescoImageBinder and SimpleDraweeViews.
 */
class FrescoDemoFragment : AbstractDemoFragment<SimpleDraweeView>() {

    companion object {
        fun newInstance() = FrescoDemoFragment()
    }

    override fun createImageBinder(): ImageBinder<SimpleDraweeView> = FrescoImageBinder(SampleImageResolver())

}
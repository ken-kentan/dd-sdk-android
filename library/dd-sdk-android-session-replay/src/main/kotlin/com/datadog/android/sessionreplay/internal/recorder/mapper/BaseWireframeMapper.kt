/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.android.sessionreplay.internal.recorder.mapper

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.view.View
import com.datadog.android.sessionreplay.internal.recorder.GlobalBounds
import com.datadog.android.sessionreplay.internal.recorder.ViewUtils
import com.datadog.android.sessionreplay.internal.utils.StringUtils
import com.datadog.android.sessionreplay.model.MobileSegment

internal abstract class BaseWireframeMapper<T : View, S : MobileSegment.Wireframe>(
    private val stringUtils: StringUtils = StringUtils,
    private val viewUtils: ViewUtils = ViewUtils()
) : WireframeMapper<T, S> {

    protected fun resolveViewId(view: View): Long {
        // we will use the System.identityHashcode in here which always returns the default
        // hashcode value whether or not a child class overrides this.
        return System.identityHashCode(view).toLong()
    }

    protected fun colorAndAlphaAsStringHexa(color: Int, alphaAsHexa: Int): String {
        return stringUtils.formatColorAndAlphaAsHexa(color, alphaAsHexa)
    }

    protected fun resolveViewGlobalBounds(view: View, pixelsDensity: Float):
        GlobalBounds {
        return viewUtils.resolveViewGlobalBounds(view, pixelsDensity)
    }

    protected fun Drawable.resolveShapeStyleAndBorder(viewAlpha: Float):
        Pair<MobileSegment.ShapeStyle?, MobileSegment.ShapeBorder?>? {
        return if (this is ColorDrawable) {
            val color = colorAndAlphaAsStringHexa(color, alpha)
            MobileSegment.ShapeStyle(color, viewAlpha) to null
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
            this is RippleDrawable &&
            numberOfLayers >= 1
        ) {
            getDrawable(0).resolveShapeStyleAndBorder(viewAlpha)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && this is InsetDrawable) {
            drawable?.resolveShapeStyleAndBorder(viewAlpha)
        } else {
            // We cannot handle this drawable so we will use a border to delimit its container
            // bounds.
            // TODO: RUMM-0000 In case the background drawable could not be handled we should
            // instead resolve it as an ImageWireframe.
            null to null
        }
    }

    companion object {
        internal const val OPAQUE_ALPHA_VALUE: Int = 255
    }
}

/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.android.core.internal.data.upload.v2

import com.datadog.android.api.context.DatadogContext
import com.datadog.android.core.internal.data.upload.UploadStatus
import com.datadog.tools.annotation.NoOpImplementation

@NoOpImplementation
internal interface DataUploader {
    fun upload(
        context: DatadogContext,
        batch: List<ByteArray>,
        batchMeta: ByteArray?
    ): UploadStatus
}

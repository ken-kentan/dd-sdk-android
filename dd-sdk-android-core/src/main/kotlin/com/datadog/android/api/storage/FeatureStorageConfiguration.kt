/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.android.api.storage

import com.datadog.android.core.configuration.BatchSize
import com.datadog.android.core.configuration.UploadFrequency

/**
 * Contains the storage configuration for an [FeatureScope] instance.
 * @property maxItemSize the maximum size (in bytes) for a single item in a batch
 * @property maxItemsPerBatch the maximum number of individual items in a batch
 * @property maxBatchSize the maximum size (in bytes) of a complete batch
 * @property oldBatchThreshold the duration (in milliseconds) after which a batch is considered too
 * old to be uploaded (usually because it'll be discarded at ingestion by the backend)
 * @property uploadFrequency the desired upload frequency policy. If not explicitly provided this
 * value will be taken from core configuration.
 * @property batchSize the desired batch size policy.If not explicitly provided this
 * value will be taken from core configuration.
 */
data class FeatureStorageConfiguration(
    val maxItemSize: Long,
    val maxItemsPerBatch: Int,
    val maxBatchSize: Long,
    val oldBatchThreshold: Long,
    val uploadFrequency: UploadFrequency?,
    val batchSize: BatchSize?
) {
    companion object {

        /**
         * Default storage configuration with the following parameters:
         * max item size = 512 KB,
         * max items per batch = 500,
         * max batch size = 4 MB,
         * old batch threshold = 18 hours.
         */
        val DEFAULT: FeatureStorageConfiguration = FeatureStorageConfiguration(
            // 512 KB
            maxItemSize = 512L * 1024,
            maxItemsPerBatch = 500,
            // 4 MB
            maxBatchSize = 4L * 1024 * 1024,
            // 18 hours
            oldBatchThreshold = 18L * 60L * 60L * 1000L,
            uploadFrequency = null,
            batchSize = null
        )
    }
}

/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.android.error.internal

import android.content.Context
import com.datadog.android.core.configuration.Configuration
import com.datadog.android.core.internal.CoreFeature
import com.datadog.android.core.internal.SdkFeature
import com.datadog.android.core.internal.net.DataUploader
import com.datadog.android.core.internal.persistence.PersistenceStrategy
import com.datadog.android.core.internal.utils.sdkLogger
import com.datadog.android.log.internal.domain.DatadogLogGenerator
import com.datadog.android.log.internal.net.LogsOkHttpUploaderV2
import com.datadog.android.log.model.LogEvent

internal class CrashReportsFeature(
    coreFeature: CoreFeature
) : SdkFeature<LogEvent, Configuration.Feature.CrashReport>(coreFeature) {

    internal var originalUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()

    // region SdkFeature

    override fun onInitialize(context: Context, configuration: Configuration.Feature.CrashReport) {
        setupExceptionHandler(context)
    }

    override fun onStop() {
        resetOriginalExceptionHandler()
    }

    override fun createPersistenceStrategy(
        context: Context,
        configuration: Configuration.Feature.CrashReport
    ): PersistenceStrategy<LogEvent> {
        return CrashReportFilePersistenceStrategy(
            coreFeature.trackingConsentProvider,
            coreFeature.storageDir,
            coreFeature.persistenceExecutorService,
            sdkLogger,
            coreFeature.localDataEncryption
        )
    }

    override fun createUploader(configuration: Configuration.Feature.CrashReport): DataUploader {
        return LogsOkHttpUploaderV2(
            configuration.endpointUrl,
            coreFeature.clientToken,
            coreFeature.sourceName,
            coreFeature.sdkVersion,
            coreFeature.okHttpClient,
            coreFeature.androidInfoProvider,
            sdkLogger
        )
    }

    override fun onPostInitialized(context: Context) { }

    // endregion

    // region Internal

    private fun setupExceptionHandler(
        appContext: Context
    ) {
        originalUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        DatadogExceptionHandler(
            DatadogLogGenerator(
                coreFeature.serviceName,
                DatadogExceptionHandler.LOGGER_NAME,
                coreFeature.networkInfoProvider,
                coreFeature.userInfoProvider,
                coreFeature.timeProvider,
                coreFeature.sdkVersion,
                coreFeature.envName,
                coreFeature.variant,
                coreFeature.packageVersionProvider
            ),
            writer = persistenceStrategy.getWriter(),
            appContext = appContext
        ).register()
    }

    private fun resetOriginalExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(originalUncaughtExceptionHandler)
    }

    // endregion

    companion object {
        internal const val CRASH_FEATURE_NAME = "crash"
    }
}

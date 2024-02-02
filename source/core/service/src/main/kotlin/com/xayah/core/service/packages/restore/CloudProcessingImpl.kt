package com.xayah.core.service.packages.restore

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CloudProcessingImpl @Inject constructor() : ProcessingService() {
    @Inject
    @ApplicationContext
    override lateinit var context: Context

    override val intent by lazy { Intent(context, CloudRestoreImpl::class.java) }
}

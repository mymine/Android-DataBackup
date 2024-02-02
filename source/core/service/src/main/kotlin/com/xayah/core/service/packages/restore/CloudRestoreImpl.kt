package com.xayah.core.service.packages.restore

import com.xayah.core.data.repository.CloudRepository
import com.xayah.core.data.repository.TaskRepository
import com.xayah.core.database.dao.PackageDao
import com.xayah.core.database.dao.TaskDao
import com.xayah.core.model.DataType
import com.xayah.core.model.OpType
import com.xayah.core.model.TaskType
import com.xayah.core.model.database.CloudEntity
import com.xayah.core.model.database.PackageEntity
import com.xayah.core.model.database.TaskDetailPackageEntity
import com.xayah.core.model.database.TaskEntity
import com.xayah.core.network.client.CloudClient
import com.xayah.core.rootservice.service.RemoteRootService
import com.xayah.core.service.util.PackagesRestoreUtil
import com.xayah.core.util.PathUtil
import com.xayah.core.util.localBackupSaveDir
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
internal class CloudRestoreImpl @Inject constructor() : RestoreService() {
    @Inject
    override lateinit var rootService: RemoteRootService

    @Inject
    override lateinit var pathUtil: PathUtil

    @Inject
    override lateinit var taskDao: TaskDao

    @Inject
    override lateinit var packageDao: PackageDao

    @Inject
    override lateinit var packagesRestoreUtil: PackagesRestoreUtil

    @Inject
    override lateinit var taskRepository: TaskRepository

    @Inject
    lateinit var cloudRepository: CloudRepository

    override val taskEntity by lazy {
        TaskEntity(
            id = 0,
            opType = OpType.RESTORE,
            taskType = TaskType.PACKAGE,
            startTimestamp = startTimestamp,
            endTimestamp = endTimestamp,
            backupDir = context.localBackupSaveDir(),
            rawBytes = 0.toDouble(),
            availableBytes = 0.toDouble(),
            totalBytes = 0.toDouble(),
            totalCount = 0,
            successCount = 0,
            failureCount = 0,
            isProcessing = true,
            cloud = "",
        )
    }

    private val tmpArchivesPackagesDir by lazy { pathUtil.getCloudTmpArchivesPackagesDir() }
    private val tmpDir by lazy { pathUtil.getCloudTmpDir() }

    private lateinit var cloudEntity: CloudEntity
    private lateinit var client: CloudClient
    private lateinit var remote: String
    private lateinit var remoteArchivesPackagesDir: String
    private lateinit var remoteConfigsDir: String

    override suspend fun createTargetDirs() {
        val pair = cloudRepository.getClient()
        cloudEntity = pair.second
        client = pair.first
        remote = cloudEntity.remote
        remoteArchivesPackagesDir = pathUtil.getCloudRemoteArchivesPackagesDir(remote)
        remoteConfigsDir = pathUtil.getCloudRemoteConfigsDir(remote)
        taskEntity.also {
            it.cloud = cloudEntity.name
            it.backupDir = remote
        }
    }

    override suspend fun restorePackage(p: PackageEntity) {
        val tmpDstDir = "${tmpArchivesPackagesDir}/${p.archivesPreserveRelativeDir}"
        val remoteSrcDir = "${remoteArchivesPackagesDir}/${p.archivesPreserveRelativeDir}"

        val t = TaskDetailPackageEntity(
            id = 0,
            taskId = taskEntity.id,
            packageEntity = p,
        ).apply {
            id = taskDao.upsert(this)
        }

        packagesRestoreUtil.download(client = client, p = p, t = t, dataType = DataType.PACKAGE_APK, srcDir = remoteSrcDir, dstDir = tmpDstDir) {
            packagesRestoreUtil.restoreApk(p = p, t = t, srcDir = tmpDstDir)
        }

        packagesRestoreUtil.download(client = client, p = p, t = t, dataType = DataType.PACKAGE_USER, srcDir = remoteSrcDir, dstDir = tmpDstDir) {
            packagesRestoreUtil.restoreData(p = p, t = t, dataType = DataType.PACKAGE_USER, srcDir = tmpDstDir)
        }

        packagesRestoreUtil.download(client = client, p = p, t = t, dataType = DataType.PACKAGE_USER_DE, srcDir = remoteSrcDir, dstDir = tmpDstDir) {
            packagesRestoreUtil.restoreData(p = p, t = t, dataType = DataType.PACKAGE_USER_DE, srcDir = tmpDstDir)
        }

        packagesRestoreUtil.download(client = client, p = p, t = t, dataType = DataType.PACKAGE_DATA, srcDir = remoteSrcDir, dstDir = tmpDstDir) {
            packagesRestoreUtil.restoreData(p = p, t = t, dataType = DataType.PACKAGE_DATA, srcDir = tmpDstDir)
        }

        packagesRestoreUtil.download(client = client, p = p, t = t, dataType = DataType.PACKAGE_OBB, srcDir = remoteSrcDir, dstDir = tmpDstDir) {
            packagesRestoreUtil.restoreData(p = p, t = t, dataType = DataType.PACKAGE_OBB, srcDir = tmpDstDir)
        }

        packagesRestoreUtil.download(client = client, p = p, t = t, dataType = DataType.PACKAGE_MEDIA, srcDir = remoteSrcDir, dstDir = tmpDstDir) {
            packagesRestoreUtil.restoreData(p = p, t = t, dataType = DataType.PACKAGE_MEDIA, srcDir = tmpDstDir)
        }

        packagesRestoreUtil.updatePackage(p = p)
        packagesRestoreUtil.restorePermissions(p = p)
        packagesRestoreUtil.restoreSsaid(p = p)

        t.apply {
            packageEntity = p
            taskDao.upsert(this)
        }
        taskEntity.also {
            if (t.isSuccess) it.successCount++ else it.failureCount++
            taskDao.upsert(it)
        }
    }

    override suspend fun clear() {
        rootService.deleteRecursively(tmpDir)
        client.disconnect()
    }
}

package com.rmtm.lifelog.data.remote

import android.content.Context
import android.accounts.Account
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.rmtm.lifelog.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val FOLDER_NAME = "LifeLog"
private const val DRIVE_SCOPE = "https://www.googleapis.com/auth/drive.file"

/**
 * Google Drive API와 통신하여 백업/복원 기능을 수행하는 서비스 클래스
 */
@Singleton
class GoogleDriveService @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private fun getDriveService(email: String): Drive {
        val account = Account(email, "com.google")
        val credential = GoogleAccountCredential.usingOAuth2(context, setOf(DRIVE_SCOPE))
            .also { it.selectedAccount = account }

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName(context.getString(R.string.app_name))
            .build()
    }

    private suspend fun getOrCreateAppFolder(drive: Drive): String = withContext(Dispatchers.IO) {
        val searchResult = drive.files().list()
            .setQ("mimeType='application/vnd.google-apps.folder' and name='$FOLDER_NAME' and trashed=false")
            .setSpaces("drive")
            .setFields("files(id)")
            .execute()

        if (searchResult.files.isNotEmpty()) {
            return@withContext searchResult.files[0].id
        }

        val folderMetadata = File().apply {
            name = FOLDER_NAME
            mimeType = "application/vnd.google-apps.folder"
        }
        val createdFolder = drive.files().create(folderMetadata).setFields("id").execute()
        createdFolder.id
    }

    suspend fun uploadBackup(email: String, backupFile: java.io.File): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val drive = getDriveService(email)
            val appFolderId = getOrCreateAppFolder(drive)

            val fileMetadata = File().apply {
                name = backupFile.name
                parents = listOf(appFolderId)
            }
            val mediaContent = FileContent("application/zip", backupFile)

            drive.files().create(fileMetadata, mediaContent).execute()

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getBackupFiles(email: String): Result<List<File>> = withContext(Dispatchers.IO) {
        try {
            val drive = getDriveService(email)
            val appFolderId = getOrCreateAppFolder(drive)

            val response = drive.files().list()
                .setQ("'$appFolderId' in parents and trashed=false")
                .setSpaces("drive")
                .setFields("files(id, name, modifiedTime)")
                .execute()
            Result.success(response.files)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun downloadBackup(email: String, fileId: String, outputFile: java.io.File): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val drive = getDriveService(email)
            drive.files().get(fileId).executeMediaAndDownloadTo(outputFile.outputStream())
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
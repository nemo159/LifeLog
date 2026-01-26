package com.rmtm.lifelog.data.remote

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.rmtm.lifelog.R
import com.rmtm.lifelog.data.local.db.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
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
    private fun getDriveService(account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(context, setOf(DRIVE_SCOPE))
            .also { it.selectedAccount = account.account }

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

    suspend fun uploadDatabase(account: GoogleSignInAccount): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val drive = getDriveService(account)
            val appFolderId = getOrCreateAppFolder(drive)
            val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)

            if (!dbFile.exists()) {
                return@withContext Result.failure(Exception("데이터베이스 파일을 찾을 수 없습니다."))
            }

            val existingFiles = drive.files().list()
                .setQ("'$appFolderId' in parents and name='${AppDatabase.DATABASE_NAME}' and trashed=false")
                .setSpaces("drive")
                .setFields("files(id)")
                .execute()

            val existingFileId = existingFiles.files.firstOrNull()?.id

            val fileMetadata = File().apply {
                name = AppDatabase.DATABASE_NAME
                parents = if (existingFileId == null) listOf(appFolderId) else null
            }
            val mediaContent = FileContent("application/x-sqlite3", dbFile)

            if (existingFileId == null) {
                drive.files().create(fileMetadata, mediaContent).execute()
            } else {
                drive.files().update(existingFileId, fileMetadata, mediaContent).execute()
            }

            Result.success(Unit)
        } catch (e: IOException) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getBackupFiles(account: GoogleSignInAccount): Result<List<File>> = withContext(Dispatchers.IO) {
        try {
            val drive = getDriveService(account)
            val appFolderId = getOrCreateAppFolder(drive)

            val response = drive.files().list()
                .setQ("'$appFolderId' in parents and trashed=false")
                .setSpaces("drive")
                .setFields("files(id, name, modifiedTime)")
                .execute()
            Result.success(response.files)
        } catch (e: IOException) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun downloadDatabase(account: GoogleSignInAccount, fileId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val drive = getDriveService(account)
            val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)

            drive.files().get(fileId).executeMediaAndDownloadTo(dbFile.outputStream())

            Result.success(Unit)
        } catch (e: IOException) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}

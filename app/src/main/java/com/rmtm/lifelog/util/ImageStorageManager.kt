package com.rmtm.lifelog.util

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [이미지 관리자]
 * 사진을 스마트폰 내부 저장소에 복사하거나 지우는 역할을 합니다.
 * - 갤러리 원본 사진이 지워져도 앱에는 사진이 남도록, 파일을 앱 전용 폴더로 가져옵니다.
 */
@Singleton
class ImageStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * URI의 사진을 internal storage/images 폴더로 복사하고 로컬 경로를 반환합니다.
     */
    suspend fun saveImageToInternalStorage(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val fileName = "img_${UUID.randomUUID()}.jpg"
            val imagesDir = File(context.filesDir, "images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }

            val file = File(imagesDir, fileName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext null

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 내부 저장소에 저장된 파일을 삭제합니다.
     */
    fun deleteImage(path: String) {
        try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

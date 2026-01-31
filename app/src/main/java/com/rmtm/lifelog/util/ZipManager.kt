package com.rmtm.lifelog.util

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * [Zip 유틸리티]
 * 파일 및 디렉터리를 압축하거나 해제하는 기능을 제공합니다.
 */
object ZipManager {

    /**
     * 지정된 파일 및 디렉터리 목록을 하나의 zip 파일로 압축합니다.
     * @param filesToZip 압축할 파일 또는 디렉터리 목록
     * @param outputZipFile 생성될 zip 파일 경로
     */
    fun zip(filesToZip: List<File>, outputZipFile: String) {
        ZipOutputStream(FileOutputStream(outputZipFile)).use { zos ->
            filesToZip.forEach { file ->
                if (!file.exists()) return@forEach

                if (file.isDirectory) {
                    addFolderToZip(file, file.name, zos)
                } else {
                    addFileToZip(file, zos)
                }
            }
        }
    }

    private fun addFileToZip(file: File, zos: ZipOutputStream, entryName: String = file.name) {
        FileInputStream(file).use { fis ->
            val zipEntry = ZipEntry(entryName)
            zos.putNextEntry(zipEntry)
            fis.copyTo(zos)
            zos.closeEntry()
        }
    }

    private fun addFolderToZip(folder: File, baseName: String, zos: ZipOutputStream) {
        folder.listFiles()?.forEach { file ->
            val entryName = baseName + File.separator + file.name
            if (file.isDirectory) {
                addFolderToZip(file, entryName, zos)
            } else {
                addFileToZip(file, zos, entryName)
            }
        }
    }


    /**
     * zip 파일을 지정된 디렉터리에 압축 해제합니다.
     * @param zipFile 압축 해제할 zip 파일
     * @param outputDir 압축 해제될 위치
     */
    fun unzip(zipFile: String, outputDir: File) {
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        ZipInputStream(FileInputStream(zipFile)).use { zipIn ->
            var entry = zipIn.nextEntry
            while (entry != null) {
                val newFile = File(outputDir, entry.name)
                if (entry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    newFile.parentFile?.mkdirs()
                    FileOutputStream(newFile).use { fileOut ->
                        zipIn.copyTo(fileOut)
                    }
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
        }
    }
}
package com.mashiro.renderengine.utils

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileUtils {
    fun saveInputStream(
        input: InputStream?, dir: String?,
        name: String?
    ): Boolean {
        if (input == null) {
            return false
        }
        var out: FileOutputStream? = null
        try {
            val path = File(dir)
            if (!path.exists()) {
                if (!path.mkdirs()) {
                    return false
                }
            }
            val f = File(path, name)
            out = FileOutputStream(f)
            var buffer: ByteArray? = ByteArray(1024)
            var length: Int
            while (input.read(buffer).also { length = it } != -1) {
                out.write(buffer, 0, length)
            }
            out.flush()
            out.close()
            out = null
            buffer = null
            input.close()
        } catch (e: Exception) {
            return false
        } finally {
            out?.close()
            input?.close()
        }
        return true
    }
}
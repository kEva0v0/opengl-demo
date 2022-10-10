package com.mashiro.filament

import android.app.Activity
import java.nio.ByteBuffer
import java.nio.channels.Channels

object AssetUtils {
    fun readUncompressedAsset(activity: Activity, assetName: String): ByteBuffer {
        activity.assets.openFd(assetName).use { fd ->
            val input = fd.createInputStream()
            val dst = ByteBuffer.allocate(fd.length.toInt())

            val src = Channels.newChannel(input)
            src.read(dst)
            src.close()

            return dst.apply { rewind() }
        }
    }
}
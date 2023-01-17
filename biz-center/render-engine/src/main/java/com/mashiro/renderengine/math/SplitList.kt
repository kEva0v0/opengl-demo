package com.mashiro.renderengine.math

import android.util.Log

/**
 * splitList，分桶数组，将数组以splitRange进行分桶
 * 为了降低New DataClass带来的GC开销，将整个数组平铺
 *
 * @param splitRange: 间距
 *
 * @see
 */
class SplitList<E>(@androidx.annotation.IntRange(from = 2) val splitRange: Int = 2): ArrayList<E>() {

    companion object {
        private const val TAG = "math-split-list"
    }

    fun splitSize(): Int = size / splitRange

    fun splitLegal(): Boolean {
        return size % splitRange == 0
    }

    fun addSplitItem(vararg item: E) {
        if (item.size == splitRange) {
            item.forEach { add(it) }
        } else {
            Log.e(TAG, "add item size not equal to splitRange")
        }
    }

    /**
     * index 指的是第index个桶
     */
    fun getSplitItem(index: Int = -1): List<E>? {
        return if (splitLegal() && index < splitSize()){
            val mul = mutableListOf<E>()
            for (i in index*splitRange until (index + 1)*splitRange){
                mul.add(this[i])
            }
            mul
        } else {
            null
        }
    }

    /**
     * index 指的是第index个桶
     * 如果list是空的，默认返回0
     */
    fun getItemIndex(index: Int = -1, offset: Int = 0): Int {
        if (index >= splitSize()) {
            throw java.lang.IllegalStateException("index should less than ${splitSize()}")
        }
        if (offset >= splitRange) {
            throw java.lang.IllegalStateException("offset should be less than $splitRange")
        }
        if (!splitLegal()) {
            throw java.lang.IllegalStateException("your data is not legal, size is ${size}, but splitRange is ${splitRange}, may lead Crash!")
        }
        return maxOf(index * splitRange + offset, 0)
    }
}


inline fun <E: Comparable<E>> SplitList<E>.minOfOffsetOrNull(offset: Int): E? {
    return this.compareOffsetOrNull(offset){ a,b -> minOf(a,b) }
}

inline fun <E: Comparable<E>> SplitList<E>.maxOfOffsetOrNull(offset: Int): E? {
    return this.compareOffsetOrNull(offset){ a,b -> maxOf(a,b) }
}

inline fun <E> SplitList<E>.compareOffsetOrNull(offset: Int, action: (E,E) -> E): E? {
    if (isNotEmpty() && this.splitLegal() && offset < splitRange) {
        var index = offset
        var minValue = this[index]
        while (index < this.size) {
            minValue = action(minValue, this[index])
            index += splitRange
        }
        return minValue
    } else {
        return null
    }
}
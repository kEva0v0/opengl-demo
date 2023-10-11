package com.mashiro.uitest.fragment

import java.util.UUID

sealed class TestCC(
    val tmp: String = UUID.randomUUID().toString()
)

data class TestDD(
    val haha: String
): TestCC(){
    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }
}

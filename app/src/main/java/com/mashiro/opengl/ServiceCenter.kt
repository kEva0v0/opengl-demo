package com.mashiro.opengl

import java.util.ServiceLoader

object ServiceCenter {
    private val serviceMap: MutableMap<Class<*>, MutableList<Any>> = mutableMapOf()

    fun registerService(api: Class<*>, impl: Any) {
        if (serviceMap.containsKey(api)){
            serviceMap[api]?.add(impl)
        } else {
            serviceMap[api] = mutableListOf(impl)
        }
    }

    fun <T> getService(api: Class<T>): List<T>?{
        if (serviceMap.containsKey(api)){
            return serviceMap[api]?.map { it as T }
        }
        return null
    }
}

interface Ia{
    fun aa() {}
}
class AImpl : Ia {}

interface Ib{
    fun bb() {}
}
class BImpl: Ib {}

class TestUse{
    fun testRegister(){
        ServiceCenter.registerService(Ia::class.java, AImpl())
        ServiceCenter.registerService(Ib::class.java, BImpl())
    }

    fun testUse() {
        val a: Ia? = ServiceCenter.getService(Ia::class.java)?.first()
        val b: Ib? = ServiceCenter.getService(Ib::class.java)?.first()
        a?.aa()
        b?.bb()
        var service = ServiceLoader.load(IMyApi::class.java).iterator()
        service.next().test()

    }
}

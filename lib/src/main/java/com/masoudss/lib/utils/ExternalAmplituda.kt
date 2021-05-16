package com.masoudss.lib.utils

import android.content.Context
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

internal object ExternalAmplituda {

    /**
     * @param args: args[0] - context || args[1] - path to file
     */
    fun run(result: (IntArray) -> Unit, vararg args: Any) : ExternalAmplituda {
        val classs = Class.forName("linc.com.amplituda.Amplituda")
            .getDeclaredConstructor(Context::class.java)
            .newInstance(args[0])

        val callback = Proxy.newProxyInstance(
            Class.forName("linc.com.amplituda.Amplituda\$StringCallback").classLoader,
            arrayOf(Class.forName("linc.com.amplituda.Amplituda\$StringCallback")),
            InvocationHandler { any, method, arrayOfAnys ->
                arrayOfAnys.forEach {
                    result(it.toString()
                        .split(" ")
                        .toList()
                        .map { it.toInt() }
                        .toIntArray()
                    )
                }
            })

        val pathMethod: Method = classs::class.java.getMethod(
            "fromPath",
            String::class.javaObjectType
        )

        pathMethod.invoke(classs, args[1])

        classs::class.java.declaredClasses.forEach {
            println(it.name)
        }

        val amplitudesListMethod: Method = classs::class.java.methods.find {
            it.name == "amplitudesAsSequence"
        }!!

        amplitudesListMethod.invoke(classs, 0, callback)

        return this
    }

}
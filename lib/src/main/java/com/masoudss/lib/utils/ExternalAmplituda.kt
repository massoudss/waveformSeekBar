package com.masoudss.lib.utils

import android.content.Context
import com.masoudss.lib.exception.AmplitudaNotFoundException
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

internal object ExternalAmplituda {

    private const val CONTEXT = 0
    private const val PATH = 1
    private const val SINGLE_LINE_SEQUENCE_FORMAT = 0

    /**
     * @param args:
     *      args[0] - context
     *      args[1] - path to file
     */
    @JvmStatic
    @Throws(AmplitudaNotFoundException::class)
    fun run(result: (IntArray) -> Unit, vararg args: Any) {
        val amplituda: Any

        // Check Amplituda dependency
        try {
            amplituda = Class.forName("linc.com.amplituda.Amplituda")
                    .getDeclaredConstructor(Context::class.java)
                    .newInstance(args[CONTEXT])
        } catch (notFound: ClassNotFoundException) {
            throw AmplitudaNotFoundException()
        }

        // Callback class for result
        val stringCallback = Class.forName("linc.com.amplituda.Amplituda\$StringCallback")

        // Handle result in this callback
        val amplitudesStringResultCallback = Proxy.newProxyInstance(stringCallback.classLoader, arrayOf(stringCallback),
                InvocationHandler { _, _, resultParams ->
                    resultParams.forEach { amplitudaString ->
                        // Convert string sequence to IntArray
                        result(amplitudaString.toString()
                                .split(" ") // Default delimiter
                                .map { it.toInt() }
                                .toIntArray()
                        )
                    }
                })

        // Set input audio path
        amplituda::class.java.getMethod(
                "fromPath",
                String::class.javaObjectType
        ).invoke(amplituda, args[PATH])

        // Process audio
        amplituda::class.java.methods.find {
            it.name == "amplitudesAsSequence"
        }!!.invoke(amplituda, SINGLE_LINE_SEQUENCE_FORMAT, amplitudesStringResultCallback)
    }
}
package com.haroldadmin.cnradapter

import com.google.common.reflect.TypeToken
import java.lang.reflect.Type

// Credits to Jake Wharton for this
internal inline fun <reified T> typeOf(): Type = object : TypeToken<T>() {}.type

/**
 * Reads and returns the contents of a text file stored in src/test/resources
 *
 * The file's path must be relative to the resources directory, but must start with "/"
 * For example, to read "src/test/resources/error_response.json", the filename parameter must be given as
 * "/error_response.json"
 */
internal fun Any.resourceFileContents(filename: String): String {
    return this::class.java.getResourceAsStream(filename).bufferedReader().use { br ->
        br.readText()
    }
}
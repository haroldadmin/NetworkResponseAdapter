package com.haroldadmin.cnradapter

import com.google.common.reflect.TypeToken
import java.lang.reflect.Type

// Credits to Jake Wharton for this
internal inline fun <reified T> typeOf(): Type = object : TypeToken<T>() {}.type
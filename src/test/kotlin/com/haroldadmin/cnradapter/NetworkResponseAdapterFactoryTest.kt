package com.haroldadmin.cnradapter

import com.haroldadmin.cnradapter.utils.StringConverterFactory
import com.haroldadmin.cnradapter.utils.typeOf
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.Retrofit

public class NetworkResponseAdapterFactoryTest : DescribeSpec({
    describe(NetworkResponseAdapterFactory::class.java.simpleName) {
        it("should return null for non NetworkResponse return types") {
            val factory = NetworkResponseAdapterFactory()
            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(factory)
                .addConverterFactory(StringConverterFactory())
                .baseUrl("http://localhost")
                .build()
            val returnType = typeOf<String>()
            val callAdapter = factory.get(returnType, emptyArray(), retrofit)
            callAdapter shouldBe null
        }

        it("should return null for deferred non-NetworkResponse types") {
            val factory = NetworkResponseAdapterFactory()
            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(factory)
                .addConverterFactory(StringConverterFactory())
                .baseUrl("http://localhost")
                .build()
            val returnType = typeOf<Deferred<String>>()
            val callAdapter = factory.get(returnType, emptyArray(), retrofit)
            callAdapter shouldBe null
        }

        it("should return null for bare NetworkResponse type") {
            val factory = NetworkResponseAdapterFactory()
            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(factory)
                .addConverterFactory(StringConverterFactory())
                .baseUrl("http://localhost")
                .build()
            val returnType = typeOf<NetworkResponse<String, String>>()
            val callAdapter = factory.get(returnType, emptyArray(), retrofit)
            callAdapter shouldBe null
        }

        it("should return non-null call adapter for Call<NetworkResponse<...,...>> type") {
            val factory = NetworkResponseAdapterFactory()
            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(factory)
                .addConverterFactory(StringConverterFactory())
                .baseUrl("http://localhost")
                .build()
            val returnType = typeOf<Call<NetworkResponse<String, String>>>()
            val callAdapter = factory.get(returnType, emptyArray(), retrofit)
            callAdapter shouldNotBe null
        }

        it("suspend return non-null call adapter for Deferred<NetworkResponse<...,...>> type") {
            val factory = NetworkResponseAdapterFactory()
            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(factory)
                .addConverterFactory(StringConverterFactory())
                .baseUrl("http://localhost")
                .build()
            val returnType = typeOf<Deferred<NetworkResponse<String, String>>>()
            val callAdapter = factory.get(returnType, emptyArray(), retrofit)
            callAdapter shouldNotBe null
        }
    }
})

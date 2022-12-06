package com.mobile.dogbreeddetection.RetrofitAPI

import android.content.Context
import android.net.ConnectivityManager
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class RetrofitDataSource(context: Context) {
    lateinit var retrofit: Retrofit
    lateinit var dogService: DogService

    init {
        retrofit = createRetrofitClient(context)
        dogService = retrofit.create(DogService::class.java)
    }

    private fun createRetrofitClient(context: Context): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://ec2-18-118-185-42.us-east-2.compute.amazonaws.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(createClient(context))
            .build()
    }

    private fun hasNetwork(context: Context): Boolean {
        val cm: ConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetwork != null && cm.getNetworkCapabilities(cm.activeNetwork) != null
    }

    private fun createCache(context: Context): Cache {
        return Cache(context.cacheDir, (5 * 1024 * 1024))
    }

    private fun createClient(context: Context): OkHttpClient? {
        return try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                    }

                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(
                        chain: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate?>? {
                        return arrayOf()
                    }
                }
            )

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory = sslContext.socketFactory
            val trustManagerFactory: TrustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(null as KeyStore?)
            val trustManagers: Array<TrustManager> =
                trustManagerFactory.trustManagers
            check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
                "Unexpected default trust managers:" + trustManagers.contentToString()
            }

            val trustManager =
                trustManagers[0] as X509TrustManager


            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, trustManager)
            builder.hostnameVerifier(HostnameVerifier { _, _ -> true })
            builder.build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

//    private fun createClient(context: Context): OkHttpClient {
//        val okHttpClient = OkHttpClient.Builder()
//            .cache(createCache(context))
//            .addInterceptor { chain ->
//                var request = chain.request()
//                request = if (hasNetwork(context))
//                    request.newBuilder().header("Cache-Control", "public, max-age=" + 5).build()
//                else
//                    request.newBuilder().header(
//                        "Cache-Control",
//                        "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7
//                    ).build()
//                chain.proceed(request)
//            }
//            .build()
//        return okHttpClient
//    }

    suspend fun getStatus(): String {
        val response = dogService.getStatus()
        if (response.isSuccessful) {
            val responseStatus = response.body()
            if (responseStatus != null) {
                return responseStatus.toString()
            }
        }

        return ""
    }

    suspend fun getDogBreed(uri: String): Dog? {
        val response = dogService.getDogBreed(uri)
        if (response.isSuccessful) {
            val responseBody = response.body()
            if (responseBody != null) {
                return responseBody
            }
        }

        return null
    }
}
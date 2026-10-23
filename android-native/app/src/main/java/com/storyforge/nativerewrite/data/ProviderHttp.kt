package com.storyforge.nativerewrite.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

/**
 * Streaming client used by the continuation milestone. The skeleton never opens it.
 * Redirects stay off so a provider cannot move a request that already carries a key.
 */
class ProviderHttp {
    fun create(): HttpClient = HttpClient(OkHttp) {
        expectSuccess = false
        engine {
            config {
                followRedirects(false)
                retryOnConnectionFailure(false)
            }
        }
    }
}

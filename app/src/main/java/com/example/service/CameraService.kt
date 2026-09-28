package com.example.service

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.model.AppSettings
import com.example.model.StreamState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

class CameraService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(3000, TimeUnit.MILLISECONDS)
        .readTimeout(5000, TimeUnit.MILLISECONDS)
        .build()

    private val _streamState = MutableStateFlow<StreamState>(StreamState.Idle)
    val streamState: StateFlow<StreamState> = _streamState.asStateFlow()

    private var streamingJob: Job? = null

    suspend fun testCamera(settings: AppSettings): Boolean = withContext(Dispatchers.IO) {
        val url = settings.cameraStreamUrl
        val request = Request.Builder().url(url).head().build()
        try {
            client.newCall(request).execute().use { response ->
                response.isSuccessful || response.code == 200
            }
        } catch (_: Exception) {
            // Try fallback GET test with short timeout
            try {
                val getRequest = Request.Builder().url("http://${settings.cameraIp}:${settings.cameraPort}/").get().build()
                client.newCall(getRequest).execute().use { it.isSuccessful }
            } catch (_: Exception) {
                false
            }
        }
    }

    fun startStream(scope: CoroutineScope, settings: AppSettings) {
        streamingJob?.cancel()
        streamingJob = scope.launch(Dispatchers.IO) {
            _streamState.value = StreamState.Connecting
            val url = settings.cameraStreamUrl
            val request = Request.Builder().url(url).get().build()

            var response: Response? = null
            var inputStream: InputStream? = null
            try {
                response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    _streamState.value = StreamState.Error(
                        title = "STREAM IP FAILED",
                        message = "STREAM NOT AVAILABLE (HTTP ${response.code})"
                    )
                    return@launch
                }

                inputStream = response.body?.byteStream()
                if (inputStream == null) {
                    _streamState.value = StreamState.Error(
                        title = "STREAM IP FAILED",
                        message = "STREAM NOT AVAILABLE"
                    )
                    return@launch
                }

                val buffer = ByteArray(16384)
                val streamBuffer = ByteArrayOutputStream()
                var prevByte = -1
                var inJpeg = false
                var frameCount = 0
                var lastFpsCheck = System.currentTimeMillis()
                var currentFps = 0

                while (isActive) {
                    val bytesRead = inputStream.read(buffer)
                    if (bytesRead == -1) break

                    for (i in 0 until bytesRead) {
                        val currByte = buffer[i].toInt() and 0xFF

                        if (!inJpeg) {
                            if (prevByte == 0xFF && currByte == 0xD8) {
                                inJpeg = true
                                streamBuffer.reset()
                                streamBuffer.write(0xFF)
                                streamBuffer.write(0xD8)
                            }
                        } else {
                            streamBuffer.write(currByte)
                            if (prevByte == 0xFF && currByte == 0xD9) {
                                inJpeg = false
                                val jpegBytes = streamBuffer.toByteArray()
                                val bitmap = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
                                if (bitmap != null) {
                                    frameCount++
                                    val now = System.currentTimeMillis()
                                    if (now - lastFpsCheck >= 1000) {
                                        currentFps = frameCount
                                        frameCount = 0
                                        lastFpsCheck = now
                                    }
                                    _streamState.value = StreamState.Streaming(bitmap, currentFps)
                                }
                            }
                        }
                        prevByte = currByte
                    }
                }
            } catch (e: CancellationException) {
                // Expected when canceling or switching
            } catch (e: Exception) {
                _streamState.value = StreamState.Error(
                    title = "STREAM IP FAILED",
                    message = "STREAM NOT AVAILABLE"
                )
            } finally {
                try {
                    inputStream?.close()
                } catch (_: Exception) {}
                try {
                    response?.close()
                } catch (_: Exception) {}
            }
        }
    }

    fun restartStream(scope: CoroutineScope, settings: AppSettings) {
        stopStream()
        startStream(scope, settings)
    }

    fun stopStream() {
        streamingJob?.cancel()
        streamingJob = null
        _streamState.value = StreamState.Idle
    }
}

package com.fakelag.v3

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer

class FakeLagVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    companion object {
        var isRunning = false
        var isStabilizerEnabled = false
        var delayMs: Long = 100L
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_VPN") {
            stopVpn()
            return START_NOT_STICKY
        }

        if (!isRunning) {
            startVpn()
        }
        return START_STICKY
    }

    private fun startVpn() {
        val builder = Builder()
            .setSession("FakeLag3.0")
            .addAddress("10.0.0.2", 32)
            .addRoute("0.0.0.0", 0)

        vpnInterface = builder.establish()
        isRunning = true

        serviceScope.launch {
            processPackets()
        }
    }

    private suspend fun processPackets() {
        val fd = vpnInterface?.fileDescriptor ?: return
        val inputStream = FileInputStream(fd)
        val outputStream = FileOutputStream(fd)
        val packet = ByteBuffer.allocate(32767)

        try {
            while (isRunning) {
                val length = inputStream.read(packet.array())
                if (length > 0) {
                    if (isStabilizerEnabled && delayMs > 0) {
                        delay(delayMs / 2)
                        yield()
                    }
                    packet.limit(length)
                    outputStream.write(packet.array(), 0, length)
                    packet.clear()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopVpn() {
        isRunning = false
        serviceJob.cancel()
        vpnInterface?.close()
        vpnInterface = null
        stopSelf()
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }
}

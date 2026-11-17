package com.fakelag.v3

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            startService(Intent(this, FakeLagVpnService::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnVpn = findViewById<Button>(R.id.btnVpn)
        val switchLag = findViewById<Switch>(R.id.switchLag)
        val seekBarDelay = findViewById<SeekBar>(R.id.seekBarDelay)
        val txtDelay = findViewById<TextView>(R.id.txtDelay)
        val txtStatus = findViewById<TextView>(R.id.txtStatus)

        btnVpn.setOnClickListener {
            if (!FakeLagVpnService.isRunning) {
                val intent = VpnService.prepare(this)
                if (intent != null) {
                    vpnPermissionLauncher.launch(intent)
                } else {
                    startService(Intent(this, FakeLagVpnService::class.java))
                }
                btnVpn.text = "DESCONECTAR"
                txtStatus.text = "ESTADO: CONECTADO"
            } else {
                val stopIntent = Intent(this, FakeLagVpnService::class.java).apply {
                    action = "STOP_VPN"
                }
                startService(stopIntent)
                btnVpn.text = "CONECTAR"
                txtStatus.text = "ESTADO: DESCONECTADO"
            }
        }

        switchLag.setOnCheckedChangeListener { _, isChecked ->
            FakeLagVpnService.isStabilizerEnabled = isChecked
        }

        seekBarDelay.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                FakeLagVpnService.delayMs = progress.toLong()
                txtDelay.text = "Retraso: $progress ms"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
}

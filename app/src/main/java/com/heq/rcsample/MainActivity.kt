package com.heq.rcsample

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.SimpleAdapter
import android.widget.SpinnerAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.heq.rcsample.databinding.ActivityMainBinding
import heq.sdk.keyvalue.key.AirLinkKey
import heq.sdk.keyvalue.key.RemoteControllerKey
import heq.sdk.keyvalue.value.airlink.BandSelectionMode
import heq.sdk.keyvalue.value.airlink.Bandwidth
import heq.sdk.keyvalue.value.airlink.ChannelSelectionMode
import heq.sdk.keyvalue.value.airlink.FrequencyBand
import heq.sdk.keyvalue.value.remotecontroller.ControlMode
import heq.v1.common.error.IHEQError
import heq.v1.common.register.HEQSDKInitEvent
import heq.v1.et.create
import heq.v1.impl.remotecontroller.RemoteControllerSdkBootstrap
import heq.v1.manager.KeyManager
import heq.v1.manager.SDKManager
import heq.v1.manager.interfaces.SDKManagerCallback
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var pollJob: Job? = null
    private var listening = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        SDKManager.getInstance().init(
            this,
            object : SDKManagerCallback {
                override fun onInitProcess(event: HEQSDKInitEvent, totalProcess: Int) {}
                override fun onRegisterSuccess() {
                    RemoteControllerSdkBootstrap.installStandalone()
                    startListen()
                    startPolling()
                }
                override fun onRegisterFailure(error: IHEQError) {
                }
                override fun onProductDisconnect(productId: Int) {}
                override fun onProductConnect(productId: Int) {}
                override fun onProductChanged(productId: Int) {}
                override fun onDatabaseDownloadProgress(current: Long, total: Long) {}
            },
        )
        SDKManager.getInstance().registerApp()

        binding.btnRequestPairing.setOnClickListener {
            RemoteControllerKey.KeyRequestPairing.create().action(
                onSuccess = { toast("请求对频已发送") },
                onFailure = { e -> toast(e.description().ifBlank { "请求对频失败" }) },
            )
        }

        binding.btnStopPairing.setOnClickListener {
            RemoteControllerKey.KeyStopPairing.create().action(
                onSuccess = { toast("停止对频已发送") },
                onFailure = { e -> toast(e.description().ifBlank { "停止对频失败" }) },
            )
        }

        binding.btnAutoSetBand.setOnClickListener {
            AirLinkKey.KeyFrequencyBandMode.create().set(BandSelectionMode.AUTO,
                onSuccess = { toast("已设置自动选频段") },
                onFailure = { e -> toast(e.description().ifBlank { "设置自动选频段失败" }) }
            )
        }

        binding.btnManualSetBand.setOnClickListener {
            AirLinkKey.KeyFrequencyBandMode.create().set(BandSelectionMode.MANUAL,
                onSuccess = { toast("已设置手动选频段") },
                onFailure = { e -> toast(e.description().ifBlank { "设置手动选频段失败" }) },)
        }

        binding.btnJP.setOnClickListener {
            RemoteControllerKey.KeyControlMode.create().set(ControlMode.JAPAN,
                onSuccess = { toast("已设置日本手") },
                onFailure = { e -> toast(e.description().ifBlank { "设置日本手失败" }) }
            )
        }

        binding.btnUS.setOnClickListener {
            RemoteControllerKey.KeyControlMode.create().set(ControlMode.USA,
                onSuccess = { toast("已设置美国手") },
                onFailure = { e -> toast(e.description().ifBlank { "设置美国手失败" }) }
            )
        }

        binding.btnCN.setOnClickListener {
            RemoteControllerKey.KeyControlMode.create().set(ControlMode.CHINESE,
                onSuccess = { toast("已设置中国手") },
                onFailure = { e -> toast(e.description().ifBlank { "设置中国手失败" }) }
            )
        }

        binding.btnRX5.setOnClickListener {
            AirLinkKey.KeyBandwidth.create().set(
                Bandwidth.BANDWIDTH_5MHZ,
                onSuccess = { toast("已设置下行带宽5MHZ") },
                onFailure = { e -> toast(e.description().ifBlank { "设置下行带宽5MHZ失败" }) }
            )
        }

        binding.btnRX10.setOnClickListener {
            AirLinkKey.KeyBandwidth.create().set(
                Bandwidth.BANDWIDTH_10MHZ,
                onSuccess = { toast("已设置下行带宽10MHZ") },
                onFailure = { e -> toast(e.description().ifBlank { "设置下行带宽10MHZ失败" }) }
            )
        }

        binding.btnRX20.setOnClickListener {
            AirLinkKey.KeyBandwidth.create().set(
                Bandwidth.BANDWIDTH_20MHZ,
                onSuccess = { toast("已设置下行带宽20MHZ") },
                onFailure = { e -> toast(e.description().ifBlank { "设置下行带宽20MHZ失败" }) }
            )
        }

        binding.btnTX25.setOnClickListener {
            AirLinkKey.KeyTxBandwidth.create().set(
                Bandwidth.BANDWIDTH_2_DOT_5MHZ,
                onSuccess = { toast("已设置上行带宽2.5MHZ") },
                onFailure = { e -> toast(e.description().ifBlank { "设置上行带宽2.5MHZ失败" }) }
            )
        }

        binding.btnTX5.setOnClickListener {
            AirLinkKey.KeyTxBandwidth.create().set(
                Bandwidth.BANDWIDTH_5MHZ,
                onSuccess = { toast("已设置上行带宽5MHZ") },
                onFailure = { e -> toast(e.description().ifBlank { "设置上行带宽5MHZ失败" }) }
            )
        }

        binding.btnTX10.setOnClickListener {
            AirLinkKey.KeyTxBandwidth.create().set(
                Bandwidth.BANDWIDTH_10MHZ,
                onSuccess = { toast("已设置上行带宽10MHZ") },
                onFailure = { e -> toast(e.description().ifBlank { "设置上行带宽10MHZ失败" }) }
            )
        }


        binding.btnSetBand24.setOnClickListener {
            AirLinkKey.KeyFrequencyBand.create().set(
                FrequencyBand.BAND_2_DOT_4_GHZ,
                onSuccess = { toast("已设置频段 2.4G") },
                onFailure = { e -> toast(e.description().ifBlank { "设置频段失败" }) },
            )
        }

        binding.btnSetBand58.setOnClickListener {
            AirLinkKey.KeyFrequencyBand.create().set(
                FrequencyBand.BAND_5_DOT_8_GHZ,
                onSuccess = { toast("已设置频段 5.8G") },
                onFailure = { e -> toast(e.description().ifBlank { "设置频段失败" }) },
            )
        }

        binding.btnSetChannelAuto.setOnClickListener {
            AirLinkKey.KeyChannelSelectionMode.create().set(
                ChannelSelectionMode.AUTO,
                onSuccess = { toast("已设置自动选频") },
                onFailure = { e -> toast(e.description().ifBlank { "设置选频模式失败" }) },
            )
        }

        binding.btnSetChannelManual.setOnClickListener {
            AirLinkKey.KeyChannelSelectionMode.create().set(
                ChannelSelectionMode.MANUAL,
                onSuccess = { toast("已设置手动选频") },
                onFailure = { e -> toast(e.description().ifBlank { "设置选频模式失败" }) },
            )
        }
    }

    override fun onDestroy() {
        startPolling()
        stopListen()
        super.onDestroy()
    }

    private fun startPolling() {
        if (pollJob?.isActive == true) return
        pollJob = lifecycleScope.launch {
            refreshSnapshot()
        }
    }

    private fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    private fun startListen() {
        if (listening) return
        listening = true
        AirLinkKey.KeySignalQuality.create().listen(this, getOnce = true) { value ->
            val percent = value ?: 0
            binding.tvSignalListen.text = "RC 信号监听: $percent%"
        }
        RemoteControllerKey.KeyControlMode.create().listen(this,{
            binding.mode.text="ControlMode:$it"
        })

        RemoteControllerKey.KeyControlMode.create().listen(this,{
            binding.mode.text="ControlMode:$it"
        })

        AirLinkKey.KeyRSSI.create().listen(this,{
            binding.rssi.text="RSSI:localRssiA:${it?.localRssiA} localRssiB:${it?.localRssiB} peerRssiA:${it?.peerRssiA} peerRssiB:${it?.peerRssiB}"

        })

        AirLinkKey.KeyFrequencyBandMode.create().listen(this,{
            binding.bandSelectMode.text="Band Mode:$it"
            binding.groupBand.visibility =if (it== BandSelectionMode.MANUAL) View.VISIBLE else View.GONE
        })
        AirLinkKey.KeyFrequencyBand.create().listen(this,{
            binding.currentBand.text="Current Band:$it"
        })
        AirLinkKey.KeyChannelSelectionMode.create().listen(this,{
            binding.pointSelectMode.text="ChannelSelectionMode:$it"
            binding.freqPointRange.visibility =if (it==ChannelSelectionMode.MANUAL) View.VISIBLE else View.GONE
        })
        AirLinkKey.KeyFrequencyPoint.create().listen(this,{
            binding.currentPoint.text="Current FrequencyPoint:$it"
        })

        AirLinkKey.KeyBandwidth.create().listen(this,{
            binding.bandWidth.text="RX Bandwidth:${it?.displayName}"
        })

        AirLinkKey.KeyTxBandwidth.create().listen(this,{
            binding.txBandWidth.text="TX Bandwidth:${it?.displayName}"
        })
    }

    private fun stopListen() {
        if (!listening) return
        KeyManager.getInstance().cancelListen(this)
        listening = false
    }

    private fun refreshSnapshot() {
        RemoteControllerKey.KeyConnection.create().listen(this,{
            if (it==true){
                RemoteControllerKey.KeyControlMode.create().get(
                    {
                        binding.mode.text="ControlMode:$it"
                    },
                    {

                    }
                )
                RemoteControllerKey.KeyFirmwareVersion.create().get(
                    {
                        binding.version.text="FirmwareVersion:$it"
                    },
                    {
                        Log.e("TAG", "refreshSnapshot: ${it.description()}", )
                    }
                )
                RemoteControllerKey.KeyPairingStatus.create().get(
                    {
                        binding.pairStatus.text="Pair Status:$it"
                    },
                    {
                        Log.e("TAG", "refreshSnapshot: ${it.description()}", )
                    }
                )


                AirLinkKey.KeyBandwidth.create().get({
                    binding.bandWidth.text="RX Bandwidth:${it?.displayName}"
                },{

                })

                AirLinkKey.KeyFrequencyPointRange.create().get({
                    it?:return@get
                    val pointRange = mutableListOf<String>()
                    for (item in it){
                        pointRange.add("$item")
                    }
                    val arrayAdapter =
                        ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item)
                    arrayAdapter.addAll(pointRange)
                    binding.freqPointRange.adapter = arrayAdapter
                    var firstInit=false
                    binding.freqPointRange.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                        override fun onItemSelected(
                            p0: AdapterView<*>?,
                            p1: View?,
                            p2: Int,
                            p3: Long
                        ) {
                            p1?:return
                            if (!firstInit){
                                firstInit=true
                                return
                            }
                            AirLinkKey.KeyFrequencyPoint.create().set(it[p2],onSuccess = {
                                toast("已设置频点${it[p2]}") },
                                onFailure = { e -> toast(e.description().ifBlank { "设置频点失败" }) },)
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {

                        }

                    }
                },{

                })

                AirLinkKey.KeyTxBandwidth.create().get({
                    binding.txBandWidth.text="TX Bandwidth:${it?.displayName}"
                },{

                })
            }
        })
    }

    private fun toast(msg: String) {
        runOnUiThread {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }
}
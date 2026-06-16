package com.heq.rcsample

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import heq.v1.manager.transport.pipeline.ConnectionPipeline
import heq.v1.manager.transport.pipeline.PipelineConnection
import heq.v1.manager.transport.pipeline.PipelineReceiveCallback
import heq.v1.manager.transport.pipeline.SocketPipelineConfig
import heq.v1.manager.transport.pipeline.TransportPipelines
import heq.v1.manager.transport.pipeline.startReceiveCallback

class RCDataActivity : AppCompatActivity() {
    private val TAG="RCDataActivity"
    private var pipelineConnection: PipelineConnection?=null
    private var serial: ConnectionPipeline<MavlinkLinkConfig>?=null
    fun hexStringFormat(byteArray: ByteArray): String {
        val hexChars = CharArray(byteArray.size * 3)
        for (i in byteArray.indices) {
            val v = byteArray[i].toInt() and 0xFF
            hexChars[i * 3] = "0123456789ABCDEF"[v ushr 4]
            hexChars[i * 3 + 1] = "0123456789ABCDEF"[v and 0x0F]
            hexChars[i * 3 + 2] = ' '
        }
        return String(hexChars)
    }
    private  val MAX_LEN: Int = 2048
    private val sb = StringBuffer()
    fun appendLog(s: String?) {
        sb.append(s)
        // 超出就从头部删除最旧内容
        val overflow = sb.length - MAX_LEN
        if (overflow > 0) {
            sb.delete(0, overflow)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rcdata)
        findViewById<Button>(R.id.open).setOnClickListener {
            serial = TransportPipelines.serial<MavlinkLinkConfig>("/dev/ttyS9", 115200, true)
            pipelineConnection = serial?.open(MavlinkLinkConfig())

            pipelineConnection?.startReceiveCallback(object : PipelineReceiveCallback{
                override fun onData(buffer: ByteArray, offset: Int, length: Int) {

                    val data = ByteArray(length)
                    System.arraycopy(buffer,offset,data,0,length)
                    Log.e(TAG, "onData: ${hexStringFormat(data)}")
                    appendLog(hexStringFormat(data))
                    runOnUiThread {
                        findViewById<TextView>(R.id.data).text= sb
                    }

                }
            })
        }
        findViewById<Button>(R.id.close).setOnClickListener {
            pipelineConnection?.link?.close()
        }

        //write to serial
//        open.output.write("HELLO".toByteArray())
//        open.output.flush()
    }

    override fun onDestroy() {
        pipelineConnection?.link?.close()
        super.onDestroy()
    }
    data class MavlinkLinkConfig(
        /** UDP target; unused for serial links (default empty). */
        override val remoteHost: String = "",
        override val remotePort: Int = 14550,
        override val localPort: Int = 14550,
        /** GCS system id on the wire (MAVLink convention: 255). */
        val gcsSystemId: Int = 255,
        /** GCS component id (MAV_COMP_ID_MISSIONPLANNER = 190). */
        val gcsComponentId: Int = 190,
        /** Default takeoff altitude (meters) when action has no param. */
        val defaultTakeoffAltitudeMeters: Float = 10f,
        /** Telemetry considered stale after this many ms without packets. */
        val linkTimeoutMs: Long = 5_000,
        val paramReadTimeoutMs: Long = 3_000,
        val paramWriteTimeoutMs: Long = 3_000,
        /** Mission upload (MISSION_COUNT … MISSION_ACK) timeout. */
        val missionUploadTimeoutMs: Long = 60_000,
        /** Serial link baud (app configures port; documented default). */
        val serialBaudRate: Int = 57600,
    ) : SocketPipelineConfig
}
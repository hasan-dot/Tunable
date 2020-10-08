package com.example.ubuntu.tunableapplication.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.example.ubuntu.tunableapplication.R
import com.example.ubuntu.tunableapplication.util.models.BaseModel
import com.example.ubuntu.tunableapplication.util.models.User
import com.example.ubuntu.tunableapplication.util.network.ApiProvider
import com.example.ubuntu.tunableapplication.util.network.ApiResult
import com.naman14.androidlame.AndroidLame
import com.naman14.androidlame.LameBuilder
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.URISyntaxException
import com.example.ubuntu.tunableapplication.util.network.Constants.Companion.SOCKET_ENDPOINT
import com.example.ubuntu.tunableapplication.util.utility.Util
import com.google.gson.JsonObject
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_PERMISSION_CODE = 1000
    }

    private val TAG = "Main_Activity"
    private var socket: Socket? = null
    private var inSampleRate = 44100

    private var minBuffer: Int = 0
    private var user = User()
    private var duration: String = ""
    private val dir = File(Environment.getExternalStorageDirectory().toString() + "/tunable/")
    private var filePath = dir.absolutePath + "/"
    private var isRecording = false

    private var audioRecord: AudioRecord? = null
    private var androidLame: AndroidLame? = null
    private var outputStream: FileOutputStream? = null
    private var permission: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getUserProfile()
        this.initSocket()
                .checkDevicePermission()
                .initOnClickListeners()


    }

    private fun initOnClickListeners() {
        if (this@MainActivity.permission) {
            dir.mkdirs()
            startRecording.setOnClickListener { this@MainActivity.startRec() }

            stopRecording.setOnClickListener { this@MainActivity.stopRec() }

        } else {
            requestPermission()
        }
        btn_logout.setOnClickListener {
            if (!isRecording)
                logout()
            else
                Toast.makeText(this@MainActivity, "Already recording", Toast.LENGTH_SHORT).show()

        }
        btn_rec_list.setOnClickListener {
            if (!isRecording) {
                startActivity(Intent(this, RecordingsActivity::class.java))
                finish()
            } else
                Toast.makeText(this@MainActivity, "Already recording", Toast.LENGTH_SHORT).show()

        }
    }

    private fun logout() {
        Util.setRemembered(this@MainActivity, false)
        Util.setToken(this@MainActivity, "")
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }


    private fun startRec() {
        filePath = dir.absolutePath + "/tempRec.mp3"
        if (!isRecording) {
            startRecording.hide()
            edt_caption.isEnabled = false
            edt_title.isEnabled = false
            startRecThread()
        } else
            Toast.makeText(this@MainActivity, "Already recording", Toast.LENGTH_SHORT).show()

    }

    private fun stopRec() {
        startRecording.show()
        val temp = MediaPlayer.create(this, Uri.parse(filePath))
        val duration_rec = temp.duration.toLong()

        temp.release()
        duration = String.format("0%d:0%d",
                TimeUnit.MILLISECONDS.toMinutes(duration_rec),
                TimeUnit.MILLISECONDS.toSeconds(duration_rec) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration_rec))
        )
        File(filePath).delete()
        isRecording = false
    }

    private fun initSocket(): MainActivity {
        try {
            socket = IO.socket(SOCKET_ENDPOINT)
            socket!!.connect()
            socket!!.emit("mobile", "mobile connected")

        } catch (e: URISyntaxException) {
            Log.e("SocketErrorTrace", e.toString())

        }
        return this@MainActivity
    }


    private fun startRecThread() {
        object : Thread() {
            override fun run() {
                isRecording = true
                this@MainActivity.recordStreaming()
            }
        }.start()
    }


    private fun recordStreaming() {

        val buffer = this@MainActivity.initLameEncoder()
                .initRecordingEnv()
        val stamp: String = System.currentTimeMillis().toString();
        while (isRecording) {
            // 'mp3buf' should be at least 7200 bytes long to hold all possible emitted data.
            val mp3buffer = ByteArray((7200).toInt())

            val bytesRead: Int = audioRecord!!.read(buffer, 0, minBuffer)

            val bytesEncoded = androidLame!!.encode(buffer, buffer, bytesRead, mp3buffer)
            this@MainActivity.sendBuffer(mp3buffer, bytesEncoded, stamp)
            try {
                outputStream!!.write(mp3buffer, 0, bytesEncoded)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        this@MainActivity.closeRecorder(stamp)
    }

    private fun closeRecorder(stamp: String) {
        try {
            val json = JSONObject()
            json.put("title", edt_title.text)
            json.put("caption", edt_caption.text)
            json.put("user_id", user.id.toString())
            json.put("duration", duration)
            json.put("storage_directory", stamp)
            outputStream!!.close()
            socket!!.emit("finish", json)
            audioRecord!!.stop()
            audioRecord!!.release()
            androidLame!!.close()
            isRecording = false
            edt_caption.setText("")
            edt_title.setText("")
            edt_caption.isEnabled = true
            edt_title.isEnabled = true
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun initRecordingEnv(): ShortArray {
        minBuffer = AudioRecord.getMinBufferSize(inSampleRate, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT)

        audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC, inSampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, minBuffer * 2)

        try {
            outputStream = FileOutputStream(File(filePath))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        audioRecord!!.startRecording()
        //5 seconds data
        return ShortArray(inSampleRate * 2 * 5)
    }

    private fun initLameEncoder(): MainActivity {
        androidLame = LameBuilder()
                .setInSampleRate(inSampleRate)
                .setOutChannels(1)
                .setOutBitrate(128)
                .setOutSampleRate(inSampleRate)
                .build()
        return this@MainActivity
    }


    private fun sendBuffer(buffer: ByteArray, bytes: Int, stamp: String) {
        object : Thread() {
            override fun run() {
                val json = JSONObject()
                json.put("buffer_chunk", buffer)
                json.put("bytes", bytes)
                socket!!.emit("send", stamp, user!!.id, json)
            }
        }.start()
    }


    private fun checkDevicePermission(): MainActivity {
        val storage: Int = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val record: Int = ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
        this.permission = (storage == PackageManager.PERMISSION_GRANTED && record == PackageManager.PERMISSION_GRANTED)
        return this@MainActivity
    }


    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO), REQUEST_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permission", "Permission Granted")
                } else {
                    Log.e("Permission", "Permission Denied")
                }
            }
        }
    }

    private fun getUserProfile() {
        ApiProvider().getUser(object : ApiResult {
            override fun onError(e: Exception) {
                Log.e(TAG, e.message)
            }

            @SuppressLint("SetTextI18n")
            override fun onModel(baseModel: BaseModel) {
                if (baseModel is User) {
                    user.id = baseModel.id
                    user.first_name = baseModel.first_name
                    user.last_name = baseModel.last_name
                    user.email = baseModel.email
                    Log.d("user123", user!!.id.toString())
                }
            }

            override fun onJson(jsonObject: JsonObject) {
                Log.e(TAG, "Received a different model")
            }

            override fun onAPIFail() {
                Log.e(TAG, "Failed horribly")
            }

        }, Util.getToken(this@MainActivity))
    }

}

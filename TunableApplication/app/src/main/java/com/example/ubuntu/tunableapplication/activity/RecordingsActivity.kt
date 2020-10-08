package com.example.ubuntu.tunableapplication.activity

import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.View
import com.example.ubuntu.tunableapplication.R
import com.example.ubuntu.tunableapplication.util.models.BaseModel
import com.example.ubuntu.tunableapplication.util.models.RecordItem
import com.example.ubuntu.tunableapplication.util.models.RecordListener
import com.example.ubuntu.tunableapplication.util.network.ApiProvider
import com.example.ubuntu.tunableapplication.util.network.ApiResult
import com.example.ubuntu.tunableapplication.util.utility.*
import com.example.ubuntu.tunableapplication.util.network.Constants.Companion.PLAY_ENDPOINT
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_recordings.*


class RecordingsActivity : AppCompatActivity(), RecordListener {
    override fun onClickPlay(view: View, position: Int) {
        try {
            (view as FloatingActionButton).setImageResource(R.drawable.ic_stop_white)
            view.backgroundTintList = resources.getColorStateList(R.color.colorDanger)
            player.reset()
            player.setAudioStreamType(AudioManager.STREAM_MUSIC)
            player.setDataSource("$PLAY_ENDPOINT/${recordings[position].user_id}/${recordings[position].storage_directory}")
            player.prepare()
            player.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onClickStop(view: View, position: Int) {
        player.reset()
        (view as FloatingActionButton).setImageResource(R.drawable.ic_play_arrow)
        view.backgroundTintList = resources.getColorStateList(R.color.colorOrange)

    }

    var recordings: ArrayList<RecordItem> = ArrayList()
    private val player = MediaPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recordings)
        initClickListeners().getRecordings()
    }

    private fun initClickListeners(): RecordingsActivity {
        btn_logout.setOnClickListener { logout() }
        btn_record.setOnClickListener { statMainActivity() }
        return this
    }

    private fun statMainActivity() {
        val mainActivityIntent = Intent(this@RecordingsActivity, MainActivity::class.java)
        startActivity(mainActivityIntent)
        finish()
    }

    private fun getRecordings(){
        ApiProvider().getRecordings(object : ApiResult {
            override fun onError(e: Exception) {
                Log.e("HEY", e.message)
            }

            override fun onModel(baseModel: BaseModel) {

            }

            override fun onJson(jsonObject: JsonObject) {
                this@RecordingsActivity.JSONtoLIST(jsonObject)

                this@RecordingsActivity.initViewAdapter()

                this@RecordingsActivity.initSwipeHandler()
            }

            override fun onAPIFail() {

            }

        }, Util.getToken(this@RecordingsActivity))
    }

    private fun initSwipeHandler() {
        val swipeHandler = object : SwipeToDeleteCallback(this@RecordingsActivity) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val swipeAdapter = recording_list.adapter as RecordingAdapter
                swipeAdapter.removeAt(viewHolder.adapterPosition)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recording_list)
    }

    private fun initViewAdapter() {
        val adapter = RecordingAdapter(this@RecordingsActivity, recordings, this@RecordingsActivity)
        recording_list.adapter = adapter
        recording_list.layoutManager = LinearLayoutManager(this@RecordingsActivity,
                                                                LinearLayoutManager.VERTICAL,
                                                    false)
    }

    private fun JSONtoLIST(jsonObject: JsonObject) {
        val gson = Gson()
        val json = jsonObject.getAsJsonArray("Array").toString()
        recordings = gson.fromJson(json, object : TypeToken<List<RecordItem>>() {}.type)
    }

    private fun logout() {
        Util.setRemembered(this@RecordingsActivity, false)
        Util.setToken(this@RecordingsActivity, "")
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}

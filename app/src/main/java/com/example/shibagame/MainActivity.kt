package com.example.shibagame

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import io.realm.*
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import io.realm.kotlin.createObject
import io.realm.kotlin.oneOf
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import java.util.*

class MainActivity : AppCompatActivity(){

    lateinit var startSound : SoundPool
    lateinit var mRealm : Realm
    var snd0 = 0
    var rank = mutableListOf<Int>(0,0,0,0)
    var gamescore = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Realm.init(this)
        mRealm = Realm.getDefaultInstance()
//        initRealm()
        // delete()
        val getData = read()
        getData.forEach {
            rank.add(it.score)
        }
        rank.sortDescending()
        rt1.text = rank[0].toString()
        rt2.text = rank[1].toString()
        rt3.text = rank[2].toString()


        val aa0 = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
        startSound = SoundPool.Builder().setAudioAttributes(aa0).setMaxStreams(2).build()
        snd0 = startSound.load(this, R.raw.menshajimei1, 1)
        startButton.setOnClickListener { onStartButtonTapped(it) }



    }

    fun onStartButtonTapped(view: View?) {
        val intent = Intent(this, MainGameActivity::class.java)
        startActivity(intent)
        startSound.play(snd0,1f,1f,2,0,1f)
    }
    override fun onResume() {
        super.onResume()

        val intent = getIntent()
        gamescore = intent.getIntExtra("score", 0)
        intent.putExtra("redata",0)
        Log.d("gamescore", gamescore.toString())
        add(gamescore)
        gamescore = 0
        for (i  in 0 until rank.size ) rank[i] = 0
        val getData = read()
        getData.forEach{
            rank.add(it.score)
            Log.d("rank-onStart",it.score.toString())
        }
        rank.sortDescending()
        rt1.text = rank[0].toString()
        rt2.text = rank[1].toString()
        rt3.text = rank[2].toString()
    }
    override fun onDestroy() {
        super.onDestroy()
        startSound.release()
        delete()
        mRealm.close()


    }

    //データベース周り________________________________\
    //データの追加
    fun add(score : Int = 0){
        mRealm.executeTransaction{
            var book = mRealm.createObject(Score::class.java, UUID.randomUUID().toString())
            book.score = score
            mRealm.copyToRealm(book)
        }
    }
    //データ読み取り
    fun read(): RealmResults<Score>{
                return mRealm.where(Score::class.java).findAll().sort("score",Sort.DESCENDING)
    }
    //データの更新
    fun update(id:String, num : Int, score:Int = 0){
        mRealm.executeTransaction{
            var book = mRealm.where(Score::class.java).equalTo("id",id).findFirst()
            book!!.num = num
            if(score != 0){
                book.score = score
            }
        }
    }
    //データの削除
    fun delete(){
        mRealm.executeTransaction{
//            var book = mRealm.where(Score::class.java).equalTo("id",id).findAll()
            var book = mRealm.where(Score::class.java).findAll()
            book.deleteAllFromRealm()
        }
    }

    private fun initRealm(){
        val realmConfiguration = RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .schemaVersion(0)
            .build()
        mRealm = Realm.getInstance(realmConfiguration)
    }
}

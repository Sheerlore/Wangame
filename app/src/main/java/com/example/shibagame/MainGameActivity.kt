package com.example.shibagame

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.preference.PreferenceManager
import android.view.MotionEvent
import android.view.SurfaceHolder
import androidx.core.content.getSystemService
import kotlinx.android.synthetic.main.activity_main_game.*


class MainGameActivity : AppCompatActivity(),SensorEventListener,SurfaceHolder.Callback {

    private var time :Long = 0L
    lateinit var Gamesound : SoundPool
    var snd1 = 0 //bakuhatu
    var snd2 = 0 //dog
    var snd3 = 0 //trumpet
    var snd4 = 0 //lose

    lateinit var v :Vibrator




    // その他の変数 ===========================================================
    //画像周り
    private var surfaceWidth:Int = 0
    private var surfaceHeight:Int = 0
    val bitmap_size :Int = 32   //画像のサイズ
    //ゲームスコア
    var score : Int  = 0
    //ライフ用変数
    var Life= mutableListOf<String>("♥","♥","♥","♥","♥")
    var life: String = " "
    //ゲーム時間
    var gameTime : Int= 0
    //主人公の生死判定
    var result : Boolean = true
    //加速度センサー用変数
    private var xx = 0f
    private var yy = 0f
    //ハンドラー
    var iniPro = true
    var startCount = 5
    var dercount = 0
    //===================================================@@@@====================

    //Player =================================================================
    private var playerX = 0f //主人公の現在のX座標
    private var playerY = 0f //主人公の現在のY座標
    private var pvx :Float = 0f
    private var pvy :Float = 0f
    private var coef = 50.0f //移動量
    private var friction = 0.85f
    //===================================================@@@@====================

    //Enemy =================================================================
    private var husfun_N = 0
    private var husfun_num = 0
    private var husX = mutableListOf<Float>(0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f)
    private var husY = mutableListOf<Float>(0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f)
    private var burufun_N = 2 //初期値2
    private var buruX = mutableListOf<Float>(0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f)
    private var buruY = mutableListOf<Float>(0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f)
    private val hasmove1 = 7
    private val hasmove2 = 7
    private val burumove1 = 5
    private val burumove2 = 5
    //===================================================@@@@====================


    //item =======================================================================
    //骨（ライフ回復）
    private var honeX = 0f
    private var honeY = 0f
    private var bakudanX = 0f
    private var bakudanY = 0f


    //===================================================@@@@======================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_game)
        val holder = surfaceView.holder
        holder.addCallback(this)

        val aa1 = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()
        Gamesound = SoundPool.Builder().setAudioAttributes(aa1).setMaxStreams(5).build()
        snd1 = Gamesound.load(this,R.raw.bakuhatu,2)
        snd2 = Gamesound.load(this,R.raw.dog1,1)
        snd3 = Gamesound.load(this,R.raw.trumpet1,1)
        snd4 = Gamesound.load(this,R.raw.make,2)

        v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator


    }

    override fun onPause() {
        super.onPause()
        score = 0
    }

    override fun onTouchEvent(event: MotionEvent):Boolean{

        when(event.action){
            MotionEvent.ACTION_DOWN -> {
                if(result){
                    playdog()
                }else{
                    playdog()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("score", score)
                    startActivity(intent)
                }
            }
        }
        return super.onTouchEvent(event)
    }



    //センサー周り ======================================================Sensor===
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }


    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = -event.values[0]
                val y = event.values[1]
                xx = x
                yy = y

            //ここに主人公などの移動処理
            Player_move(x, y)


            if (gameTime >= 250f) {
                when(husfun_num){
                    0 ->{ //move1
                        husfun_N = hasmove1
                        for (i in 0..husfun_N) {
                            Husky_move_1(i)
                            if (iniPro){
                                if (gameTime >= 350) iniPro = false
                            }else{
                                HitCheck(playerX, playerY, husX[i], husY[i], 1)
                            }
                        }
                    }
                    1 ->{//move2
                        husfun_N = hasmove1
                        for (i in 0..husfun_N) {
                            Husky_move_2(i)
                            if (iniPro){
                                if (gameTime >= 350) iniPro = false
                            }else{
                                HitCheck(playerX, playerY, husX[i], husY[i], 1)
                            }
                        }
                    }
                    2 -> {
                        burufun_N = burumove1
                        for (i in 0..burufun_N) {
                            Buru_move_1(i)
                            if (iniPro){
                                if (gameTime >= 350) iniPro = false
                            }else{
                                HitCheck(playerX, playerY, buruX[i], buruY[i], 1)
                            }
                        }
                    }
                    3 -> {
                        burufun_N = burumove2
                        for (i in 0..burufun_N) {
                            Buru_move_2(i)
                            if (iniPro){
                                if (gameTime >= 350) iniPro = false
                            }else{
                                HitCheck(playerX, playerY, buruX[i], buruY[i], 1)
                            }
                        }

                    }
                    else ->{
                        husfun_N = hasmove1
                        for (i in 0..husfun_N) {
                            Husky_move_1(i)
                            if (iniPro){
                                if (gameTime >= 350) iniPro = false
                            }else{
                                HitCheck(playerX, playerY, husX[i], husY[i], 1)
                            }
                        }

                    }

                }

            }

            HitCheck(playerX,playerY,honeX,honeY,2)
            HitCheck(playerX,playerY,bakudanX,bakudanY,3)

        }
        if(dercount == 500){
            husfun_num = (Math.random() * 4).toInt() //関数のタグ取得
            honeX = (Math.random() * surfaceWidth).toFloat()
            honeY = (Math.random() * surfaceHeight).toFloat()
            bakudanX = (Math.random() * surfaceWidth).toFloat()
            bakudanY = (Math.random() * surfaceHeight).toFloat()
            dercount = 0
        }
        dercount++
        if(result){
            gameTime++
        }else{
            score = gameTime
        }
        drawCanvas()
    }

    //==================================================================Sensor===


    //サーフェイス周り =================================================Surface=====
    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

        surfaceWidth = width
        surfaceHeight = height
        playerX = (width/2).toFloat()
        playerY = (height/2).toFloat()
        husfun_num = (Math.random() * 4).toInt() //関数のタグ取得

        when(husfun_num){
            0 -> {
                husfun_N = hasmove1
                for (i in 0..husfun_N) {
                    husX[i] = 0f
                    husY[i] = 0f
                }
            }
            1 -> {
                husfun_N = hasmove2
                for (i in 0..husfun_N) {
                    husX[i] = surfaceWidth.toFloat() - bitmap_size * 2
                    husY[i] = 0f
                }
            }
            2 -> {
                burufun_N = burumove1
                for (i in 0..burufun_N) {
                    buruX[i] = (Math.random() * surfaceWidth).toFloat()
                    buruY[i] = 0f
                }
            }
            3 -> {
                burufun_N = burumove2
                for (i in 0..burufun_N) {
                    buruX[i] = surfaceWidth.toFloat()
                    buruY[i] = (Math.random() * surfaceHeight - 800).toFloat()
                }
            }
            else -> {
                husfun_N = hasmove1
                for (i in 0..husfun_N) {
                    husX[i] = 0f
                    husY[i] = 0f
                }
            }
        }

        honeX = (Math.random() * width).toFloat()
        honeY = (Math.random() * height).toFloat()
        bakudanX = (Math.random() * width).toFloat()
        bakudanY = (Math.random() * height).toFloat()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        //センサーの監視を終了する
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.unregisterListener(this)

        Gamesound.release()
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        //センサーの監視を開始する
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_GAME)
        life = Life.joinToString(separator = "")
    }

    //==================================================================Surface=====

    //描画用、、主に画像---------------------------------------------------------------DrawCanvas()
    private fun drawCanvas() {
        val canvas = surfaceView.holder.lockCanvas()
        if (result) {
            canvas.drawColor(Color.WHITE)
        } else {
            canvas.drawColor(Color.RED)

            canvas.drawText(
                "${score}点",
                50f,
                surfaceHeight / 2f - 300f,
                Paint().apply {
                textSize = 200f
            })

            canvas.drawText(
                "GAMEOVER",
                surfaceWidth / 2f - 420f,
                surfaceHeight / 2f,
                Paint().apply {
                    textSize = 140f
                })
            canvas.drawText(
                "画面をタップしてください",
                surfaceWidth / 2f - 480f,
                surfaceHeight / 2f + 200,
                Paint().apply {
                    textSize = 80f
                })
        }

        val shibaImage: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.shiba)
        val huskyImage: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.hasky)
        val buruImage: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.burudogtoumei)
        val boneImage: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.kaifukuitemtoumei)
        val bakudanImage: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.bakudanntoumei)

        if(result){
            if (gameTime <= 250) {
                canvas.drawText(
                    "$startCount",
                    surfaceWidth / 2f - 50f,
                    surfaceHeight / 2f,
                    Paint().apply {
                        textSize = 120f
                    })
                canvas.drawText("画面を傾けて柴犬を動かそう！！", surfaceWidth / 2f - 350f, surfaceHeight / 2f + 100f, Paint().apply {
                    textSize = 50f
                })
                canvas.drawText("襲いかかる犬を避けてください", surfaceWidth / 2f - 350f, surfaceHeight / 2f + 160f, Paint().apply {
                    textSize = 50f
                })
                canvas.drawText("骨を取るとライフが増えます", surfaceWidth / 2f - 350f, surfaceHeight / 2f + 220f, Paint().apply {
                    textSize = 50f
                })
                canvas.drawText("爆弾に気をつけて！", surfaceWidth / 2f - 350f, surfaceHeight / 2f + 280f, Paint().apply {
                    textSize = 50f
                })
                if (gameTime % 50 == 0) {
                    startCount--
                }
            }
            if (startCount == 0 && gameTime <= 350) {
                canvas.drawText("Start", surfaceWidth / 2f - 200f, surfaceHeight / 2f, Paint().apply {
                    textSize = 200f
                })
            }

        }


        canvas.drawText("Score :  $gameTime", 20f, 240f, Paint().apply {
            textSize = 60f
        })
//        canvas.drawText("deathtime :  $deathTime", 20f, 310f, Paint().apply {
//            textSize = 60f
//        })

        canvas.drawText("LIFE : ${life}", 20f, 100f, Paint().apply {
            textSize = 60f
        })
        canvas.drawText("TIME : ${timeToText(gameTime)}", 20f, 170f, Paint().apply {
            textSize = 60f
        })
//        canvas.drawText("random : ${husfun_num}", 300f, 170f, Paint().apply {
//            textSize = 60f
//        })


        //主人公（柴犬）
        canvas.drawBitmap(shibaImage, playerX, playerY, Paint().apply {
            isFilterBitmap = isAntiAlias
        })

        //敵出現
        if (gameTime >= 250f) {
            when (husfun_num) {
                0 -> {
                    husfun_N = hasmove1
                    for (i in 0..husfun_N) {
                        canvas.drawBitmap(huskyImage, husX[i], husY[i], Paint().apply {
                            isFilterBitmap = isAntiAlias
                        })
                    }
                }
                1 -> {
                    husfun_N = hasmove2
                    for (i in 0..husfun_N) {
                        canvas.drawBitmap(huskyImage, husX[i], husY[i], Paint().apply {
                            isFilterBitmap = isAntiAlias
                        })
                    }
                }
                2 -> {
                    burufun_N = burumove1
                    for (i in 0..burufun_N) {
                        canvas.drawBitmap(buruImage, buruX[i], buruY[i], Paint().apply {
                            isFilterBitmap = isAntiAlias
                        })
                    }
                }
                3 -> {
                    burufun_N = burumove2
                    for (i in 0..burufun_N) {
                        canvas.drawBitmap(buruImage, buruX[i], buruY[i], Paint().apply {
                            isFilterBitmap = isAntiAlias
                        })
                    }

                }
                else -> {
                    husfun_N = hasmove1
                    for (i in 0..husfun_N) {
                        canvas.drawBitmap(huskyImage, husX[i], husY[i], Paint().apply {
                            isFilterBitmap = isAntiAlias
                        })
                    }
                }

            }
        }
            canvas.drawBitmap(boneImage,honeX,honeY,Paint().apply {
                isFilterBitmap=isAntiAlias
            })

        canvas.drawBitmap(bakudanImage,bakudanX,bakudanY,Paint().apply {
            isFilterBitmap=isAntiAlias
        })
        //骨


        surfaceView.holder.unlockCanvasAndPost(canvas)
    }
    //描画用、、主に画像---------------------------------------------------------------DrawCanvas()



    //******************************************************************MyFunction********


    //主人公（柴犬）用____________________________________________________________________Player
    fun Player_move(x: Float, y :Float){
        var t = (System.currentTimeMillis() - time).toFloat()
        time = System.currentTimeMillis()
        t /= 1000.0f

        val dx = pvx * t + x * t * t / 2.0f
        val dy = pvy * t + y * t * t / 2.0f
        if(gameTime <= 150){
            playerX = (surfaceWidth/2).toFloat()
            playerY = (surfaceHeight/2).toFloat()
        }else{
            playerX += dx * coef * (1- friction)
            playerY += dy * coef * (1- friction)

        }
        pvx += x
        pvy += y

        if (playerX < 0 - bitmap_size * 2 && pvx < 0) {
//            playerX = 0f
            pvx = -pvx / 1.5f
            playerX = -bitmap_size.toFloat()
        }
        if (playerX > surfaceWidth - bitmap_size * 2 && pvx > 0) {
//            playerX = (surfaceWidth - bitmap_size * 2).toFloat()
            pvx = -pvx / 1.5f
            playerX = surfaceWidth - bitmap_size.toFloat()
        }
        if (playerY < 0 - bitmap_size* 2 && pvy < 0) {
//            playerY = 0f
            pvy = -pvy / 1.5f
            playerY = -bitmap_size.toFloat()
        }
        if (playerY > surfaceHeight - bitmap_size * 2 && pvy > 0) {
//            playerY = (surfaceHeight - bitmap_size * 2).toFloat()
            pvy = -pvy / 1.5f
            playerY = surfaceHeight - bitmap_size .toFloat()
        }
    }
    //主人公（柴犬）用____________________________________________________________________Player

    //ハスキー用 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Husky
    fun Husky_move_1(index : Int){
        husX[index] += 10f
        husY[index] += 20f
        if (husX[index] > surfaceWidth - bitmap_size ) {
            husX[index] = 0f
        }
        if (husY[index] > surfaceHeight - bitmap_size) {
            husY[index] = 0f
        }
    }
    fun Husky_move_2(index : Int){
        husX[index] -= 10f
        husY[index] += 20f
        if (husX[index]  < 0 - bitmap_size * 2 ) {
            husX[index] = surfaceWidth.toFloat() - bitmap_size
        }
        if (husY[index] > surfaceHeight - bitmap_size) {
            husY[index] = 0f
        }
    }
    //ハスキー用~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Husky

    //ブルドック用~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Buru
    fun Buru_move_1(index: Int){
        buruY[index] +=10f
        if (buruY[index] > surfaceHeight - bitmap_size ) {
            buruX[index]=(Math.random()*surfaceWidth).toFloat()
            buruY[index]=0f
        }
    }
    fun Buru_move_2(index: Int) {
        buruX[index] -= 10f
        buruY[index] = (400 * Math.sin((buruX[index]).toDouble() / 50) + 700).toFloat()
        if (buruX[index] < 0 - bitmap_size * 2) {
            buruX[index] = surfaceWidth.toFloat()
            buruY[index] = (Math.random() * surfaceHeight).toFloat()
        }
    }
    //ブルドック用~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Buru




    //ヒットチェック用___________________________________________________________________HitCheck

    //[tag]  (1 : enemy) ,  (2 : hone)
    fun HitCheck(px : Float, py : Float, ex: Float, ey : Float,tag : Int){
        var player_pos_x = px
        var player_pos_xw = px+bitmap_size
        var player_pos_y = py
        var player_pos_yh = py+bitmap_size

        var enemy_pos_x = ex
        var enemy_pps_xw = ex+bitmap_size
        var enemy_pos_y = ey
        var enemy_pos_yh = ey+bitmap_size

        //[tag]  (1 : enemy) ,  (2 : hone) , (3 : bakudan)
        if (tag == 1){
            if((player_pos_x < enemy_pps_xw + bitmap_size) &&
                (enemy_pos_x < player_pos_xw + bitmap_size) &&
                (player_pos_y < enemy_pos_yh + bitmap_size) &&
                (enemy_pos_y < player_pos_yh+ bitmap_size)){
                life = life.dropLast(1)
                score++ //当たった回数
                playdog()
                v.vibrate(300)
                if(life.isEmpty()){
                    result = false
                    playlose()
                }
            }
        }else if (tag == 2){
            if((player_pos_x < enemy_pps_xw+ bitmap_size) &&
                (enemy_pos_x < player_pos_xw+ bitmap_size) &&
                (player_pos_y < enemy_pos_yh+ bitmap_size) &&
                (enemy_pos_y < player_pos_yh+ bitmap_size)){
                playtrum()
                if(life.length < 10){
                    life+="♥"
                }

            }
        }else if (tag == 3){
            if((player_pos_x < enemy_pps_xw+ bitmap_size) &&
                (enemy_pos_x < player_pos_xw+ bitmap_size) &&
                (player_pos_y < enemy_pos_yh+ bitmap_size) &&
                (enemy_pos_y < player_pos_yh+ bitmap_size)){
                playbakuhatu()
                life = ""
                v.vibrate(300)
                if(life.isEmpty()){
                    result = false
                    playlose()
                }
            }
        }



    }

    private fun timeToText(time : Int = 0): String? {
        return if (time < 0){
            null
        }else if(time == 0){
            "00:00.00"
        } else {
            val h = time / 3600
            val m = time % 3600/60
            val s = time % 60
            "%1$02d:%2$02d.%3$02d".format(h,m,s)
        }
    }
    //___________________________________________________________________________________HitCheck

    fun playbakuhatu(){
        Gamesound.play(snd1,1f,1f,1,0,1f)
    }
    fun playdog(){
        Gamesound.play(snd2,1f,1f,1,0,1f)
    }
    fun playtrum(){
        Gamesound.play(snd3,1f,1f,1,0,1f)
    }
    fun playlose(){
        Gamesound.play(snd4,1f,1f,1,0,1f)
    }

    //******************************************************************MyFunction********
}

package com.yukilonger.bible

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.*
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.setPadding
import java.io.*


class MainActivity : AppCompatActivity() {
    private var olds : List<String> = mutableListOf(
        "创世纪",
        "出埃及记",
        "利未记",
        "民数记",
        "申命记",
        "约书亚记",
        "士师记",
        "路得记",
        "撒母耳记上",
        "撒母耳记下",
        "列王纪上",
        "列王纪下",
        "历代志上",
        "历代志下",
        "以斯拉记",
        "尼希米记",
        "以斯帖记",
        "约伯记",
        "诗篇",
        "箴言",
        "传道书",
        "雅歌",
        "以赛亚书",
        "耶利米书",
        "耶利米哀歌",
        "以西结书",
        "但以理书",
        "何西阿书",
        "约珥书",
        "阿摩司书",
        "俄巴底亚书",
        "约拿书",
        "弥迦书",
        "那鸿书",
        "哈巴谷书",
        "西番雅书",
        "哈该书",
        "撒迦利亚书",
        "玛拉基书"
    )
    private var news : List<String> = mutableListOf(
        "马太福音",
        "马可福音",
        "路加福音",
        "约翰福音",
        "使徒行传",
        "罗马书",
        "哥林多前书",
        "哥林多后书",
        "加拉太书",
        "以弗所书",
        "腓立比书",
        "歌罗西书",
        "帖撒罗尼迦前书",
        "帖撒罗尼迦后书",
        "提摩太前书",
        "提摩太后书",
        "提多书",
        "腓利门书",
        "希伯来书",
        "雅各书",
        "彼得前书",
        "彼得后书",
        "约翰一书",
        "约翰二书",
        "约翰三书",
        "犹大书",
        "启示录"
    )
    private var curText: TextView? = null
    private var oldsCount: Int = 0
    private var total: Int = 0
    private var fileCount = 0

    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

    var musicFiles:Array<File>? = null
    var musicBinder: IBinder? = null
    var musicService: MusicService? = null
    var holder: SurfaceHolder? = null
    var musicIndex = 0
    var currentNumber = 0
    var selectedColor = Color.argb(255,189,36,39)
    var defaultColor = 0

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            musicBinder = service
            val localBinder = service as MusicService.MyBinder
            musicService = localBinder.getService()
            holder?.let {
                musicService?.initialization(it, 0, 0) {
                    playNext()
                }
                if(musicService?.isPlaying() == false) {
                    read()
                }
            }
        }
        override fun onServiceDisconnected(name: ComponentName) {
            musicBinder = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val readPermission = ActivityCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED
        if (readPermission) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                requestPermissions(permissions, 1)
            }
            else
            {
                requestPermissions(permissions, 1)
            }
        }

//        acquireWakeLock()

        var oldView = findViewById<LinearLayout>(R.id.olds)
        var newView = findViewById<LinearLayout>(R.id.news)
        var titleView = findViewById<TextView>(R.id.title)
        defaultColor = titleView.currentTextColor

        oldsCount = olds.count()
        total = oldsCount + news.count()

        olds.forEachIndexed { index, it ->
            var number = index + 1
            TextView(this).apply {
                id = number
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                text = it
                textSize = 20F
                setPadding(10)
                setOnClickListener {
                    select(number)
                }
                oldView.addView(this)
            }
        }

        news.forEachIndexed { index, it ->
            var number = oldsCount + index + 1
            TextView(this).apply {
                id = number
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                text = it
                textSize = 20F
                setPadding(10)
                setOnClickListener {
                    select(number)
                }
                newView.addView(this)
            }
        }

        // 视频播放
        val bindIntent = Intent(this, MusicService::class.java)
        val player = findViewById<SurfaceView>(R.id.player)
        holder = player.holder
        holder?.addCallback(object : SurfaceHolder.Callback{
            override fun surfaceCreated(p0: SurfaceHolder) {
                if(musicService == null) {
                    startService(bindIntent)
                    bindService(bindIntent, connection, BIND_AUTO_CREATE)
                }
            }
            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {}
            override fun surfaceDestroyed(p0: SurfaceHolder) {}
        })
    }

    override fun onDestroy() {
        save()
        super.onDestroy()
//        releaseWakeLock()
        unbindService(connection)
        val data = Parcel.obtain()
        musicBinder!!.transact(MusicService.ServiceCode.Destroy.value, data, null, 0)
    }

    private fun select(number: Int, index: Int? = null) {
        var text = findViewById<TextView>(number)
        if (text == curText)
            return
        text.setTextColor(selectedColor)
        curText?.setTextColor(defaultColor)
        curText = text

        currentNumber = number
        musicFiles = null
        fileCount = 0
        val dir = Environment.getExternalStorageDirectory().absolutePath
        val musicFolder = File("${dir}/bible/${number}")
        if (musicFolder.exists()) {
            // 异常：listFiles返回空时，将build.gradle中targetSdk设置为28
            musicFiles = musicFolder.listFiles()
            if (musicFiles == null)
                return
            fileCount = musicFiles!!.count()
            if (fileCount == 0)
                return
            musicFiles!!.sortWith(compareBy { it.nameWithoutExtension.toInt() })
            musicIndex = index ?: 0
            play()
            // 长按弹出每一章选项
            registerForContextMenu(curText)
        }
    }

    private fun play(index: Int? = null) {
        musicIndex = index ?: musicIndex
        if (musicIndex > fileCount-1)
            return

        val data = Parcel.obtain()
        val reply = Parcel.obtain()

        try {
            data.writeInterfaceToken("MusicService")
            data.writeString(musicFiles?.get(musicIndex)?.path)
            musicBinder!!.transact(MusicService.ServiceCode.SetPath.value, data, null, 0)
            musicBinder!!.transact(MusicService.ServiceCode.Play.value, data, null, 0)
        } catch (e: RemoteException) {
            e.printStackTrace()
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    private fun playNext() {
        if (musicFiles == null)
            return
        musicIndex++
        if (musicIndex > fileCount-1) {
            // 下一部
            currentNumber++
            if (currentNumber <= total) {
                select(currentNumber)
            }
            // 启示录之后停止播放
            return
        }
        play()
    }

    private fun save() {
        try {
            val output = openFileOutput("data", Context.MODE_PRIVATE)
            val write = BufferedWriter(OutputStreamWriter(output))
            write.use {
                it.write("${currentNumber},${musicIndex}")
            }
        }
        catch(e: Exception)  {

        }
    }

    private fun read() {
        try {
            val input = openFileInput("data")
            val reader = BufferedReader(InputStreamReader(input))
            reader.use {
                var info = reader.readLine()
                var arr = info.split(',')
                if (arr.count() == 2) {
                    currentNumber = arr[0].toInt()
                    musicIndex = arr[1].toInt()
                    if (currentNumber > 0) {
                        select(currentNumber, musicIndex)
                    }
                }
            }
        }
        catch(e: Exception)  {

        }
    }

    // 长按弹出每一章选项
    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        musicFiles?.forEachIndexed { index, file ->
            menu?.add(0, index, 0, "第${file.nameWithoutExtension}章")
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        play(item.itemId.toInt())
        return true
    }


    // 测试一次失败，锁屏后长时间后依旧会被杀掉进程
    // 目前只有手动改变电池使用策略：不受限制，才不会锁屏后被杀掉进程
    //获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
    //    <uses-permission android:name="android.permission.WAKE_LOCK"/>
    //    <uses-permission android:name="android.permission.DEVICE_POWER"/>
    private var wakeLock:PowerManager.WakeLock? = null
    @SuppressLint("InvalidWakeLockTag")
    private fun acquireWakeLock() {
        if (null == wakeLock) {
            val pm = this.getSystemService(POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE,
                "PostLocationService"
            )
            wakeLock?.acquire()
        }
    }

    //释放设备电源锁
    private fun releaseWakeLock() {
        if (null != wakeLock) {
            wakeLock!!.release()
            wakeLock = null
        }
    }
}
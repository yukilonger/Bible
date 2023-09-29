package com.yukilonger.bible

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.os.Parcel
import android.os.RemoteException
import android.view.SurfaceHolder

class MusicService : Service() {
    private val myBinder: IBinder = MyBinder()
    enum class ServiceCode(val value: Int) {
        Play(0), Pause(1), Stop(2), Seek(3), SetPath(4), GetPath(5), Destroy(6), Display(7)
    }
    val mediaPlayer : MediaPlayer = MediaPlayer()
    var mediaPath : String = "";
    var surfaceHolder : SurfaceHolder? = null
    var viewWidth = 0
    var viewHeight = 0

    override fun onBind(intent: Intent): IBinder {
        return myBinder;
    }

    fun play(){
        mediaPlayer.start()
    }

    fun pause(){
        mediaPlayer.pause()
    }

    fun stop(){
        mediaPlayer.stop()
    }

    fun seek(pos: Int) {
        mediaPlayer.seekTo(pos)
    }

    fun destroy(){
        mediaPlayer.stop()
//        mediaPlayer.release()
    }

    fun setSource(path:String?){
        mediaPlayer.reset()
        mediaPath = path!!
        mediaPlayer.setDataSource(mediaPath)
        mediaPlayer.prepare()
    }

    fun display(holder: SurfaceHolder){
        mediaPlayer.setDisplay(holder)
    }

    fun initialization(holder: SurfaceHolder, width: Int, height: Int, callback: () -> Unit){
        surfaceHolder = holder
        viewWidth = width
        viewHeight = height

        mediaPlayer.setOnCompletionListener {
            callback()
        }
    }

    fun isPlaying():Boolean{
        return mediaPlayer.isPlaying
    }

    inner class MyBinder : Binder() {
        fun getService(): MusicService{
            return this@MusicService
        }

        @SuppressLint("ParcelClassLoader")
        @Throws(RemoteException::class)
        public override fun onTransact(
            code: Int,
            data: Parcel,
            reply: Parcel?,
            flags: Int
        ): Boolean {
            when (code) {
                ServiceCode.Play.value -> play()
                ServiceCode.Pause.value -> pause()
                ServiceCode.Stop.value -> stop()
                ServiceCode.Seek.value -> {
                    data.enforceInterface("MusicService")
                    seek(data.readInt())
                }
                ServiceCode.SetPath.value -> {
                    data.enforceInterface("MusicService")
                    setSource(data.readString())
                }
                ServiceCode.GetPath.value -> {
                    data.enforceInterface("MusicService")
                    reply?.writeString(mediaPath)
                }
                ServiceCode.Destroy.value -> destroy()
            }
            return super.onTransact(code, data, reply, flags)
        }
    }
}
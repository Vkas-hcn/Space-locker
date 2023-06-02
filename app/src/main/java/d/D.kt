package d

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import sl.wo.ip.R
import sl.wo.ip.utils.FrontProcessManagement
import sl.wo.ip.utils.LockWindow
import sl.wo.ip.utils.SpaceLockerUtils
import com.xuexiang.xui.utils.Utils
import kotlinx.coroutines.*

class D : Service() {
    private var mLockJob: Job? = null

    override fun onCreate() {
        super.onCreate()

        mLockJob = GlobalScope.launch(Dispatchers.IO) {
            var lastAppPackageName = ""
            while (true) {
                delay(300)
                val currentTopApp = FrontProcessManagement.getForegroundPackageName(applicationContext)
                if (!Utils.isNullOrEmpty(currentTopApp) && currentTopApp != lastAppPackageName) {
                    lastAppPackageName = currentTopApp
                    checkAndShowPasswordBox(currentTopApp)
                }
            }
        }

        createNotificationChannel()
        startForeground(1, buildNotification())
    }

    private fun checkAndShowPasswordBox(packageName: String) {
        val lockedApps = SpaceLockerUtils.appList.filter { it.isLocked && it.packageNameSl == packageName }
        if (lockedApps.isNotEmpty()) {
            showPasswordBox()
        }
    }

    private fun showPasswordBox() {
        GlobalScope.launch(Dispatchers.Main) {
            LockWindow.getInstance().initWindow(applicationContext)
            LockWindow.getInstance().showPasswordBox()
        }
    }

    private fun createNotificationChannel() {
        val messageNotificatioManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("5996773", "Space Locker", NotificationManager.IMPORTANCE_DEFAULT)
            channel.enableLights(true)
            channel.lightColor = Color.GREEN
            channel.setShowBadge(false)
            messageNotificatioManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this)
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
        builder.setContentText("Space Locker is working")
        builder.setWhen(System.currentTimeMillis())
        builder.setChannelId("5996773")
        return builder.build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        mLockJob?.cancel()
    }
}
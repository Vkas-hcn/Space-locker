package sl.wo.ip.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import sl.wo.ip.utils.LockWindow.InstanceHelper.lockWindowHelper
import android.content.Intent

class SlBroadcastReceiver : BroadcastReceiver() {
    private val SYSTEM_DIALOG_REASON_KEY = "reason"
    private val SYSTEM_DIALOG_REASON_HOME_KEY = "homekey"
    private val SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps"
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
            val reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY) ?: return

            // Home键
            if (reason == SYSTEM_DIALOG_REASON_HOME_KEY) {
                lockWindowHelper.closeThePasswordBox()
            }

            // 最近任务列表键
            if (reason == SYSTEM_DIALOG_REASON_RECENT_APPS) {
                lockWindowHelper.closeThePasswordBox()
            }
        }
    }
}
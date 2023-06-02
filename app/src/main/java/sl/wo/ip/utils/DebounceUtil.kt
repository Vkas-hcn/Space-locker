package sl.wo.ip.utils

import android.os.Handler
import android.os.Looper

class DebounceUtil (private val delayMillis: Long) {
    private var isDebouncing = false
    private val handler = Handler(Looper.getMainLooper())
    fun debounce(action: () -> Unit) {
        if (isDebouncing) {
            return
        }

        isDebouncing = true

        handler.postDelayed({
            action.invoke()
            isDebouncing = false
        }, delayMillis)
    }

    fun cancel() {
        handler.removeCallbacksAndMessages(null)
        isDebouncing = false
    }
}
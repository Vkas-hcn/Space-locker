package sl.wo.ip.bean

import android.graphics.drawable.Drawable
import androidx.annotation.Keep

@Keep
class SlAppBean {
    var appNameSl: String? = null
    var packageNameSl: String? = null
    var isLocked: Boolean = false
    var appIconSl: Drawable? = null
    var installTime:Long?=null
}
package a;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class I extends AppCompatTextView {

    private float mRadius;
    private boolean mHasPassword;

    private Paint mPaint;

    public I(Context context) {
        this(context, null);
    }

    public I(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public I(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    private void initPaint() {
        // 画一个黑色的圆
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mHasPassword) {
            canvas.drawCircle(getWidth() >> 1, getHeight() >> 1, mRadius, mPaint);
        } else {
            super.onDraw(canvas);
        }
    }

    /**
     * 清除密码
     */
    public void clearPassword() {
        mHasPassword = false;
        mPaint.setColor(Color.BLACK);
        invalidate();
    }

    /**
     * 绘制密码
     *
     * @param radius
     */
    public void drawPassword(float radius) {
        mHasPassword = true;
        if (radius == 0) {
            mRadius = getWidth() >> 2; //除以4
        } else {
            mRadius = radius;
        }
        invalidate();
    }

    /**
     * 设置密码颜色
     */
    public void setPasswordColor(int colorData) {
        mPaint.setColor(colorData);
        invalidate();
    }
}


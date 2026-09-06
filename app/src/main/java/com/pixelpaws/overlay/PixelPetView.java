package com.pixelpaws.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

final class PixelPetView extends View {
    interface DragListener { void moveBy(float dx, float dy); }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final PetState state;
    private final DragListener dragListener;
    private float downX, downY, lastRawX, lastRawY;
    private long downAt;
    private boolean moved, menuOpen = false, sleeping = false;
    private int frame = 0;

    PixelPetView(Context context, PetState state, DragListener listener) {
        super(context);
        this.state = state;
        this.dragListener = listener;
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        postDelayed(animator, 380);
    }

    private final Runnable animator = new Runnable() {
        @Override public void run() { frame++; invalidate(); postDelayed(this, 380); }
    };

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        float u = getWidth() / 18f;
        if (menuOpen) drawMenu(c);
        drawShadow(c, u);
        drawCat(c, u);
        if (!menuOpen) drawHint(c);
    }

    private void drawCat(Canvas c, float u) {
        float bounce = sleeping ? 4f : ((frame % 2 == 0) ? 0f : -u * .3f);
        c.save();
        c.translate(0, bounce + (menuOpen ? dp(48) : dp(8)));
        int outline = Color.rgb(70, 58, 68), fur = Color.rgb(246, 174, 190);
        int light = Color.rgb(255, 218, 225), dark = Color.rgb(217, 126, 151);
        px(c, 3, 4, 3, 4, outline, u); px(c, 12, 4, 3, 4, outline, u);
        px(c, 4, 3, 2, 4, fur, u); px(c, 12, 3, 2, 4, fur, u);
        px(c, 4, 6, 10, 8, outline, u); px(c, 5, 6, 8, 7, fur, u);
        px(c, 6, 7, 6, 3, light, u);
        if (sleeping) {
            px(c, 6, 9, 2, 1, outline, u); px(c, 10, 9, 2, 1, outline, u);
        } else {
            px(c, 6, 8, 2, 2, outline, u); px(c, 10, 8, 2, 2, outline, u);
            px(c, 7, 8, 1, 1, Color.WHITE, u); px(c, 11, 8, 1, 1, Color.WHITE, u);
        }
        px(c, 8, 10, 2, 1, dark, u); px(c, 9, 11, 1, 1, outline, u);
        px(c, 5, 13, 8, 4, outline, u); px(c, 6, 13, 6, 3, fur, u);
        px(c, 4, 15, 3, 2, outline, u); px(c, 11, 15, 3, 2, outline, u);
        if (frame % 4 < 2) { px(c, 13, 13, 3, 2, outline, u); px(c, 14, 12, 2, 2, dark, u); }
        else { px(c, 13, 14, 4, 2, outline, u); }
        if (sleeping) {
            text(c, "z", 14.5f*u, 5*u, dp(18), Color.rgb(112, 101, 151));
            text(c, "Z", 16*u, 3*u, dp(22), Color.rgb(112, 101, 151));
        }
        c.restore();
    }

    private void drawShadow(Canvas c, float u) {
        paint.setColor(0x33000000);
        c.drawOval(new RectF(4*u, getHeight()-2.2f*u, 15*u, getHeight()-.8f*u), paint);
    }

    private void drawMenu(Canvas c) {
        paint.setColor(0xF7FFF8EF);
        c.drawRoundRect(new RectF(dp(4), dp(2), getWidth()-dp(4), dp(51)), dp(12), dp(12), paint);
        text(c, "🍗", getWidth()*.18f, dp(32), dp(21), Color.DKGRAY);
        text(c, "★", getWidth()*.49f, dp(32), dp(21), Color.rgb(233,134,160));
        text(c, "Zz", getWidth()*.77f, dp(31), dp(16), Color.rgb(112,101,151));
        text(c, "Lv."+state.level+"  ◉"+state.coins, dp(8), dp(69), dp(11), Color.rgb(70,58,68));
        bar(c, dp(7), dp(74), state.hunger, Color.rgb(244,167,185));
        bar(c, dp(7), dp(81), state.happiness, Color.rgb(248,195,108));
        bar(c, dp(7), dp(88), state.energy, Color.rgb(132,202,190));
    }

    private void drawHint(Canvas c) {
        if (frame % 8 < 4) text(c, "tap me", getWidth()/2f, dp(16), dp(11), 0xCC463A44);
    }

    private void bar(Canvas c, float x, float y, int value, int color) {
        float width = getWidth()-dp(14);
        paint.setColor(0x33463A44); c.drawRoundRect(x,y,x+width,y+dp(4),dp(2),dp(2),paint);
        paint.setColor(color); c.drawRoundRect(x,y,x+width*value/100f,y+dp(4),dp(2),dp(2),paint);
    }

    private void px(Canvas c, int x, int y, int w, int h, int color, float u) {
        paint.setColor(color); paint.setAntiAlias(false);
        c.drawRect(x*u, y*u, (x+w)*u, (y+h)*u, paint);
        paint.setAntiAlias(true);
    }

    private void text(Canvas c, String s, float x, float y, float size, int color) {
        paint.setColor(color); paint.setTextSize(size); paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD); c.drawText(s,x,y,paint);
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX=e.getX(); downY=e.getY(); lastRawX=e.getRawX(); lastRawY=e.getRawY();
                downAt=SystemClock.uptimeMillis(); moved=false; return true;
            case MotionEvent.ACTION_MOVE:
                float dx=e.getRawX()-lastRawX, dy=e.getRawY()-lastRawY;
                if (Math.abs(e.getX()-downX)>dp(5) || Math.abs(e.getY()-downY)>dp(5)) moved=true;
                if (moved) dragListener.moveBy(dx,dy);
                lastRawX=e.getRawX(); lastRawY=e.getRawY(); return true;
            case MotionEvent.ACTION_UP:
                if (!moved) handleTap(e.getX(),e.getY(),SystemClock.uptimeMillis()-downAt);
                return true;
            default: return true;
        }
    }

    private void handleTap(float x, float y, long duration) {
        if (duration > 650) { sleeping=!sleeping; if (sleeping) state.sleep(); invalidate(); return; }
        if (menuOpen && y < dp(55)) {
            if (x < getWidth()/3f) { state.feed(); sleeping=false; }
            else if (x < getWidth()*2/3f) { state.play(); sleeping=false; }
            else { sleeping=!sleeping; if (sleeping) state.sleep(); }
        } else if (menuOpen) { state.pet(); }
        else { state.pet(); menuOpen=true; }
        invalidate();
    }

    private float dp(float v) { return v*getResources().getDisplayMetrics().density; }

    @Override protected void onDetachedFromWindow() { removeCallbacks(animator); super.onDetachedFromWindow(); }
}

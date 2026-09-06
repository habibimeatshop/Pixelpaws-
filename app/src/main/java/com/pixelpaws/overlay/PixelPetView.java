package com.pixelpaws.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import java.util.Random;

final class PixelPetView extends View {
    interface MotionListener { void moveBy(float dx, float dy, boolean userDrag); }
    private enum Action { IDLE, WALK, RUN, BALL, HUNGRY, AFFECTION, SLEEP }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final PetState state;
    private final MotionListener motionListener;
    private final Random random = new Random();
    private float downX, downY, lastRawX, lastRawY;
    private long downAt, actionUntil;
    private boolean moved, menuOpen, facingRight = true;
    private int frame;
    private Action action = Action.IDLE;

    PixelPetView(Context context, PetState state, MotionListener listener) {
        super(context); this.state=state; this.motionListener=listener;
        setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        actionUntil=SystemClock.uptimeMillis()+2200; post(animator);
    }

    private final Runnable animator = new Runnable() {
        @Override public void run() {
            frame++; long now=SystemClock.uptimeMillis();
            if(now>=actionUntil) chooseNextAction(now);
            if(!moved && !menuOpen) {
                float speed=action==Action.RUN?dp(3.5f):(action==Action.WALK||action==Action.BALL?dp(1.5f):0f);
                if(speed>0) motionListener.moveBy(facingRight?speed:-speed,0,false);
            }
            invalidate(); postDelayed(this,90);
        }
    };

    private void chooseNextAction(long now) {
        if(state.hunger<=25) action=Action.HUNGRY;
        else if(state.happiness<=28) action=Action.AFFECTION;
        else if(state.energy<=18) action=Action.SLEEP;
        else {
            int pick=random.nextInt(100);
            if(pick<25) action=Action.IDLE; else if(pick<55) action=Action.WALK;
            else if(pick<72) action=Action.RUN; else if(pick<90) action=Action.BALL; else action=Action.AFFECTION;
            if(random.nextInt(4)==0) facingRight=!facingRight;
        }
        int duration=action==Action.RUN?1800:(action==Action.BALL?4200:3000);
        actionUntil=now+duration+random.nextInt(1700);
    }

    void turnAround(){ facingRight=!facingRight; }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c); if(menuOpen) drawMenu(c); drawBubble(c); drawGroundShadow(c); drawCat(c);
        if(action==Action.BALL) drawBall(c);
    }

    private void drawCat(Canvas c) {
        float u=dp(5.1f), baseX=getWidth()/2f, baseY=getHeight()-dp(17);
        float bob=(action==Action.WALK||action==Action.RUN||action==Action.BALL)&&((frame/2)%2==0)?-dp(3):0;
        if(action==Action.SLEEP) bob=dp(5);
        c.save(); c.translate(baseX,baseY+bob); c.scale(facingRight?1:-1,1); c.translate(-9*u,-15*u);
        int outline=Color.rgb(55,31,24), dark=Color.rgb(91,55,40), fur=Color.rgb(133,89,62);
        int warm=Color.rgb(174,126,87), light=Color.rgb(226,186,139), cream=Color.rgb(244,218,174);
        int pink=Color.rgb(211,111,104), eye=Color.rgb(226,196,31);
        px(c,12,8,4,6,outline,u); px(c,14,7,3,6,dark,u); px(c,15,9,3,4,fur,u);
        px(c,4,9,11,6,outline,u); px(c,5,9,9,5,fur,u);
        px(c,5,13,4,3,outline,u); px(c,10,13,4,3,outline,u); px(c,6,13,2,2,warm,u); px(c,11,13,2,2,warm,u);
        px(c,2,3,4,4,outline,u); px(c,12,3,4,4,outline,u); px(c,3,2,3,4,fur,u); px(c,12,2,3,4,fur,u);
        px(c,4,3,1,2,pink,u); px(c,13,3,1,2,pink,u);
        px(c,3,5,12,8,outline,u); px(c,4,5,10,7,fur,u); px(c,2,7,3,4,warm,u); px(c,13,7,3,4,warm,u);
        px(c,4,10,10,3,cream,u); px(c,6,9,6,3,light,u);
        px(c,7,5,1,2,dark,u); px(c,9,5,1,2,dark,u); px(c,11,5,1,2,dark,u); px(c,4,7,2,1,dark,u); px(c,12,7,2,1,dark,u);
        if(action==Action.SLEEP){ px(c,5,8,3,1,outline,u); px(c,10,8,3,1,outline,u); }
        else {
            px(c,5,7,3,3,eye,u); px(c,10,7,3,3,eye,u); px(c,6,7,2,3,outline,u); px(c,10,7,2,3,outline,u);
            px(c,6,7,1,1,Color.WHITE,u); px(c,10,7,1,1,Color.WHITE,u);
        }
        px(c,8,9,2,1,pink,u); px(c,8,10,1,1,outline,u); px(c,9,10,1,1,outline,u);
        if(action==Action.RUN){ px(c,3,14,4,1,outline,u); px(c,11,14,4,1,outline,u); }
        c.restore();
    }

    private void drawBubble(Canvas c) {
        String message=null;
        if(action==Action.HUNGRY) message="Lapar... 🍗"; else if(action==Action.AFFECTION) message="Elus aku ♡";
        else if(action==Action.SLEEP) message="Zz..."; else if(action==Action.BALL) message="Main bola!";
        if(message==null||menuOpen) return;
        paint.setColor(0xF7FFF8EF); c.drawRoundRect(new RectF(dp(23),dp(10),getWidth()-dp(23),dp(46)),dp(14),dp(14),paint);
        text(c,message,getWidth()/2f,dp(34),dp(14),Color.rgb(70,58,68));
    }

    private void drawBall(Canvas c) {
        float x=facingRight?getWidth()-dp(25):dp(25), bounce=Math.abs((frame%12)-6)*dp(1.5f);
        paint.setColor(Color.rgb(244,128,150)); c.drawCircle(x,getHeight()-dp(16)-bounce,dp(10),paint);
        paint.setColor(Color.rgb(255,214,104)); c.drawCircle(x-dp(3),getHeight()-dp(19)-bounce,dp(3),paint);
    }

    private void drawGroundShadow(Canvas c) {
        paint.setColor(0x32000000); c.drawOval(new RectF(getWidth()/2f-dp(47),getHeight()-dp(17),getWidth()/2f+dp(53),getHeight()-dp(7)),paint);
    }

    private void drawMenu(Canvas c) {
        paint.setColor(0xF7FFF8EF); c.drawRoundRect(new RectF(dp(7),dp(3),getWidth()-dp(7),dp(91)),dp(14),dp(14),paint);
        text(c,"🍗",getWidth()*.20f,dp(31),dp(21),Color.DKGRAY); text(c,"●",getWidth()*.50f,dp(31),dp(23),Color.rgb(244,128,150));
        text(c,"♡",getWidth()*.79f,dp(31),dp(23),Color.rgb(211,111,104));
        text(c,"Lv."+state.level+"   ◉"+state.coins,getWidth()/2f,dp(51),dp(12),Color.rgb(70,58,68));
        bar(c,dp(16),dp(60),state.hunger,Color.rgb(244,167,185)); bar(c,dp(16),dp(69),state.happiness,Color.rgb(248,195,108));
        bar(c,dp(16),dp(78),state.energy,Color.rgb(132,202,190));
    }

    private void bar(Canvas c,float x,float y,int value,int color){
        float width=getWidth()-dp(32); paint.setColor(0x33463A44); c.drawRoundRect(x,y,x+width,y+dp(5),dp(3),dp(3),paint);
        paint.setColor(color); c.drawRoundRect(x,y,x+width*value/100f,y+dp(5),dp(3),dp(3),paint);
    }
    private void px(Canvas c,int x,int y,int w,int h,int color,float u){paint.setColor(color);paint.setAntiAlias(false);c.drawRect(x*u,y*u,(x+w)*u,(y+h)*u,paint);paint.setAntiAlias(true);}
    private void text(Canvas c,String s,float x,float y,float size,int color){paint.setColor(color);paint.setTextSize(size);paint.setTextAlign(Paint.Align.CENTER);paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);c.drawText(s,x,y,paint);}

    @Override public boolean onTouchEvent(MotionEvent e) {
        switch(e.getActionMasked()){
            case MotionEvent.ACTION_DOWN: downX=e.getX();downY=e.getY();lastRawX=e.getRawX();lastRawY=e.getRawY();downAt=SystemClock.uptimeMillis();moved=false;return true;
            case MotionEvent.ACTION_MOVE:
                float dx=e.getRawX()-lastRawX,dy=e.getRawY()-lastRawY;
                if(Math.abs(e.getX()-downX)>dp(5)||Math.abs(e.getY()-downY)>dp(5))moved=true;
                if(moved)motionListener.moveBy(dx,dy,true);lastRawX=e.getRawX();lastRawY=e.getRawY();return true;
            case MotionEvent.ACTION_UP: if(!moved)handleTap(e.getX(),e.getY(),SystemClock.uptimeMillis()-downAt);moved=false;return true;
            default:return true;
        }
    }

    private void handleTap(float x,float y,long duration){
        long now=SystemClock.uptimeMillis();
        if(duration>650){action=Action.SLEEP;state.sleep();actionUntil=now+5000;menuOpen=false;invalidate();return;}
        if(menuOpen&&y<dp(44)){
            if(x<getWidth()/3f){state.feed();action=Action.IDLE;}else if(x<getWidth()*2/3f){state.play();action=Action.BALL;}else{state.pet();action=Action.AFFECTION;}
            actionUntil=now+4200;menuOpen=false;
        }else if(menuOpen)menuOpen=false;
        else{state.pet();action=Action.AFFECTION;actionUntil=now+2600;menuOpen=true;}
        invalidate();
    }
    private float dp(float v){return v*getResources().getDisplayMetrics().density;}
    @Override protected void onDetachedFromWindow(){removeCallbacks(animator);super.onDetachedFromWindow();}
}

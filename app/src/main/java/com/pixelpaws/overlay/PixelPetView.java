package com.pixelpaws.overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    private enum Action { IDLE, WALK, RUN, BALL, HUNGRY, EAT, AFFECTION, PETTED, SLEEP }

    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final PetState state;
    private final MotionListener motionListener;
    private final Random random=new Random();
    private final Bitmap[] sprites=new Bitmap[16];
    private final int[] spriteIds={
            R.drawable.cat_idle_1,R.drawable.cat_idle_2,R.drawable.cat_idle_3,R.drawable.cat_idle_4,
            R.drawable.cat_walk_1,R.drawable.cat_walk_2,R.drawable.cat_run_1,R.drawable.cat_run_2,
            R.drawable.cat_ball_1,R.drawable.cat_ball_2,R.drawable.cat_hungry,R.drawable.cat_eat,
            R.drawable.cat_affection,R.drawable.cat_petted,R.drawable.cat_sleep_1,R.drawable.cat_sleep_2
    };
    private float downX,downY,lastRawX,lastRawY;
    private long downAt,actionUntil;
    private boolean moved,menuOpen,facingRight=true;
    private int frame;
    private Action action=Action.IDLE;

    PixelPetView(Context context,PetState state,MotionListener listener){
        super(context);this.state=state;this.motionListener=listener;
        setLayerType(View.LAYER_TYPE_SOFTWARE,null);paint.setFilterBitmap(false);
        for(int i=0;i<spriteIds.length;i++)sprites[i]=BitmapFactory.decodeResource(getResources(),spriteIds[i]);
        actionUntil=SystemClock.uptimeMillis()+2200;post(animator);
    }

    private final Runnable animator=new Runnable(){
        @Override public void run(){
            frame++;long now=SystemClock.uptimeMillis();
            if(now>=actionUntil)chooseNextAction(now);
            if(!moved&&!menuOpen){
                float speed=action==Action.RUN?dp(3.5f):(action==Action.WALK||action==Action.BALL?dp(1.5f):0f);
                if(speed>0)motionListener.moveBy(facingRight?speed:-speed,0,false);
            }
            invalidate();postDelayed(this,90);
        }
    };

    private void chooseNextAction(long now){
        if(state.hunger<=25)action=Action.HUNGRY;
        else if(state.happiness<=28)action=Action.AFFECTION;
        else if(state.energy<=18)action=Action.SLEEP;
        else{
            int pick=random.nextInt(100);
            if(pick<25)action=Action.IDLE;else if(pick<55)action=Action.WALK;
            else if(pick<72)action=Action.RUN;else if(pick<90)action=Action.BALL;else action=Action.AFFECTION;
            if(random.nextInt(4)==0)facingRight=!facingRight;
        }
        int duration=action==Action.RUN?1800:(action==Action.BALL?4200:3000);
        actionUntil=now+duration+random.nextInt(1700);
    }

    void turnAround(){facingRight=!facingRight;}

    @Override protected void onDraw(Canvas c){
        super.onDraw(c);if(menuOpen)drawMenu(c);drawBubble(c);drawGroundShadow(c);drawCat(c);
    }

    private void drawCat(Canvas c){
        int index;
        switch(action){
            case WALK:index=4+(frame/3)%2;break;
            case RUN:index=6+(frame/2)%2;break;
            case BALL:index=8+(frame/3)%2;break;
            case HUNGRY:index=10;break;
            case EAT:index=11;break;
            case AFFECTION:index=12;break;
            case PETTED:index=13;break;
            case SLEEP:index=14+(frame/7)%2;break;
            default:index=(frame/7)%4;break;
        }
        float size=dp(action==Action.RUN||action==Action.WALK||action==Action.BALL?142:136);
        float left=(getWidth()-size)/2f,top=getHeight()-size-dp(2);
        RectF target=new RectF(left,top,left+size,top+size);
        // The source artwork faces left. Mirror it only when travelling right.
        // drawGroundShadow() uses a translucent Paint; reset it so that alpha
        // never leaks into the character bitmap.
        paint.setAlpha(255);
        paint.setColor(Color.WHITE);
        c.save();if(facingRight)c.scale(-1,1,getWidth()/2f,getHeight()/2f);
        c.drawBitmap(sprites[index],null,target,paint);c.restore();
    }

    private void drawBubble(Canvas c){
        String message=null;
        if(action==Action.HUNGRY)message="Lapar... 🍗";else if(action==Action.AFFECTION)message="Elus aku ♡";
        else if(action==Action.SLEEP)message="Zz...";else if(action==Action.BALL)message="Main bola!";
        if(message==null||menuOpen)return;
        paint.setColor(0xF7FFF8EF);c.drawRoundRect(new RectF(dp(23),dp(10),getWidth()-dp(23),dp(46)),dp(14),dp(14),paint);
        text(c,message,getWidth()/2f,dp(34),dp(14),Color.rgb(70,58,68));
    }

    private void drawGroundShadow(Canvas c){
        paint.setColor(0x28000000);c.drawOval(new RectF(getWidth()/2f-dp(47),getHeight()-dp(13),getWidth()/2f+dp(53),getHeight()-dp(5)),paint);
    }

    private void drawMenu(Canvas c){
        paint.setColor(0xF7FFF8EF);c.drawRoundRect(new RectF(dp(7),dp(3),getWidth()-dp(7),dp(91)),dp(14),dp(14),paint);
        text(c,"🍗",getWidth()*.20f,dp(31),dp(21),Color.DKGRAY);
        text(c,"●",getWidth()*.50f,dp(31),dp(23),Color.rgb(244,128,150));
        text(c,"♡",getWidth()*.79f,dp(31),dp(23),Color.rgb(211,111,104));
        text(c,"Lv."+state.level+"   ◉"+state.coins,getWidth()/2f,dp(51),dp(12),Color.rgb(70,58,68));
        bar(c,dp(16),dp(60),state.hunger,Color.rgb(244,167,185));
        bar(c,dp(16),dp(69),state.happiness,Color.rgb(248,195,108));
        bar(c,dp(16),dp(78),state.energy,Color.rgb(132,202,190));
    }

    private void bar(Canvas c,float x,float y,int value,int color){
        float width=getWidth()-dp(32);paint.setColor(0x33463A44);
        c.drawRoundRect(x,y,x+width,y+dp(5),dp(3),dp(3),paint);
        paint.setColor(color);c.drawRoundRect(x,y,x+width*value/100f,y+dp(5),dp(3),dp(3),paint);
    }
    private void text(Canvas c,String s,float x,float y,float size,int color){
        paint.setColor(color);paint.setTextSize(size);paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);c.drawText(s,x,y,paint);
    }

    @Override public boolean onTouchEvent(MotionEvent e){
        switch(e.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                downX=e.getX();downY=e.getY();lastRawX=e.getRawX();lastRawY=e.getRawY();
                downAt=SystemClock.uptimeMillis();moved=false;return true;
            case MotionEvent.ACTION_MOVE:
                float dx=e.getRawX()-lastRawX,dy=e.getRawY()-lastRawY;
                if(Math.abs(e.getX()-downX)>dp(5)||Math.abs(e.getY()-downY)>dp(5))moved=true;
                if(moved)motionListener.moveBy(dx,dy,true);lastRawX=e.getRawX();lastRawY=e.getRawY();return true;
            case MotionEvent.ACTION_UP:
                if(!moved)handleTap(e.getX(),e.getY(),SystemClock.uptimeMillis()-downAt);moved=false;return true;
            default:return true;
        }
    }

    private void handleTap(float x,float y,long duration){
        long now=SystemClock.uptimeMillis();
        if(duration>650){action=Action.SLEEP;state.sleep();actionUntil=now+5000;menuOpen=false;invalidate();return;}
        if(menuOpen&&y<dp(44)){
            if(x<getWidth()/3f){state.feed();action=Action.EAT;}
            else if(x<getWidth()*2/3f){state.play();action=Action.BALL;}
            else{state.pet();action=Action.PETTED;}
            actionUntil=now+4200;menuOpen=false;
        }else if(menuOpen)menuOpen=false;
        else{state.pet();action=Action.PETTED;actionUntil=now+2600;menuOpen=true;}
        invalidate();
    }

    private float dp(float v){return v*getResources().getDisplayMetrics().density;}
    @Override protected void onDetachedFromWindow(){
        removeCallbacks(animator);for(Bitmap sprite:sprites)if(sprite!=null&&!sprite.isRecycled())sprite.recycle();
        super.onDetachedFromWindow();
    }
}

package com.example.get_through_maze;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
/**
 * 画板视图
 * @author Administrator
 *
 */
public class WritingView  extends View{
    private Canvas mCanvas;
    private Bitmap mBitmap;
    private Paint paint = null;

    public short basic_pix=0; //小球半径，迷宫墙宽度

    public double start_Px=0, start_Py=0; //迷宫起点坐标
    public double end_Px=0, end_Py=0; //迷宫终点坐标
    public double Px=0, Py=0; //小球球心坐标


    public int start_color;
    public int end_color;
    public int ball_color;

    public int mazeMtx[][] = null; // 输入迷宫设计矩阵

    public WritingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inite();
        // TODO Auto-generated constructor stub
    }

    /**
     * 初始化
     */
    private void inite(){
        paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(1);
        mCanvas = new Canvas();
        mCanvas.drawColor(Color.TRANSPARENT,Mode.CLEAR); // 清屏幕

        start_color = Color.RED;
        end_color = Color.GREEN;
        ball_color = Color.BLUE;

    }

    /**
     * 设置颜色
     * @param color 颜色
     */
    public void setcolor(int color){
        if(paint!=null){
            paint.setColor(color);
        }
    }
    /**
     * 设置笔刷大小
     * @param size 笔刷大小值
     */
    public void setPenSize(double size){
        if(paint!=null){
            paint.setStrokeWidth((float)size);
        }
    }
    /**
     * 清屏
     */
    public void clearall(){
        if(mCanvas!=null){
            mCanvas.drawColor(Color.TRANSPARENT,Mode.CLEAR); // 清屏幕
            invalidate();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // TODO Auto-generated method stub
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas.setBitmap(mBitmap);
        super.onSizeChanged(w, h, oldw, oldh);
    }



    protected void drawMazeLine(int [] vec, int pos, int mod){
        //实际显示坐标系是将迷宫矩阵顺时针旋转90度
        //drawLine函数的x是mazeMtx的column
        //drawLine函数的y是mazeMtx的row

        boolean state=false;
        int start=-1;
        int end=-1;
        // 画迷宫竖线部分
        if(mod==0){
            for(int i=0;i<vec.length;i++){
                // 按照检测到的连续1画线
                if(vec[i]==1 && state==false){
                    state=true;
                    start=i;
                }
                if(vec[i]==0 && state==true){
                    state=false;
                    end=i;
                    mCanvas.drawLine((float)basic_pix*(pos+1/2f), (float)basic_pix*(start), (float)basic_pix*(pos+1/2f), (float)basic_pix*((end)), paint);
                }
            }
            if(state==true){
                mCanvas.drawLine((float)basic_pix*(pos+1/2f), (float)basic_pix*(start), (float)basic_pix*(pos+1/2f), (float)basic_pix*((vec.length)), paint);
            }
        }
        // 画迷宫横线部分
        else{
            for(int i=0;i<vec.length;i++){
                // 按照检测到的连续1画线
                if(vec[i]==1 && state==false){
                    state=true;
                    start=i;
                }
                if(vec[i]==0 && state==true){
                    state=false;
                    end=i;
                    mCanvas.drawLine((float)basic_pix*(start), (float)basic_pix*(pos+1/2f), (float)basic_pix*((end)),(float)basic_pix*(pos+1/2f), paint);
                }
            }
            if(state==true){
                mCanvas.drawLine((float)basic_pix*(start), (float)basic_pix*(pos+1/2f), (float)basic_pix*((vec.length)),(float)basic_pix*(pos+1/2f), paint);
            }
        }
    }

    protected void drawMaze(){
        if(mazeMtx==null){
            System.out.println("Draw maze ERROR!");
            return;
        }

        int row = mazeMtx.length;
        int col = mazeMtx[0].length;

        int col_buf[] = new int[row];

        for(int i=0;i<row;i++){
            drawMazeLine(mazeMtx[i],i, 0); // 画迷宫竖线部分
        }
        for(int j=0;j<col;j++){
            for(int i=0;i<row;i++){
                col_buf[i] = mazeMtx[i][j];
            }
            drawMazeLine(col_buf, j,1); // 画迷宫横线部分
        }


    }

    public void draw(){
        //System.out.println("Running draw!");
        clearall();
        setPenSize(1);
        setcolor(start_color);
        mCanvas.drawCircle((float)start_Px, (float)start_Py, basic_pix, paint); //画起点
        setcolor(end_color);
        mCanvas.drawCircle((float)end_Px, (float)end_Py, basic_pix, paint); //画终点
        setcolor(ball_color);
        mCanvas.drawCircle((float)Px, (float)Py, basic_pix, paint); //画小球
        setcolor(Color.BLACK);
        setPenSize(basic_pix);
        drawMaze(); //画迷宫
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        //System.out.println("Running onDraw!");
        super.onDraw(canvas);
        draw();
        canvas.drawBitmap(mBitmap,0 , 0, null);
    }

}

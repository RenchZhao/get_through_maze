package com.example.get_through_maze;


import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.widget.Toast;


public class Game extends AppCompatActivity {

    private WritingView WritingView1;

    private SensorManager sensorManager1;
    private Sensor sensor1;

    private double gravity[] = new double[3];
    private boolean bRegSuccess=false;

    private double vx=0;
    private double vy=0; //小球的速度

    private double H=0;  //绘画图窗宽
    private double W=0;  //绘画图窗高
    private int maze_length=0; //正方形迷宫生成矩阵的边长

    private boolean finish_game=false; //是否成功完成游戏
    private boolean pause_game=false; //是否暂停游戏
    private boolean initialized = false; //图窗是否已经绘制完毕

    private final SensorEventListener listener1 = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub
            if (!initialized) {
                initialized = true;
                initGame();
                WritingView1.postInvalidate();

            }
            else if (!pause_game) {
                gravity[0] = event.values[0];
                gravity[1] = event.values[1];
                gravity[2] = event.values[2];
                Change_Ball_POS(-gravity[0], gravity[1]);
                WritingView1.postInvalidate();
                finish_game = Judge_Win();
                if (finish_game) {
                    pause_game = true;
                    AlertDialog alert = new AlertDialog.Builder(Game.this).create();
                    alert.setTitle("Goal!!");
                    alert.setMessage("Congratulations!! You Win!!");
                    alert.setButton(DialogInterface.BUTTON_POSITIVE, "Good!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });
                    alert.show();
                }
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);

        //绑定资源变量
        WritingView1 = (WritingView)findViewById(R.id.writing);

        Button pause_button = (Button) findViewById(R.id.pause_game);
        Button exit_button = (Button) findViewById(R.id.exit_game);
        Button restart_button = (Button) findViewById(R.id.restart_game);

        //注册加速度传感器
        /* 注意：这行代码必须放onCreate的最后，否则会引发崩溃*/
        /* 因为onCreate执行完成前，画图板没有被创建，initGame执行时会出现越界*/
        sensorManager1 = (SensorManager)getSystemService(SENSOR_SERVICE);
        sensor1 = sensorManager1.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        bRegSuccess = sensorManager1.registerListener(listener1, sensor1, SensorManager.SENSOR_DELAY_GAME);


        pause_button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                pause_game = true;
                AlertDialog alert = new AlertDialog.Builder(Game.this).create();
                alert.setMessage("Paused");
                alert.setCanceledOnTouchOutside(false);
                alert.setButton(DialogInterface.BUTTON_POSITIVE, "Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        pause_game = false;
                    }
                });
                alert.show();

            }

        });

        exit_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                pause_game = true;
                AlertDialog alert = new AlertDialog.Builder(Game.this).create();
                alert.setCanceledOnTouchOutside(false);
                alert.setMessage("Do you really want to quit?");
                alert.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                alert.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        pause_game = false;
                    }
                });
                alert.show();

            }

        });

        restart_button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                pause_game = true;
                AlertDialog alert = new AlertDialog.Builder(Game.this).create();
                alert.setMessage("Do you really want to restart?");
                alert.setCanceledOnTouchOutside(false);
                alert.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        WritingView1.Px = WritingView1.start_Px;
                        WritingView1.Py = WritingView1.start_Py;
                        vx = 0;
                        vy = 0;
                        gravity[0] = 0;
                        gravity[1] = 0;
                        gravity[2] = 0;
                        pause_game = false;
                    }
                });
                alert.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        pause_game = false;
                    }
                });
                alert.show();

            }
        });

    }


    @Override
    public void onBackPressed() {
        pause_game = true;
        AlertDialog alert = new AlertDialog.Builder(Game.this).create();
        alert.setMessage("Do you really want to quit?");
        alert.setCanceledOnTouchOutside(false);
        alert.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        alert.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                pause_game = false;
            }
        });
        alert.show();
    }

    public void designMaze(){
        maze_length = 50;
        WritingView1.mazeMtx = new int[maze_length][maze_length];
        //墙壁
        for(int i=0;i<maze_length;i++){
            WritingView1.mazeMtx[0][i]=1;
            WritingView1.mazeMtx[maze_length-1][i]=1;
            WritingView1.mazeMtx[i][0]=1;
            WritingView1.mazeMtx[i][maze_length-1]=1;
        }

        //设计墙内迷宫
        for(int i=1;i<=(int)maze_length/25;i++)
            for(int j=1;j<=maze_length-(int)maze_length/5;j=j+(int)maze_length/5){
            WritingView1.mazeMtx[maze_length/10-i][j+maze_length/10]=1;
            WritingView1.mazeMtx[i][j]=1;
        }
        for(int i=1;i<=maze_length-(int)maze_length/5;i++)
            for(int j=5;j<=maze_length-maze_length/10-1;j=j+maze_length/5){
            WritingView1.mazeMtx[j][i]=1;
            WritingView1.mazeMtx[j+maze_length/10][maze_length-i]=1;
        }

        //WritingView1.mazeMtx[7][3]=1;


    }

    public void initGame(){
        H = 1500;
        W = 1200;
        //不使用getWidth或getHeight的原因是太容易崩溃了


        //设计迷宫
        designMaze();

        WritingView1.basic_pix = 20;
        WritingView1.start_Px = 47*WritingView1.basic_pix;
        WritingView1.start_Py = 47*WritingView1.basic_pix;
        WritingView1.end_Px = 3*WritingView1.basic_pix;
        WritingView1.end_Py = 3*WritingView1.basic_pix;
        WritingView1.Px = WritingView1.start_Px;
        WritingView1.Py = WritingView1.start_Py;  // 小球开始在起始点
    }


    public boolean Judge_Win(){
        double distance_to_fin = Math.sqrt(Math.pow(WritingView1.Px-WritingView1.end_Px,2)+Math.pow(WritingView1.Py-WritingView1.end_Py,2));

        if (distance_to_fin<WritingView1.basic_pix){

            return true;
        }
        return false;

    }

    public int Crash_Result(int px, int py){
        //判断小球位置是否贴墙函数
        //实现方法，将小球位置定位到mazeMtx中，判断4邻域是否有墙壁
        //这么做的原因是mazeMtx的每个元素代表basic_pix*basic_pix的像素方块
        //而小球半径正好是basic_pix，因此假如4邻域有墙，必会相撞

        //返回0则表示没有相撞
        //返回1表示沿x轴（水平）撞墙
        //返回2表示沿y轴（竖直）撞墙
        //返回3表示既沿x轴撞也沿y轴撞（撞到墙凹进去或凸出来的角）


        int small_x = (int)px/WritingView1.basic_pix;
        int small_y = (int)py/WritingView1.basic_pix;

        int start_x = small_x - 1;
        int end_x = small_x + 1;
        int start_y = small_y - 1;
        int end_y = small_y + 1;

        boolean x_crash=false;
        boolean y_crash=false;

        /*
        if(small_x<0){
            small_x = 0;
        }
        else if(small_x>=maze_length){
            small_x = maze_length-1;
        }

        if(small_y<0){
            small_y = 0;
        }
        else if(small_y>=maze_length){
            small_y = maze_length-1;
        }

        */

        //判断边界

        if(end_x>=maze_length){
            end_x = maze_length-1;
            x_crash=true;
        }
        if(start_x<0){
            start_x = 0;
            x_crash=true;
        }

        if(end_y>=maze_length){
            end_y = maze_length-1;
            y_crash=true;
        }
        if(start_y<0){
            start_y = 0;
            y_crash=true;
        }

        if(WritingView1.mazeMtx[small_x][small_y]!=0){ //之前的4邻域都没有出现墙壁，现在中间突然是墙壁
            x_crash = true;  //说明撞到突出墙角
            y_crash = true;
        }
        else {
            if(WritingView1.mazeMtx[start_x][small_y]!=0 || WritingView1.mazeMtx[end_x][small_y]!=0){
                if(vx!=0){
                    if(WritingView1.mazeMtx[start_x][small_y]!=0 && WritingView1.mazeMtx[end_x][small_y]!=0){
                        y_crash = true; // 左右邻域都有墙壁，过不去，竖直方向反弹
                    }
                    else if(vx<0 && WritingView1.mazeMtx[start_x][small_y]!=0){ // 确实是水平方向撞上了
                        x_crash = true;
                    }
                    else if(vx>0 && WritingView1.mazeMtx[end_x][small_y]!=0){ // 确实是水平方向撞上了
                        x_crash = true;
                    }
                }

                else{ // 水平方向没有速度，左右方还会出现墙壁，说明是撞到了凸出墙角，竖直方向反弹
                    y_crash = true;
                }
            }

            if(WritingView1.mazeMtx[small_x][start_y]!=0 || WritingView1.mazeMtx[small_x][end_y]!=0){
                if (vy!=0){
                    if(WritingView1.mazeMtx[small_x][start_y]!=0 && WritingView1.mazeMtx[small_x][end_y]!=0){
                        x_crash = true; // 上下邻域都有墙壁，过不去，水平方向反弹
                    }
                    else if(vy<0 && WritingView1.mazeMtx[small_x][start_y]!=0){ // 确实是竖直方向撞上了
                        y_crash = true;
                    }
                    else if(vy>0 && WritingView1.mazeMtx[small_x][end_y]!=0){ // 确实是竖直方向撞上了
                        y_crash = true;
                    }
                }
                else{ // 竖直方向没有速度，上方还会出现墙壁，说明是撞到了凸出墙角，水平方向反弹
                    x_crash = true;
                }
            }

        }

        int result=0;
        if(x_crash){
            result = result + 1;
        }
        if(y_crash){
            result = result + 2;
        }
        return result;

    }

    public void Change_Ball_POS(double ax,double ay){
        double alpha = 0.2; // 加速度转换效率
        double gamma = 0.8; // 碰撞能量损失系数
        double u = 0.01; //摩擦力产生的加速度
        double Nx,Ny; //小球自由运动到达的位置
        double dv_x,dv_y; //小球受到手机倾斜产生加速度转换的瞬时速度增量
        double Px,Py; //小球上一时刻速度

        double V=0 ,V_last=0;

        boolean x_Coll=false;
        boolean y_Coll=false; //是否碰撞的结果变量

        int crash_result = 0; //调用碰撞结果判断函数的返回结果接收器


        Px = WritingView1.Px;  //上一时刻的x轴坐标
        Py = WritingView1.Py;  //上一时刻的y轴坐标

        //小球自由运动的到达点
        Nx = Px + vx;
        Ny = Py + vy;



        //判断撞边界
        if(Nx>W){
            Nx = W;
            x_Coll = true;
        }
        else if(Nx<0){
            Nx = 0;
            x_Coll = true;
        }

        if(Ny>H){
            Ny = H;
            y_Coll = true;
        }
        else if(Ny<0){
            Ny = 0;
            y_Coll = true;
        }

        // 判断撞墙

        //判断x轴和y轴运动方向
        int step_x=0;
        int step_y=0;

        int dx = (int)Nx - (int)Px;
        int dy = (int)Ny - (int)Py;
        if(dx>0){
            step_x = 1;
        }
        else if(dx<0){
            step_x = -1;
        }

        if(dy>0){
            step_y = 1;
        }
        else if(dy<0){
            step_y = -1;
        }

        int i=((int)Px) + step_x;
        int j=((int)Py) + step_y;  // 不考虑起始点的碰撞情况
        int last_i = (int)Px;
        int last_j = (int)Py; //当前判断点运动轨迹的前一点
        for(;(i!=(int)Nx) || (j!=(int)Ny);){  //从上一时刻到本时刻的小球的路线逐点搜索是否碰撞
            crash_result=Crash_Result(i,j);
            if (crash_result!=0) {   //在自由运动还没有到终点前就撞墙
                Nx = (double)(last_i); // 在刚好碰撞的前一点像素停下
                Ny = (double)(last_j); // 在刚好碰撞的前一点像素停下  /*注意！！这非常重要，否则会导致小球一直“钉”在原地不动，因为一直判断是原地有墙*/
                if(crash_result==1){
                    x_Coll = true;
                }
                else if(crash_result==2){
                    y_Coll = true;
                }
                else {
                    x_Coll = true;
                    y_Coll = true;
                }

                break;
            }

            if(i!=(int)Nx){  //x轴方向没有到最终点，继续搜索判断
                last_i = i;
                i = i+step_x;
            }
            else{
                last_i = i;
            }
            if(j!=(int)Ny){  //y轴方向没有到最终点，继续搜索判断
                last_j = j;
                j = j+step_y;
            }
            else {
                last_j = j;
            }
        }

        if(crash_result==0){  //最终点的碰撞判断
            crash_result=Crash_Result((int)Nx,(int)Ny);
            if (crash_result!=0) {
                Nx = (double)(last_i); // 在刚好碰撞的前一点像素停下
                Ny = (double)(last_j); // 在刚好碰撞的前一点像素停下  /*注意！！这非常重要，否则会导致小球一直“钉”在原地不动，因为一直判断是原地有墙*/
                if (crash_result == 1) {
                    x_Coll = true;
                }
                else if (crash_result == 2) {
                    y_Coll = true;
                }
                else {
                    x_Coll = true;
                    y_Coll = true;
                }
            }
        }

        //计算本时刻小球的位置
        if(Crash_Result((int)Nx,(int)Ny)==0){ //只有确保不碰撞才赋值
            WritingView1.Px = Nx;
            WritingView1.Py = Ny;
        }


        //设置下一时刻速度
        if(x_Coll || y_Coll){ //有碰撞，除了摩擦和倾斜加速度，还有对应碰撞的轴速度反向并给予一定减小的处理
            // 很明显假如存在碰撞，上一时刻速度总量一定不为0
            // 而且运动时间不再是定义的单位1，所以要计算
            double tx=0,ty=0,t=0;
            if (vx!=0){
                tx = (WritingView1.Px - Px)/vx;
            }
            if (vy!=0){
                ty = (WritingView1.Py - Py)/vy;
            }
            t = Math.max(tx,ty);

            V_last = Math.sqrt(Math.pow(vx, 2) + Math.pow(vy, 2));

            //摩擦力影响
            if (V_last > 0) {
                V = V_last - u*t;
                if (V < 0) {
                    V = 0;
                }
                vx = V * (vx) / V_last;
                vy = V * (vy) / V_last;
            }

            //倾斜手机产生加速度作用的影响
            dv_x = ax * alpha * tx;
            dv_y = ay * alpha * ty;

            vx = vx + dv_x;
            vy = vy + dv_y;

            if(x_Coll){
                vx = -vx * (1-gamma);
            }
            if(y_Coll) {
                vy = -vy * (1-gamma);
            }
        }
        else { //没有碰撞，根据摩擦和x与y轴加速度算出这一时刻速度
            V_last = Math.sqrt(Math.pow(vx, 2) + Math.pow(vy, 2));

            //摩擦力影响
            if (V_last > 0) {
                V = V_last - u;
                if (V < 0) {
                    V = 0;
                }
                vx = V * (vx) / V_last;
                vy = V * (vy) / V_last;
            }

            //倾斜手机产生加速度作用的影响
            dv_x = ax * alpha;
            dv_y = ay * alpha;
            vx = vx + dv_x;
            vy = vy + dv_y;
        }


    }

}
package de.spas.freeworkout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by roland on 24.04.2017.
 */

public class RunClockView extends View{
    private Paint paint = new Paint();
    int top=getTopList(R.id.practice_1);

    public RunClockView(Context context) {
        super(context);
        paint.setAntiAlias(true);
    }
    @Override
    protected void onDraw(Canvas canvas) {

        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(3);

        Toast.makeText(getContext(), "getTop: "+String.valueOf(top), Toast.LENGTH_LONG).show();

        canvas.drawRect(30, 30, 800, 800, paint);
    }

    private int getTopList(int id) {
        //TextView tv = (TextView) findViewById(id);
        //int s= tv.getTop();
        int s=1234;
        return s;
    }
}

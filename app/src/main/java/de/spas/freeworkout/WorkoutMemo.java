package de.spas.freeworkout;



import java.text.SimpleDateFormat;
import java.util.Date;

import tools.BaseGameActivity;

public class WorkoutMemo extends BaseGameActivity {

    private long id;
    private int wore;
    private int number;
    private String name;
    private int type;
    private int quantity;
    private long startTime;
    private long endTime;
    private long duration;
    private String exTimes;
    private boolean star;
    private boolean checked;
    private boolean upload;
    private String[] Types = {"Endurance","Standard","Strength",""};


    public WorkoutMemo(int wore, int number, String name, int type, long id, boolean checked, int quantity, long startTime, long endTime, long duration, String exTimes, boolean star, boolean upload) {
        this.wore = wore;
        this.number = number;
        this.name = name;
        this.type = type;
        this.id = id;
        this.checked = checked;
        this.quantity = quantity;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.exTimes = exTimes;
        this.star = star;
        this.upload = upload;
    }

    public int getWore() {
        return wore;
    }
    public void setWore(int wore) {
        this.wore = wore;
    }

    public int getNumber() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public boolean isChecked() {
        return checked;
    }
    public void setChecked (boolean checked) {
        this.checked = checked;
    }

    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public long getStartTime() {
        return startTime;
    }
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getDuration() { return duration; }
    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getExTimes() {
        return exTimes;
    }
    public void setExTimes(String exTimes) {
        this.exTimes = exTimes;
    }

    public boolean getStar() {
        return star;
    }
    public void setStar (boolean star) {
        this.star = star;
    }

    public boolean isUpload() {
        return upload;
    }
    public void setUpload (boolean upload) { this.upload = upload; }


    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        long ts = startTime;
        Date resultdate = new Date(ts);
        String formattedTime=sdf.format(resultdate);
        String xmeter = " x ";
        String xhalf = "";
        int q = quantity;
        if (name.equals("Sprint")) xmeter = " m ";
        if (name.equals("Run")) xmeter = " m ";
        if (name.equals("Lunge Walk"))  xmeter = " m ";
        if (name.equals("HH Lunge Walk"))  xmeter = " m ";
        if (name.equals("Sprawl Frogs"))  xmeter = " m ";
        if (name.equals("Burpee Frogs"))  xmeter = " m ";
        if (name.equals("Burpee Deepfrogs"))  xmeter = " m ";
        if (xmeter.equals(" m ") && q < 100) {
            q = q / 2;
            xhalf = "2x ";
        }


        String output = id + ". " + formattedTime + " " + xhalf+q+xmeter+name + " " + Types[type]+ " " + timeFormat((int)(duration/1000));

        return output;
    }

}
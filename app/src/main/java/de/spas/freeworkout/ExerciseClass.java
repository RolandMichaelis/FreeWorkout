package de.spas.freeworkout;

class ExerciseClass
{
    public String name;
    public int number;
    public String textIntent;

    public ExerciseClass(String name, int number, String textIntent) {
        this.name = name;
        this.number = number;
        this.textIntent = textIntent;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getNumber() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
    }

    public String getTextIntent() {
        return textIntent;
    }
    public void setTextIntent(String textIntent) {
        this.name = textIntent;
    }

}

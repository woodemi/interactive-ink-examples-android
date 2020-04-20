package com.myscript.iink.getstarted;

import android.support.annotation.NonNull;

public class NotePointer {

    double x;
    double y;
    double t;
    double p;

    NotePointer(double x, double y, double t, double p) {
        this.x = x;
        this.y = y;
        this.t = t;
        this.p = p;
    }

    @NonNull
    @Override
    public String toString() {
        return "x: " + this.x + ", y: " + this.y + ", t: " + this.t + ", p: " + this.p;
    }
}

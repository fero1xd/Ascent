package me.fero.ascent.utils;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

public class Waiter {
    public static Waiter instance;
    public EventWaiter waiter;


    public Waiter() {
        this.waiter = new EventWaiter();
    }

    public static Waiter getInstance(){
        if(instance == null) {
            instance = new Waiter();
        }
        return instance;
    }
}

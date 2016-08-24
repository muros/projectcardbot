package com.comtrade.gcb;

/**
 * Created by muros on 4.8.2016.
 */
public enum Gender {
    MALE("male"), FEMALE("female");

    private String gender;

    Gender(String gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return gender;
    }
}

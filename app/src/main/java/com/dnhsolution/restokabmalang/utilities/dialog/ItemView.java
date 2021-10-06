package com.dnhsolution.restokabmalang.utilities.dialog;

public class ItemView {

    // the resource ID for the imageView
    private String mNumberInDigit;

    // TextView 1
    private String mInText;

    // create constructor to set the values for all the parameters of the each single view
    public ItemView(String NumbersInDigit, String InText) {
        mNumberInDigit = NumbersInDigit;
        mInText = InText;
    }

    // getter method for returning the ID of the imageview
    public String getNumberInDigit() {
        return mNumberInDigit;
    }

    // getter method for returning the ID of the TextView 2
    public String getInText() {
        return mInText;
    }
}

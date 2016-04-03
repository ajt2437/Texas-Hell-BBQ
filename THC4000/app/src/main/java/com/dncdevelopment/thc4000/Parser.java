package com.dncdevelopment.thc4000;

/**
 * Created by AbelardoJose on 3/30/2016.
 */
public class Parser {

    public static final String HOT_TEMPERATURE = "Hey the Smoker is going way to high!!";
    public static final String LOW_TEMPERATURE = "Hey the Smoker is going way to high!!";
    public static final String FOOD_READY = "Hey the Smoker is going way to high!!";

    public static final String startTimeTag = "Time";
    public static final String startITempTag = "ITemp";
    public static final String startETempTag = "ETemp";
    public static final String startErrorTag = "Error";

    private static TAGS tagFound = TAGS.NOTFOUND;

    public enum TAGS {
        TIME,
        ITEMP,
        ETEMP,
        ERROR,
        NOTFOUND
    }

    public static String stringHandler(String bluetoothInput){
        //need to parse first
        String[] token = new String[4];
        System.out.println(bluetoothInput);
        token = bluetoothInput.split(",");
        double data = 0.0;	//this is not how we will use the result... maybe write dummy variables as
        String message = "";
        if (token.length > 2) {
            switch (token[0]) {
			/*
			 * can change the structure easily in here;
			 */
                case startTimeTag:
                    if (token[2].equals("/" + startTimeTag)) {
                        tagFound = TAGS.TIME;
                        return token[1];
                    } else {
                        return "";
                    }
                case startITempTag:
                    if (token[2].equals("/" + startITempTag)) {
                        tagFound = TAGS.ITEMP;
                        return token[1];
                    } else {
                        return "";
                    }
                case startETempTag:
                    if (token[2].equals("/" + startETempTag)) {
                        tagFound = TAGS.ETEMP;
                        return token[1];
                    } else {
                        return "";
                    }
                case startErrorTag:
                    if (token[2].equals("/" + startErrorTag)) {
                        message = userHandler(token);
                        tagFound = TAGS.ERROR;
                        //handle String message here;
                        return message;
                    } else {
                        return "";
                    }
                default:
                    tagFound = TAGS.NOTFOUND;
                    return "";
            }
        }
        return "";
    }


    /*
	 * function for handling "Error" tags
	 */
    private static String userHandler(String[] token){
        String message; //need to declare constants to print out messages
        switch(token[1]){
            case "TemperatureHigh":
                message = HOT_TEMPERATURE;
                break;
            case "TemperatureLow":
                message = LOW_TEMPERATURE;
                break;
            case "FoodReady":
                message = FOOD_READY;
                break;
            default:
                message = "Invalid Error message sent";
        }
        return message;
    }

    /* getter method for tagFound */
    public static TAGS getTagFound() {
        return tagFound;
    }


}

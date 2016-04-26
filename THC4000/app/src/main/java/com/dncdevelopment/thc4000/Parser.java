package com.dncdevelopment.thc4000;

/**
 * Created by AbelardoJose on 3/30/2016.
 */
public class Parser {

    public static final String HOT_TEMPERATURE = "Overheating";
    public static final String LOW_TEMPERATURE = "Add more fuel to the smoker";
//    public static final String FOOD_READY = "Hey the Smoker is going way to high!!";
    public static final String TOO_HOT_ERROR = "0";
    public static final String TOO_COLD_ERROR = "1";

    public static final String startTimeTag = "A";
    public static final String startITempTag = "I";
    public static final String startETempTag = "E";
    public static final String startSetTag = "S";
    public static final String dumpTag = "Dump";

    public static String[] stringHandler(String bluetoothInput){
        //need to parse first
        String[] token = new String[4];
        String[] result = new String[2];
        result[0] = null;
        result[1] = null;
        System.out.println(bluetoothInput);
        token = bluetoothInput.split(",");
        //double data = 0.0;	//this is not how we will use the result... maybe write dummy variables as
        String message;
        if (token.length == 3) {
            switch (token[0]) {
			/*
			 * can change the structure easily in here;
			 */
                case startTimeTag:
                    if (token[2].equals("/" + startTimeTag)) {
                        result[0] = startTimeTag;
                        result[1] = token[1];
                    }
                    break;
                case startSetTag:
                    if (token[2].equals("/" + startSetTag)) {
                        result[0] = startSetTag;
                        result[1] = token[1];
                    }
                    break;
                case startITempTag:
                    if (token[2].equals("/" + startITempTag)) {
                        result[0] = startITempTag;
                        result[1] = token[1];
                    }
                    break;
                case startETempTag:
                    if (token[2].equals("/" + startETempTag)) {
                        result[0] = startETempTag;
                        result[1] = token[1];
                    }
                    break;
            }
        }
        else if (token.length > 3) {
            result[0] = dumpTag;
            result[1] = "0";
        }

        if (result[0] != null) {
            if (result[1].isEmpty()) {
                result[0] = null;
                result[1] = null;
            }
        }
        return result;
    }

    /*
	 * function for handling "Error" tags
	 */
    private static String userHandler(String[] token){
        String message; //need to declare constants to print out messages
        switch(token[1]){
            case TOO_HOT_ERROR:
                message = HOT_TEMPERATURE;
                break;
            case TOO_COLD_ERROR:
                message = LOW_TEMPERATURE;
                break;
            default:
                message = "Invalid Error message sent";
        }
        return message;
    }

}

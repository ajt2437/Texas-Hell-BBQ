package ServerReader;

public class test1
{
	public static final String HOT_TEMPERATURE = "Hey the Smoker is going way to high!!";
	public static final String LOW_TEMPERATURE = "Hey the Smoker is going way to high!!";
	public static final String FOOD_READY = "Hey the Smoker is going way to high!!";

ITemp,99.562,/ITemp
ITemp,99.562,/ITempITemp,95.4,/ITempITemp,99.0,/ITemp
	
	
	public static void main(String[] args){
		stringHandler("ITemp,99.562,/ITemp");
		stringHandler("ETemp,99.1234,asdf,/ITemp");
		stringHandler("ETemp,913.123,/ETemp");
		stringHandler("Error,TemperatureHigh,/Error");
	}
	public static void stringHandler(String bluetoothInput){
		//need to parse first
		String[] token = new String[4];
		System.out.println(bluetoothInput);
		token = bluetoothInput.split(",");
		double data = 0.0;	//this is not how we will use the result... maybe write dummy variables as 
		String message = "";
		switch(token[0]){
			/*
			 * can change the structure easily in here;
			 */
			case "Time":
				if(token[2].equals("Time")){
					data = Double.parseDouble(token[1]);
					//do something here
					System.out.println(data);
				}
				else{
					System.out.println("Invalid Tag");
				}
				break;
			case "ITemp":
				if(token[2].equals("ITemp")){
					data = Double.parseDouble(token[1]);
					//do something here
					System.out.println(data);
				}
				else{
					System.out.println("Invalid Tag");
				}
				break;
			case "ETemp":
				if(token[2].equals("ETemp")){
					data = Double.parseDouble(token[1]);
					//do something here
					System.out.println(data);
				}
				else{
					System.out.println("Invalid Tag");
				}
				break;
			case "Error":
				if(token[2].equals("Error")){
				message = userHandler(token);
				//handle String message here;
				System.out.println(message);
				}
				else{
					System.out.println("Invalid Tag");
				}
				break;
			default:
				System.out.println("Invalid Tag");
		}
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
}

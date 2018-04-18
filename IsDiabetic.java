
public class IsDiabetic {

	
	public Object run(String val) {
		System.out.println("Inside  IsDiabetic Service");
		return processVal(val);
	}
 
	private String processVal(String insulinVal) {
		// TODO Auto-generated method stub
		
		System.out.println("value is "+insulinVal);
		try 
		{
			if(Integer.parseInt(insulinVal) > 500)
			{
				
				Integer integerVal=Integer.parseInt(insulinVal)/100;
				System.out.println("value returend is  "+integerVal);
				return integerVal.toString() ;
			}
			else
			{
				Integer integerVal=Integer.parseInt(insulinVal)/10;
				System.out.println("value returend is  "+integerVal);
				return integerVal.toString() ;
			}
		}
		catch(Exception e)
		{
			System.out.println("Expected input is Integer");
		}
				
		return "-1";
	}
	
	
}

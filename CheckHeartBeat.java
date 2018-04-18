public class CheckHeartBeat {
	public Object run(String val) {
		return processVal(val);
	}

	private String processVal(String val) {
		
		System.out.println("Inside CheckHeartBeat service  and  data received is "+val);
		if(Integer.parseInt(val) > 100)
		{
			
			Integer integerVal=3*Integer.parseInt(val);
			System.out.println(integerVal.toString() + "is new data ");	
			return integerVal.toString() ;
		} 
		else
		{
			Integer integerVal=5*Integer.parseInt(val);
			System.out.println(integerVal.toString() + "is new data ");
			return integerVal.toString() ;
		}
				
	}
}

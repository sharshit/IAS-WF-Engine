public class BPHighService {
	public Object run(String val) {
		return processVal(val);
	}

	private String processVal(String val) {
		// TODO Auto-generated method stub  
		System.out.println("Inside BPHighService.. value of BP is "+val);
		if(Integer.parseInt(val) > 150)
		{ 
			System.out.println(" returning  TRUE");
			return "TRUE" ;
		}
		System.out.println("returning FALSE");
		return "FALSE";
				
	}
}

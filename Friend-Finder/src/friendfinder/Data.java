package friendfinder;

public enum Data 
{
	NAME("name"), PHONE_NUMBER("phonenum"), EMAIL("email"), GRADE("grade"), USER_ID("userid"),
	PASSWORD("password");
	
	String data;
	
	Data(String a)
	{
		this.data = a;
	}
}

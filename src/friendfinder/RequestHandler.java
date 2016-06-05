package friendfinder;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.*;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RequestHandler extends Thread
{
	private Connection connect = null;
	private Socket client;
	private ObjectInputStream ois;
	private ObjectOutputStream ous;
	private int userid;
	
	public RequestHandler(Socket client) throws SQLException, ClassNotFoundException, IOException
	{
		client.setSoTimeout(60*1000);
		this.userid = client.getInputStream().read();
		Class.forName("com.mysql.jdbc.Driver");
		  // Setup the connection with the DB
		  connect = DriverManager
		      .getConnection("jdbc:mysql://localhost/info", "root","root");
		this.client = client;
	}
	
	@Override
	public void run()
	{
		try 
		{
			//Get the connected clients input/output streams
			ois = new ObjectInputStream(client.getInputStream());
			ous = new ObjectOutputStream(client.getOutputStream());
			/*
			 * Get the request they want.  Can be one of four options
			 * 1) GET - returns list of user objects with name, phone number, email, and grade level
			 * 2) UPDATE - returns true if completed successfully, false if otherwise
			 * 
			 * These items are to be implemented later.
			 * 3) REGISTER - returns true if completed successfully, false if otherwise
			 * 4) CHECKPASS - returns true if authentication successful, false if otherwise
			 */
			String task = (String) ois.readObject();
			
			switch(task)
			{
				//Search option
				case "GET":
						//Acknowledge request
						ous.writeObject(new String("ACK"));
						ous.flush(); //Makes sure there is nothing left in the output stream
						//Set a timeout incase they disconnect or close app
						client.setSoTimeout(60*1000);
						//searchQuery would be the search that they type in on app
						String searchQuery = (String)ois.readObject();
						//Send back a List<User> which contains information about each user in the database
						ous.writeObject(getInfo(searchQuery));
//						These lines were supposed to only return specific parts of the users information based on what was allowed
//						however for simpleness we decided not to go with this just yet.
//						List<String> items = (List<String>) ois.readObject();
//						ous.writeObject(getItems(userid, items));
						ous.flush(); //Makes sure there is nothing left in the output stream
					break;
				//Update Option -- SHOULD ONLY WORK ON USERS OWN USERID
				case "UPDATE":
						ous.writeObject(new String("ACK")); 
						ous.flush();
						client.setSoTimeout(60*1000);
						List<String> items2 = (List<String>) ois.readObject();
						//pseudocode for update method
						// key = items2[0]
						//newvalue = items2[1]
						//write(newvalue, key)
						boolean b = true;
						ous.writeObject(b);
						ous.flush();
					break;
					
				case "REGISTER":
					break;
				case "CHECKPASS":
					break;
				default:
					ous.writeObject(new String("INVALID TASK"));
					ous.flush();
					break;
			}
			
		}
		catch (IOException | ClassNotFoundException | SQLException e)
		{
			e.printStackTrace();
		}
		
	}
	
	public List<String> getItems(int id, List<String> items) throws SQLException
	{
		Statement statement = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		List<String> results = new ArrayList<String>();
		statement = connect.createStatement();
		String query = "Select * from userinfo where userid="+id;
		resultSet = statement.executeQuery(query);
		
        String name = resultSet.getString("name");
        String phonenum = resultSet.getString("phonenum");
        String email = resultSet.getString("email");
        int grade = resultSet.getInt("grade");
        
        for(String s : items)
        {
        	switch(s)
        	{
	        	case "name":
	        		results.add(name);
	        			break;
	        	case "phonenum":
	        		results.add(phonenum);
	        			break;
	        	case "email":
	        		results.add(email);
	        			break;
	        	case "grade":
	        		results.add(""+grade);
	        			break;
	        	default:
	        			break;
        	}
        }
		
		return results;
	}
	
	/**
	 * 
	 * @param partial - The search query that the user types into the app
	 * @return List populated with User objects that contains information about each user
	 * @throws SQLException An error in the database
	 */
	public List<User> getInfo(String partial) throws SQLException {
		List<User> people = new ArrayList<User>();
		Statement statement = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		statement = connect.createStatement();
		String query = "SELECT name,phonenum,email,grade FROM userinfo WHERE name LIKE "+partial;
		resultSet = statement.executeQuery(query);
		
		while(resultSet.next()){
			User user = new User();
			user.setName(resultSet.getString("name"));
			user.setPhonenum(resultSet.getString("phonenum"));
			user.setEmail(resultSet.getString("email"));
			user.setGrade(resultSet.getInt("grade"));
			people.add(user);
		}
		
		return people;
	}
	
	
	public void updateInfo(String newValue, String key){
		
		
		
	}
	
}

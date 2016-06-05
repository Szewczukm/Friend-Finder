package friendfinder;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.*;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RequestHandler extends Thread
{
	private Connection connect = null;
	private Socket client;
	private ObjectInputStream ois;
	private ObjectOutputStream ous;
	private int userid;
	private String delimiter = "[,]";
	
	public RequestHandler(Socket client) throws SQLException, ClassNotFoundException, IOException {
		client.setSoTimeout(60*1000);
		this.userid = client.getInputStream().read();
		Class.forName("com.mysql.jdbc.Driver");
		// Setup the connection with the DB
		connect = DriverManager.getConnection("jdbc:mysql://localhost/info", "root","root");
		this.client = client;
	}
	
	@Override
	public void run()
	{
		try {
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
			
			switch(task) {
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
					
//					These lines were supposed to only return specific parts of the users information based on what was allowed
//					however for simpleness we decided not to go with this just yet.
//					List<String> items = (List<String>) ois.readObject();
//					ous.writeObject(getItems(userid, items));
					
					ous.flush(); //Makes sure there is nothing left in the output stream
					break;
				//Update Option -- SHOULD ONLY WORK ON USERS OWN USERID
				case "UPDATE":
					ous.writeObject(new String("ACK")); 
					ous.flush();
					client.setSoTimeout(60*1000);
					String updates = (String)ois.readObject();
					String[] tokens = updates.split(delimiter);
					String key = tokens[0];
					String newValue = tokens[1];
					boolean b = updateInfo(key, newValue);
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
		catch (IOException | ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param partial - The search query that the user types into the app
	 * @return List populated with User objects that contains information about each user
	 * @throws SQLException An error in the database
	 */
	public List<User> getInfo(String partial) throws SQLException {
		List<User> people = new ArrayList<User>();
		Statement statement = connect.createStatement(); //prepares a MySQL statement to access database
		String query = "SELECT name,phonenum,email,grade FROM userinfo WHERE name LIKE "+partial; //the actual MySQL query 
		ResultSet resultSet = statement.executeQuery(query); //resultSet from the database that executes the query given, returns a huge matrix essentially
		
		while(resultSet.next()) { //loop through the results returned and assign each piece of vital info to a User object
			User user = new User();
			user.setName(resultSet.getString("name"));
			user.setPhonenum(resultSet.getString("phonenum"));
			user.setEmail(resultSet.getString("email"));
			user.setGrade(resultSet.getInt("grade"));
			people.add(user);
		}
		return people;
	}
	
	/**
	 * 
	 * @param key - The name of what is being changed
	 * @param newValue - The new value that it is being changed to
	 * @return True if completed successfully, false if an error has occured
	 */
	public boolean updateInfo(String key, String newValue){
		try {
			Statement statement = connect.createStatement();
			String query = "UPDATE userinfo SET "+key+"='"+newValue+"' WHERE userid="+userid;
			statement.executeQuery(query);
			return true;
		} catch(SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
//	public List<String> getItems(int id, List<String> items) throws SQLException
//	{
//			Statement statement = null;
//			ResultSet resultSet = null;
//			List<String> results = new ArrayList<String>();
//			statement = connect.createStatement();
//			String query = "Select * from userinfo where userid="+id;
//			resultSet = statement.executeQuery(query);
//			
//			String name = resultSet.getString("name");
//			String phonenum = resultSet.getString("phonenum");
//			String email = resultSet.getString("email");
//			int grade = resultSet.getInt("grade");
//			
//			for(String s : items)
//			{
//				switch(s)
//				{
//					case "name":
//						results.add(name);
//						break;
//					case "phonenum":
//						results.add(phonenum);
//						break;
//					case "email":
//						results.add(email);
//						break;
//					case "grade":
//						results.add(""+grade);
//						break;
//					default:
//						break;
//				}
//			}
//			
//			return results;
//	}
//	
//	public void updateInfo(String key, String newValue) throws SQLException{
//	Statement statement = connect.createStatement();
//	String query = "UPDATE userinfo SET "+key+"='"+newValue+"' WHERE userid="+userid;
//	statement.executeQuery(query);
//}
	
}

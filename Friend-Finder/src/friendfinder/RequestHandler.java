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
	private String delimiters = "[,]";
	
	public RequestHandler(Socket client) throws SQLException, ClassNotFoundException, IOException {
		client.setSoTimeout(60*1000);
		this.userid = client.getInputStream().read();
		Class.forName("com.mysql.jdbc.Driver");
		// Setup the connection with the DB
		connect = DriverManager.getConnection("jdbc:mysql://localhost/info", "root","root");
		this.client = client;
	}
	
	@Override
	public void run() {
		try {
			//Get the connected clients input/output streams
			ois = new ObjectInputStream(client.getInputStream());
			ous = new ObjectOutputStream(client.getOutputStream());
			/*
			 * Get the request they want.  Can be one of four options
			 * 1) GET - returns list of user objects with name, phone number, email, and grade level
			 * 2) UPDATE - returns true if completed successfully, false if otherwise
			 * 3) REGISTER - returns true if completed successfully, false if otherwise
			 * 4) CHECKPASS - returns true if authentication successful, false if otherwise
			 */
			String task = (String) ois.readObject();
			
			switch(task) {
				//Search option
				case "GET":
					//Acknowledge request
					ous.writeObject(new String("ACK"));
					ous.flush();
					client.setSoTimeout(60*1000);
					String searchQuery = (String)ois.readObject();
					ous.writeObject(getInfo(searchQuery));
					ous.flush();				
					break;
				case "UPDATE":
					ous.writeObject(new String("ACK")); 
					ous.flush();
					client.setSoTimeout(60*1000);
					String updates = (String)ois.readObject();
					String[] tokens = updates.split(delimiters);
					String key = tokens[0];
					String newValue = tokens[1];
					boolean b = updateInfo(key, newValue);
					ous.writeObject(b);
					ous.flush();
					break;
				case "REGISTER":
					ous.writeObject(new String("ACK"));
					ous.flush();
					client.setSoTimeout(60*1000);
					String userInfo = (String)ois.readObject();
					ous.writeObject(register(userInfo));
					ous.flush();
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
	public boolean updateInfo(String key, String newValue) {
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

	/**
	 * 
	 * @param userInfo - String from app side which should be all the fields in order separated by commas (i.e. name,email,phonenum,grade)
	 * @return true if register was successful, false if register had an error
	 */
	public boolean register(String userInfo) {
		String[] parsedInfo = userInfo.split(delimiters);
		String name = parsedInfo[0];
		String user_email = parsedInfo[1];
		String phonenum = parsedInfo[2];
		String grade = parsedInfo[3];
		if(!checkForDupe(user_email)) {
			try { //insert new info
				Statement statement = connect.createStatement();
				String query = "INSERT INTO userinfo (name, phonenum, email, grade, userid) VALUES ("+name+","+phonenum+","+user_email
						+ ","+grade+","+this.userid+")";
				statement.executeQuery(query);
				System.out.println("New Entry Added.");
				return true;
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @return True if user info is duplicate, false if not duplicate
	 */
	public boolean checkForDupe(String user_email) {
		try {
			Statement statement = connect.createStatement();
			String query = "SELECT email FROM userinfo WHERE userid="+this.userid;
			ResultSet rs = statement.executeQuery(query);
			String email = rs.getString("email");
			if(email.equalsIgnoreCase(user_email)) {
				return true;
			}
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false; //an error occured trying to connect
		}
	}
}

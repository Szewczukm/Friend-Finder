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
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Mark Szewczuk (with help from Chad Horack and Joe Waldinger)
 *
 */
public class RequestHandler extends Thread
{
	private Connection connect = null;
	private Socket client;
	private ObjectInputStream ois;
	private ObjectOutputStream ous;
	private int userid;
	private String delimiters = "[,]";
	private static final Logger log = Logger.getLogger(RequestHandler.class.getName());
	private FileHandler fh;
	
	public RequestHandler(Socket client) throws SQLException, ClassNotFoundException, IOException {
		this.client = client;
		client.setSoTimeout(60*1000);
		this.userid = client.getInputStream().read();
		Class.forName("com.mysql.jdbc.Driver");
		connect = DriverManager.getConnection("jdbc:mysql://localhost/info", "root","root"); // Setup the connection with the DB
		fh = new FileHandler("~/workspace/Hackathon/Friend-Finder/UserLogs/client"+this.userid);
		log.addHandler(fh);
	}
	
	@Override
	public void run() {
		try {
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
				case "GET": 
					log.log(Level.INFO, "Client requesting information.");
					ous.writeObject(new String("ACK"));
					ous.flush();
					client.setSoTimeout(60*1000);
					String searchQuery = (String)ois.readObject();
					ous.writeObject(getInfo(searchQuery));
					ous.flush();
					log.log(Level.INFO, "Sent client information.");
					break;
				case "UPDATE":
					log.log(Level.INFO, "Client requesting to update information.");
					ous.writeObject(new String("ACK")); 
					ous.flush();
					client.setSoTimeout(60*1000);
					String updates = (String)ois.readObject();
					ous.writeObject(updateInfo(updates));
					ous.flush();
					log.log(Level.INFO, "Information updated.");
					break;
				case "REGISTER":
					log.log(Level.INFO, "Client wishes to register an account.");
					ous.writeObject(new String("ACK"));
					ous.flush();
					client.setSoTimeout(60*1000);
					String userInfo = (String)ois.readObject();
					ous.writeObject(register(userInfo));
					ous.flush();
					log.log(Level.INFO, "Registering successful.");
					break;
				case "CHECKPASS":
					break;
				default:
					log.log(Level.WARNING, "Client tried an invalid request.");
					ous.writeObject(new String("INVALID TASK"));
					ous.flush();
					break;
			}	
		}
		catch (IOException | ClassNotFoundException | SQLException e) {
			log.log(Level.SEVERE,"An unexpected error has occured: " , e);
		}
	}
	
	/**
	 * 
	 * @param partial - The search query that the user types into the app
	 * @return List populated with User objects that contains information about each user
	 * @throws SQLException An error in the database
	 */
	public List<User> getInfo(String partial) throws SQLException {
		log.log(Level.INFO, "Preparing information requested");
		List<User> people = new ArrayList<User>();
		log.log(Level.WARNING, "Preparing MySQL statement");
		Statement statement = connect.createStatement(); //prepares a MySQL statement to access database
		String query = "SELECT name,phonenum,email,grade FROM userinfo WHERE name LIKE "+partial; //the actual MySQL query
		log.log(Level.WARNING, "Executing MySQL query");
		ResultSet resultSet = statement.executeQuery(query); //resultSet from the database that executes the query given, returns a huge matrix essentially
		log.log(Level.INFO,"Adding users to List<User>");
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
	 * @param unparsedUpate - the unparsed string 
	 * @return true if successfully updated, false if error occured
	 */
	public Boolean updateInfo(String unparsedUpdate) {
		String[] tokens = unparsedUpdate.split(delimiters);
		String key = tokens[0];
		String newValue = tokens[1];
		log.log(Level.INFO, "Successfully parsed string");
		try {
			Statement statement = connect.createStatement();
			String query = "UPDATE userinfo SET "+key+"='"+newValue+"' WHERE userid="+userid;
			log.log(Level.WARNING, "Preparing MySQL statement");
			statement.executeQuery(query);
			log.log(Level.WARNING, "Executing query to database");
			return true;
		} catch(SQLException e) {
			log.log(Level.SEVERE, "An unexpected error has occured: ", e);
			return false;
		}
	}
	
	/**
	 * 
	 * @param userInfo - String from app side which should be all the fields in order separated by commas (i.e. name,email,phonenum,grade)
	 * @return true if register was successful, false if register had an error
	 */
	public Boolean register(String userInfo) {
		String[] parsedInfo = userInfo.split(delimiters);
		String name = parsedInfo[0];
		String user_email = parsedInfo[1];
		String phonenum = parsedInfo[2];
		String grade = parsedInfo[3];
		log.log(Level.INFO, "Successfully parsed input string");
		if(!checkForDupe(user_email)) {
			try { //insert new info
				log.log(Level.WARNING, "Preparing MySQL statement");
				Statement statement = connect.createStatement();
				String query = "INSERT INTO userinfo (name, phonenum, email, grade, userid) VALUES ("+name+","+phonenum+","+user_email
						+ ","+grade+","+this.userid+")";
				log.log(Level.WARNING, "Executing MySQL query");
				statement.executeQuery(query);
				log.log(Level.INFO, "New entry added to database");
				return true;
			} catch(Exception e) {
				log.log(Level.SEVERE, "An unexpected error has occured: ", e);
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @return True if user info is duplicate, false if not duplicate
	 */
	public Boolean checkForDupe(String user_email) {
		log.log(Level.FINE, "Checking for duplicate users");
		try {
			log.log(Level.WARNING, "Preparing MySQL statement");
			Statement statement = connect.createStatement();
			String query = "SELECT email FROM userinfo WHERE userid="+this.userid;
			log.log(Level.WARNING, "Executing MySQL query");
			ResultSet rs = statement.executeQuery(query);
			String email = rs.getString("email");
			if(email.equalsIgnoreCase(user_email)) {
				return true;
			}
			return false;
		} catch (SQLException e) {
			log.log(Level.SEVERE, "An unexpected error has occured: ", e);
			return false;
		}
	}
	
	/**
	 * @deprecated
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
	
}

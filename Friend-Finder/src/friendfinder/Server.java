package friendfinder;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server extends Thread
{
	private ServerSocket ss;
	private Socket client;
//	private static List<User> users = new ArrayList<User>();
	private static final Logger log = Logger.getLogger(Server.class.getName());
	private FileHandler fh;
	
	@Override
	public void run()
	{
		try {
			fh = new FileHandler("~/workspace/Hackathon/Friend-Finder/Server");
			log.addHandler(fh);
		} catch (SecurityException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try 
		{
			ss = new ServerSocket(40000);
			while(true)
			{
				client = ss.accept();
				log.info("Client connected from: "+client.getInetAddress()+":"+client.getPort());
				new RequestHandler(client);
			}			
		} 
		catch (IOException | ClassNotFoundException | SQLException e) 
		{
			log.log(Level.SEVERE,"An unexpected error has occured", e);
		}
	}
	/*
	public static void getUsers()
	{
		Connection connect = null;
		Statement statement = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
				      // This will load the MySQL driver, each DB has its own driver
		  Class.forName("com.mysql.jdbc.Driver");
		  // Setup the connection with the DB
		  connect = DriverManager
		      .getConnection("jdbc:mysql://localhost/info", "root","root");
		
		  // Statements allow to issue SQL queries to the database
		  statement = connect.createStatement();
		  // Result set get the result of the SQL query
		  resultSet = statement
		      .executeQuery("select * from userinfo");
		  
		  while(resultSet.)
		  
		}
		catch(Exception e)
		{
			
		}
	}
	*/
}

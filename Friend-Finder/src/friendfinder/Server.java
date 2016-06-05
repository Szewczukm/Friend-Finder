package friendfinder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread
{
	ServerSocket ss;
	Socket client;
	static List<User> users = new ArrayList<User>();
	
	@Override
	public void run()
	{
		//users = getUsers();
		try 
		{
			ss = new ServerSocket(40000);
			while(true)
			{
				client = ss.accept();
				new RequestHandler(client);
			}			
		} 
		catch (IOException | ClassNotFoundException | SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
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

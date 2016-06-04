package util;

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

public class ServerThread extends Thread
{
	Connection connect = null;
	Socket client;
	ObjectInputStream ois;
	ObjectOutputStream ous;
	int userid;
	
	public ServerThread(Socket client) throws SQLException, ClassNotFoundException, IOException
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
			ois = new ObjectInputStream(client.getInputStream());
			ous = new ObjectOutputStream(client.getOutputStream());
			String task = (String) ois.readObject();
			switch(task)
			{
				case "GET":
						ous.writeObject(new String("ACK"));
						ous.flush();
						client.setSoTimeout(60*1000);
						List<String> items = (List<String>) ois.readObject();
						ous.writeObject(getItems(userid, items));
						ous.flush();
					break;
				case "UPDATE":
						ous.writeObject(new String("ACK"));
						ous.flush();
						client.setSoTimeout(60*1000);
						List<String> items2 = (List<String>) ois.readObject();
						// key = items2[0]
						//newvalue = items2[1]
						//write(newvalue, key)
						/*
						 * String query = "";
						 * 
						 * 
						 */
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
	
}

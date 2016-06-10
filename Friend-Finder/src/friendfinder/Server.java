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

/**
 * 
 * @author Mark Szewzcuk (with help from Chad Horack)
 *
 */
public class Server extends Thread
{
	private ServerSocket ss;
	private Socket client;
	private static final Logger log = Logger.getLogger(Server.class.getName());
	private FileHandler fh;
	private int port = 3079; //default port
	
	public Server(int port){
		this.port = port;
	}
	
	public Server(){
		
	}
	
	@Override
	public void run()
	{
		try {
			fh = new FileHandler("/home/dankey/workspace/Hackathon/Friend-Finder/Server.log");
			log.addHandler(fh);
		} catch (SecurityException e) {
			log.log(Level.SEVERE, "An unexpected error has occured: ", e);
		} catch (IOException e) {
			log.log(Level.SEVERE, "An unexpected error has occured: ", e);
		}
		try 
		{
			log.log(Level.WARNING, "Opening server socket on port: "+port);
			ss = new ServerSocket(port);
			log.log(Level.INFO, "Successfully opened socket on port: "+port);
			while(true)
			{
				log.info("Waiting for client to connect");
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
}

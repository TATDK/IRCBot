package dk.earthgame.TAT.IRCBot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;

import dk.earthgame.TAT.IRCBot.Configuration.Configuration;

public class IRCBot {
	public static final String version = "0.0.1-SNAPSHOT";
	//System
	Configuration config;
	boolean connected;
	
	//Config
	String server;
	String nick;
	String password;
	boolean identify;
	boolean verbose;
	boolean privateBot;
	List<String> channels = new ArrayList<String>();
	HashMap<String, String> users = new HashMap<String, String>();
	
	//Actions
	List<String> moderate = new ArrayList<String>();
	
	/**
	 * Run IRCBot
	 */
	public IRCBot() {
		//Config
		createConfig();
    	loadConfig();
    	
    	//Run
    	run();
	}
	
	/**
	 * Get user permission
	 * @param username Nick of user
	 * @return a(dmin) / m(oderator) / u(ser) / "" (No permission)
	 */
	public String getPermission(String username) {
		if (users.containsKey(username)) {
			if (users.get(username).equalsIgnoreCase("a") || users.get(username).equalsIgnoreCase("admin")) {
				return "a";
			}
			if (users.get(username).equalsIgnoreCase("m") || users.get(username).equalsIgnoreCase("mod") || users.get(username).equalsIgnoreCase("moderator")) {
				return "m";
			}
			if (users.get(username).equalsIgnoreCase("u") ||users.get(username).equalsIgnoreCase("user")) {
				return "u";
			}
		}
		if (privateBot)
			return "";
		else
			return "u";
	}
	
	/**
	 * Check if the user have permission
	 * @param username Nick of user
	 * @param permissionRequired Required permission
	 * @return true if the user have permission else false
	 */
	public boolean havePermission(String username, String permissionRequired) {
		String permission = getPermission(username);
		if (permissionRequired.equalsIgnoreCase("a") || permissionRequired.equalsIgnoreCase("admin")) {
			if (permission.equalsIgnoreCase("a"))
				return true;
			else
				return false;
		}
		if (permissionRequired.equalsIgnoreCase("m") || permissionRequired.equalsIgnoreCase("mod") || permissionRequired.equalsIgnoreCase("moderator")) {
			if (permission.equalsIgnoreCase("a") || permission.equalsIgnoreCase("m"))
				return true;
			else
				return false;
		}
		if (permissionRequired.equalsIgnoreCase("u") || permissionRequired.equalsIgnoreCase("user")) {
			if (!(permission.equalsIgnoreCase("")))
				return true;
			else
				return false;
		}
		return false;
	}
    
	/**
	 * Run the system
	 */
    void run() {
        Bot bot = new Bot(this);
        
        bot.setVerbose(verbose);
        
        while (!connected) {
	        // Connect to the IRC server.
	        try {
				bot.connect(server);
				connected = true;
			} catch (NickAlreadyInUseException e) {
				if (identify) {
					//Kill ghost
					bot.sendRawLine("/NickServ GHOST " + nick + " " + password);
				} else {
					//Try new nick
					bot.ChangeNick(nick + Math.floor(Math.random()*100));
				}
			} catch (IOException e) {
				e.printStackTrace();
				bot.disconnect();
			} catch (IrcException e) {
				e.printStackTrace();
				bot.disconnect();
			}
        }
        
        if (identify)
        	bot.identify(password);
        
        // Join channels.
        for (Iterator<String> it = channels.iterator(); it.hasNext();) {
        	String channel = it.next();
	        bot.joinChannel("#" + channel);
        }
    }
    
    void createConfig() {
        String name = "config.yml";
        File actual = new File(name);
        if (!actual.exists()) {
            InputStream input = IRCBot.class.getResourceAsStream("/Config/" + name);
            if (input != null) {
                FileOutputStream output = null;

                try {
                    output = new FileOutputStream(actual);
                    byte[] buf = new byte[8192];
                    int length = 0;
                    while ((length = input.read(buf)) > 0) {
                        output.write(buf, 0, length);
                    }
                    
                    //Default config file created!
                } catch (IOException e) {
                    e.printStackTrace();
                    //Error creating config file!
                } finally {
                    try {
                        if (input != null)
                            input.close();
                    } catch (IOException e) {}

                    try {
                        if (output != null)
                            output.close();
                    } catch (IOException e) {}
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
	public void loadConfig() {
    	config = new Configuration(new File("config.yml"));
    	config.load();
    	
    	server     = config.getString("Server", "irc.esper.net");
    	nick       = config.getString("Nick", "IRCBot");
    	identify   = config.getBoolean("LoginInfo.Identify", false);
    	password   = config.getString("LoginInfo.Password", "");
    	verbose    = config.getBoolean("Verbose", true);
    	users      = (HashMap<String, String>)config.getProperty("Users");
    	channels   = (ArrayList<String>)config.getStringList("Channels", null);
    	moderate   = (ArrayList<String>)config.getStringList("Actions.Moderate", null);
    	privateBot = config.getBoolean("Private", false);
    }
}
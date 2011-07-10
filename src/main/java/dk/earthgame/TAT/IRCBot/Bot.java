package dk.earthgame.TAT.IRCBot;

import org.jibble.pircbot.PircBot;

public class Bot extends PircBot {
	private IRCBot main;
	
	public Bot(IRCBot instantiate) {
		main = instantiate;
		setName(main.nick);
	}
	
	public void ChangeNick(String nick) { setName(nick); }
	
	@Override
	public void onMessage(String channel, String sender, String login, String hostname, String message) {
		if (message.equalsIgnoreCase("time")) {
			if (main.havePermission(sender, "u")) {
				String time = new java.util.Date().toString();
				sendMessage(channel, sender + ": The time is now " + time);
			} else {
				sendMessage(channel, sender + ": You don't have permission to this command!");
			}
		}
		if (message.equalsIgnoreCase("!disconnect")) {
			if (main.havePermission(sender, "a")) {
				disconnect();
			} else {
				sendMessage(channel, sender + ": You don't have permission to this command!");
			}
		}
		if (message.equalsIgnoreCase("!leave")) {
			if (main.havePermission(sender, "a")) {
				partChannel(channel);
			} else {
				sendMessage(channel, sender + ": You don't have permission to this command!");
			}
		}
	}
	
	@Override
	public void onVersion(String sourceNick, String sourceLogin, String sourceHostname, String target) {
        this.sendRawLine("NOTICE " + sourceNick + " :\u0001VERSION " + IRCBot.version + "\u0001");
    }
	
	@Override
	public void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
		//On join channel
		if (!changed) {
			if (main.moderate.contains("#" + channel)) {
				log("Setting #" + channel + " mode to +m");
				setMode(channel, "+m");
			}
		}
	}
	
	@Override
	public void onDisconnect() {
		System.exit(0);
	}
}
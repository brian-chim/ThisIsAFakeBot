package com.thisisafakecom.thisisafakebot.commands.etc;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.thisisafakecom.thisisafakebot.App;
import com.thisisafakecom.thisisafakebot.commands.CommandAbstract;
import com.thisisafakecom.thisisafakebot.commands.IncorrectUsageException;
import com.thisisafakecom.thisisafakebot.connections.GenericResponseHandler;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class JokeCommand extends CommandAbstract {

	public JokeCommand() {
		commandHandled = "joke";
	}

	@Override
	public void handle(Message input) throws IncorrectUsageException {
		String[] tokenized = input.getContentRaw().split(" ");
		if (tokenized.length != 1) {
			throw new IncorrectUsageException();
		} else {
			MessageChannel channel = input.getChannel();
		   // make a request to get a new token
	    CloseableHttpClient client = HttpClients.createDefault();
	    try {
	      // https://opentdb.com/api_config.php
	      HttpGet get = new HttpGet("https://icanhazdadjoke.com/");
	      get.addHeader("Accept", "text/plain");
        ResponseHandler<String> responseHandler = new GenericResponseHandler();
	      String resp = client.execute(get, responseHandler);
	      channel.sendMessage(resp).queue();
	    } catch (ClientProtocolException e) {
	      e.printStackTrace();
	    } catch (IOException e) {
	      e.printStackTrace();
	    } finally {
	      try {
	        client.close();
	      } catch (IOException e) {
	        e.printStackTrace();
	        System.err.println("could not close client");
	      }
	    }
		}
	}

	@Override
	public void correctUsage(Message input) {
		MessageChannel channel = input.getChannel();
	    String msg = "Correct Usage: ``" + App.botPrefix + commandHandled + "``";
		channel.sendMessage(msg).queue();
	}

	public String commandDescription() {
		String ret = "Tells you a random joke!\n"
				+ "Usage: " + App.botPrefix + commandHandled;
		return ret;
	}

}

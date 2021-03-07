package com.thisisafakecom.thisisafakebot.commands.points.trivia.handlers;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.thisisafakecom.thisisafakebot.connections.GenericResponseHandler;

public class GuildTriviaManager {

  String token;

  public void createSession() {
    // make a request to get a new token
    CloseableHttpClient client = HttpClients.createDefault();
    try {
      // https://opentdb.com/api_config.php
      HttpGet get = new HttpGet("https://opentdb.com/api_token.php?command=request");
      ResponseHandler<String> responseHandler = new GenericResponseHandler();
      // should do some error checking based on the response code it gets back (from
      // the api)
      String resp = client.execute(get, responseHandler);
      SessionResponse session = new Gson().fromJson(resp, SessionResponse.class);
      token = session.token;
    } catch (ClientProtocolException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        client.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void resetSession() {
    // make a request to reset the token
    CloseableHttpClient client = HttpClients.createDefault();
    if (token == null) {
      return;
    }
    try {
      // https://opentdb.com/api_config.php
      HttpGet get = new HttpGet("https://opentdb.com/api_token.php?command=reset&token=" + token);
      ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
        @Override
        public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
          int status = response.getStatusLine().getStatusCode();
          if (status >= 200 && status < 300) {
            HttpEntity entity = response.getEntity();
            return entity != null ? EntityUtils.toString(entity) : null;
          } else {
            throw new ClientProtocolException("Unexpected response status: " + status);
          }
        }
      };
      // should do some error checking based on the response code it gets back (from
      // the api)
      String resp = client.execute(get, responseHandler);
      SessionResponse session = new Gson().fromJson(resp, SessionResponse.class);
      token = session.token;
    } catch (ClientProtocolException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        client.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  // for now all this does is return 10 questions from the videogames category
  public ArrayList<TriviaQuestion> getVideogameQuestions() {
    // check if a token exists - if it does then try and use it
    if (token == null) {
      createSession();
      return getVideogameQuestions();
    } else {
      // use the token to grab results - if token does not exist then create a new
      // token
      CloseableHttpClient client = HttpClients.createDefault();
      try {
        // 31 is anime, 15 was videogames
        HttpGet get = new HttpGet("https://opentdb.com/api.php?amount=5&category=31&token=" + token);
        ResponseHandler<String> responseHandler = new GenericResponseHandler();
        // should do some error checking based on the response code it gets back (from
        // the api)
        String resp = client.execute(get, responseHandler);
        QuestionCall questions = new Gson().fromJson(resp, QuestionCall.class);
        String respCode = questions.response_code;
        if (respCode == "3") {
          createSession();
          return getVideogameQuestions();
        } else if (respCode == "4") {
          resetSession();
          return getVideogameQuestions();
        } else {
          return questions.results;
        }
      } catch (ClientProtocolException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        try {
          client.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return null;
  }

  class SessionResponse {
    public String response_code;
    public String response_message;
    public String token;
  }

  class QuestionCall {
    public String response_code;
    public ArrayList<TriviaQuestion> results;
  }
}
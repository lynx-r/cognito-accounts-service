package com.workingbit;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class.
 */
public class Main {

  private static final URI BASE_URI = URI.create("http://localhost:3333/");
  public static final String ROOT_HELLO_PATH = "helloworld";
  public static final String ROOT_COUNTER_PATH = "counter";

  public static void main(String[] args) {
    try {
      System.out.println("\"Hello World\" Jersey Example App");

      ResourceConfig resourceConfig = new ResourceConfig().packages("com.workingbit.resource");
      HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, resourceConfig, false);
      Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));

      server.start();

      System.out.println("Application started.\nTry out");
      System.out.println(String.format("%s%s", BASE_URI, ROOT_HELLO_PATH));
      System.out.println(String.format("%s%s%s", BASE_URI, ROOT_COUNTER_PATH, "/request"));
      System.out.println(String.format("%s%s%s", BASE_URI, ROOT_COUNTER_PATH, "/application"));
      System.out.println("Stop the application using CTRL+C");

      Thread.currentThread().join();
    } catch (IOException | InterruptedException ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}

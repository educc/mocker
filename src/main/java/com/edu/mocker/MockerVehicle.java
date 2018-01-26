package com.edu.mocker;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.net.ProxyOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Properties;

public class MockerVehicle extends AbstractVerticle {
  private static String PROPERTIES_PATH = null;
  private int appPort = 9000;

  private static final String[] VALID_METHODS = new String[]{"GET", "POST", "PUT", "DELETE", "PATCH"};

    private static String PATH = "C:\\www";

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
      if( args.length != 1){
        System.out.println("Debe especificar el directorio de archivos web");
        return;
      }
      PATH = args[0];
      Runner.runExample(MockerVehicle.class);
    }

    @Override
    public void start() throws Exception {
        System.out.println("Starting server proxy at port: " + appPort);

        vertx.createHttpServer().requestHandler(req -> {
            System.out.println("uri: " + req.uri());
            System.out.println("method:" + req.method());

            String[] parts = req.uri().split("/");

            Path absPath = Paths.get(PATH);

            for(String item : parts){
              absPath = absPath.resolve(item);
            }

            req.response().setChunked(true);
            req.response().putHeader("Content-Type","application/json");
            req.response().setStatusCode(200);
            if(Files.isRegularFile(absPath)){
              System.out.println("It's regular file");
              try {
                req.response().write(Buffer.buffer(Files.readAllBytes(absPath)));
              } catch (IOException e) {
                req.response().write(e.toString());
              }
            }else{
              System.out.println("It's a folder");
              String file = null;
              for(String validMethod: VALID_METHODS){
                if( validMethod.equalsIgnoreCase(req.method().toString()) ){
                 file = validMethod;
                }
              }

              if( file == null){
                System.out.println("NOT METHOD VALID");
                req.response().write("NOT METHOD VALID");
              }else{
                System.out.println("file = " + file);

                String content = getContentWithIgnoreCase(absPath, file);
                req.response().write(content);
              }
            }
            req.response().end();
        }).listen(appPort);
    }

    private String getContentWithIgnoreCase(Path abspath, String file){
      String result = null;

      //get error file
      result = getContent(abspath, file + ".error.json");
      if(result != null){
        return result;
      }


      //get error file.lowerCase
      result = getContent(abspath, file.toLowerCase() + ".error.json");
      if(result != null){
        return result;
      }

      //get json file
      result = getContent(abspath, file + ".json");
      if(result != null){
        return result;
      }

      //get json file.lowerCase
      result = getContent(abspath, file.toLowerCase() + ".json");
      if(result != null){
        return result;
      }

      return "FILE NOT FOUND";
    }

    private String getContent(Path abspath, String file){
      String result = null;
      Path newpath = abspath.resolve(file);
      System.out.println(newpath.toUri());
      if( Files.exists(newpath) ){
        try {
          result = new String(Files.readAllBytes(newpath));
        } catch (IOException e) {
          System.err.println(e);
          result = null;
        }
      }

      return result;
    }

}

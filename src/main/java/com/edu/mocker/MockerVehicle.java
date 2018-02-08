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
  private static int APP_PORT = 9000;


  private static final String[] VALID_METHODS = new String[]{"GET", "POST", "PUT", "DELETE", "PATCH"};
  private static final String[] FILES_SEARCH = new String[]{
          ".error.json",
          ".json",
          ".error.xml",
          ".xml",
  };

    private static String PATH = "C:\\www";

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
      if( args.length < 2){
        System.out.println(String.format("%s %s", App.NAME, App.VERSION));
        System.out.println("develop by: edu cacho");
        System.out.println("***");
        System.out.println("Faltan parametros");
        System.out.println("java -jar mocker.jar port directory");
        return;
      }

      APP_PORT = Integer.parseInt(args[0]);
      PATH = args[1];
      Runner.runExample(MockerVehicle.class);

}
    @Override
    public void start() throws Exception {
        System.out.println("Starting server proxy at port: " + APP_PORT);

        vertx.createHttpServer().requestHandler(req -> {
            System.out.println("uri: " + req.uri());
            System.out.println("method:" + req.method());

            String[] parts = cleanUri( req.uri()).split("/");

            Path absPath = Paths.get(PATH);

            for(String item : parts){
              absPath = absPath.resolve(item);
            }

            req.response().setChunked(true);
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

                StringRef extUsed = new StringRef();
                String content = getContentWithIgnoreCase(absPath, file, extUsed);

                String contentType = "text/plain";
                if( extUsed.data.length() > 0){
                  if(extUsed.data.indexOf("json") >= 0){
                    contentType = "application/json";
                  }

                  if(extUsed.data.indexOf("xml") >= 0){
                    contentType = "application/xml";
                  }
                }
                req.response().putHeader("Content-Type",contentType);
                req.response().write(content);
              }
            }
            req.response().end();
        }).listen(APP_PORT);
    }

    /**
     * remove query param from uri.
     * @param uri
     * @return
     */
    private String cleanUri(String uri){
      String result = uri;
      int idx = uri.indexOf("?");
      if( idx != -1){
        result = uri.substring(0,idx);
      }
      return result;
    }

    private String getContentWithIgnoreCase(Path abspath, String file, StringRef extUsed){
      String result = null;

      for(String ext: FILES_SEARCH){
        result = getContent(abspath, file + ext);
        if(result != null){
          extUsed.data = ext;
          break;
        }

        //tolowercase
        result = getContent(abspath, file.toLowerCase() + ext);
        if(result != null){
          extUsed.data = ext;
          break;
        }
      }

      if( result == null){
        result = "FILE NOT FOUND";
      }
      return result;
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

    private class StringRef {
      public String data;

      public StringRef(){
        data = "";
      }
    }

}

package com.edu.mocker;

import com.edu.mocker.contentwriter.ContentWriteFactory;
import com.edu.mocker.contentwriter.ContentWriter;
import com.edu.mocker.utils.ContentFile;
import com.edu.mocker.utils.StringRef;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class MockerVehicle extends AbstractVerticle {
  private static String PROPERTIES_PATH = null;
  private static int APP_PORT = 9000;


  private static final String[] VALID_METHODS = new String[]{"GET", "POST", "PUT", "DELETE", "PATCH"};
  private static final String[] HEADER_FILES_SEARCH = new String[]{
          ".header.properties",
  };

  private static final String[] BODY_FILES_SEARCH = new String[]{
          ".error.json",
          ".stream.json",
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
        System.out.println(String.format("%s %s", App.NAME, App.VERSION));
        System.out.println("Starting server mocker at port: " + APP_PORT);

        vertx.createHttpServer().requestHandler(req -> {
            Path absPath = getLocalPath(req.uri());

            req.response().setChunked(true);

            if (Files.isRegularFile(absPath)) {
              System.out.println(String.format("%s -> serve static file",req.uri()));
              req.response().sendFile(absPath.toString());
              req.response().end();
            } else {
              System.out.println(String.format("%s %s", req.method(), req.uri()));

              if ( isMethodAllowed(req) ) {
                  ContentWriter contentWriter = ContentWriteFactory.get(vertx,req, absPath);
                  if ( contentWriter == null) {
                      writeDefaultResponse(req, "FILE NOT FOUND");
                  } else {
                    contentWriter.writeHeaderAndBody();
                  }
              } else {
                  writeDefaultResponse(req, "NOT METHOD VALID");
              }
            }
        }).listen(APP_PORT);
    }

    private Path getLocalPath(String uri){
      String[] parts = cleanUri(uri).split("/");

      Path absPath = Paths.get(PATH);

      for(String item : parts){
        absPath = absPath.resolve(item);
      }
      return absPath;
    }

    private boolean isMethodAllowed(HttpServerRequest req){
        String file = null;
        for(String validMethod: VALID_METHODS){
            if( validMethod.equalsIgnoreCase(req.method().toString()) ){
                file = validMethod;
            }
        }
        return file != null;
    }

    private void writeDefaultResponse(HttpServerRequest req, String message){
        req.response().putHeader("Cache-Control","no-cache");
        req.response().putHeader("Content-Type","text/html");
        req.response().write(message);
        req.response().end();
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


}

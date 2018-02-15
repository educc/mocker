package com.edu.mocker;

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

            Path absPath = getLocalPath(req.uri());

            req.response().setChunked(true);

            if(Files.isRegularFile(absPath)){
              System.out.println("It's regular file");
              try {
                req.response().write(Buffer.buffer(Files.readAllBytes(absPath)));
              } catch (IOException e) {
                req.response().write(e.toString());
              }
            }else{
              System.out.println("It's a folder");

              setHeader(req, absPath);
              setContent(req, absPath);
            }
            req.response().end();
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

    private void setContent(HttpServerRequest req, Path localPath){

      String file = null;
      for(String validMethod: VALID_METHODS){
        if( validMethod.equalsIgnoreCase(req.method().toString()) ){
          file = validMethod;
        }
      }

      if( file == null){
        System.out.println("NOT METHOD VALID");
        req.response().write("NOT METHOD VALID");
      }else {
        System.out.println("file = " + file);

        StringRef extUsed = new StringRef();
        String content = ContentFile.getContentWithIgnoreCase(
                localPath, file, extUsed, BODY_FILES_SEARCH);

        String contentType = "text/plain";
        int statusCode = 404;
        if (extUsed.data.length() > 0) {
          if (extUsed.data.indexOf("json") >= 0) {
            contentType = "application/json";
            statusCode = 200;
          }

          if (extUsed.data.indexOf("xml") >= 0) {
            contentType = "application/xml";
            statusCode = 200;
          }

          if (extUsed.data.indexOf("error") >= 0) {
            statusCode = 500;
          }
        }
        req.response().putHeader("Content-Type", contentType);
        req.response().setStatusCode(statusCode);
        req.response().write(content);
      }
    }

    private void setHeader(HttpServerRequest req, Path localPath){
      req.response().putHeader("Cache-Control","no-cache");

      Properties prop = ContentFile.readProperties(
              localPath, req.method().toString(), HEADER_FILES_SEARCH);

      if (prop != null){
        for(Map.Entry<Object, Object> key: prop.entrySet()){
          req.response().putHeader(
                  key.getKey().toString(),
                  key.getValue().toString());
        }
      }
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

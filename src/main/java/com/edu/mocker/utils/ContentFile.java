package com.edu.mocker.utils;

import com.edu.mocker.MockerVehicle;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ContentFile {


  public static Properties readProperties(Path dirpath, String file, String[] filesSearchExtension){
    Properties prop = null;

    try {
      for(String ext: filesSearchExtension){
        Path abspath = dirpath.resolve(file+ext);
        if(Files.exists(abspath)){
          prop = new Properties();
          prop.load(new FileReader(abspath.toFile()));
          break;
        }
        //tolowercase
        abspath = dirpath.resolve(file.toLowerCase()+ext);
        if(Files.exists(abspath)){
          prop = new Properties();
          prop.load(new FileReader(abspath.toFile()));
          break;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return prop;
  }

  public static String getContentWithIgnoreCase(
          Path abspath, String file, StringRef extUsed, String[] filesSearchExtension){
    String result = null;

    for(String ext: filesSearchExtension){
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

  private static String getContent(Path abspath, String file){
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

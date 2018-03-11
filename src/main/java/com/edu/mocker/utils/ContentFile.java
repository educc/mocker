package com.edu.mocker.utils;

import com.edu.mocker.MockerVehicle;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

  public static Path getPathWithIgnoreCase(Path abspath, String file, String[] filesSearchExtension){

    for(String ext: filesSearchExtension){
      Path result = abspath.resolve(file + ext);
      if( Files.exists(result) ){
        return result;
      }

      //tolowercase
      result = abspath.resolve(file.toLowerCase() + ext);
      if( Files.exists(result) ){
        return result;
      }
    }
    return null;
  }
}

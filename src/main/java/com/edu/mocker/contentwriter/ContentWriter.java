package com.edu.mocker.contentwriter;

import com.edu.mocker.utils.ContentFile;
import io.vertx.core.http.HttpServerRequest;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

public interface ContentWriter {

    int getStatusCode();
    String getContentType();
    void writeContent();
    void writeHeader();

    default void writeHeaderAndBody(){
        writeHeader();
        writeContent();
    }
}

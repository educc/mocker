package com.edu.mocker.contentwriter;

import com.edu.mocker.utils.ContentFile;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;

import java.nio.file.Path;

public class ContentWriteFactory {


    public static ContentWriter get(Vertx vertx, HttpServerRequest req, Path localPath){
        Path abspath = ContentFile.getPathWithIgnoreCase(
                localPath,
                req.method().toString(),
                FileSearch.BODY);
        if( abspath == null) return null;
        String name  = abspath.getFileName().toString();
        if( name.contains("stream") ){
            return new ContentWriterStreamImpl(vertx, req, abspath);
        }
        return new ContentWriterDefaultImpl(req, abspath);
    }
}

package com.edu.mocker.contentwriter;

import com.edu.mocker.utils.ContentFile;
import io.vertx.core.http.HttpServerRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

public class ContentWriterDefaultImpl implements ContentWriter {

    protected HttpServerRequest req;
    protected Path contentFile;

    public ContentWriterDefaultImpl(HttpServerRequest req, Path contentFile) {
        this.req = req;
        this.contentFile = contentFile;
    }

    @Override
    public int getStatusCode() {
        int statusCode = 200;
        String name = contentFile.getFileName().toString();

        if (name.indexOf("error") >= 0) {
            statusCode = 500;
        }
        return statusCode;
    }

    @Override
    public String getContentType() {
        String contentType = "text/plain";
        String name = contentFile.getFileName().toString();

        if (name.contains("json") || name.contains("error.json")) {
            contentType = "application/json";
        }

        if (name.contains("xml") || name.contains("error.xml")) {
            contentType = "text/xml";
        }

        return contentType;
    }

    @Override
    public void writeContent() {
        req.response().write(readContent());
        req.response().end();
    }

    private String readContent(){
        String content = null;
        try {
            content = new String(Files.readAllBytes(contentFile));
        } catch (IOException e) {
            content = e.getMessage();
        }
        return content;
    }

    @Override
    public void writeHeader() {
        req.response().putHeader("Cache-Control","no-cache");

        Properties prop = ContentFile.readProperties(
                contentFile.getParent(),
                req.method().toString(),
                FileSearch.HEADER);

        if (prop != null){
            for(Map.Entry<Object, Object> key: prop.entrySet()){
                req.response().putHeader(
                        key.getKey().toString(),
                        key.getValue().toString());
            }
        }

        req.response().putHeader("Content-Type", getContentType());
        req.response().setStatusCode(getStatusCode());
    }
}

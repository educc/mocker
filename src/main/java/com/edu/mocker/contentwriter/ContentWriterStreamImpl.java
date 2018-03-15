package com.edu.mocker.contentwriter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.streams.Pump;
import io.vertx.core.streams.ReadStream;
import io.vertx.rx.java.RxHelper;
import rx.Observable;
import rx.Single;

import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ContentWriterStreamImpl extends  ContentWriterDefaultImpl {

    private Vertx vertx;

    public ContentWriterStreamImpl(Vertx vertx, HttpServerRequest req, Path contentFile) {
        super(req, contentFile);
        this.vertx = vertx;
    }

    @Override
    public int getStatusCode() {
        return 200;
    }

    @Override
    public String getContentType() {
        if ( isAcceptStreamJson() ){
            return "application/stream+json";
            //return "text/event-stream";
        }
        return "application/json";
    }

    private boolean isAcceptStreamJson(){
        boolean result = true;
        String accept = req.getHeader("accept");
        if (accept != null && accept.toLowerCase().contains("application/json")) {
            result = false;
        }
        return result;
    }

    @Override
    public void writeContent() {
        if(!isAcceptStreamJson()){
            req.response()
                    .sendFile(contentFile.toString())
                    .end();
            return;
        }

        JsonElement content = null;
        try {
            content = readContent();
        } catch (FileNotFoundException e) {
            req.response()
                    .write("ERROR at read file " + contentFile.toString() + " :")
                    .write(e.getMessage())
                    .end();
            return;
        }

        if( content instanceof JsonArray){
            JsonArray el = (JsonArray) content;

            Observable<Buffer> observable = Observable.range(0,el.size())
                    .map(i -> el.get(i).toString() + "\n")
                    .map(str -> Buffer.buffer(str))
                    .concatMap(buff -> Observable.just(buff).delay(500,TimeUnit.MILLISECONDS))
                    .doOnCompleted(() -> req.response().end());

            ReadStream<Buffer> readStream = RxHelper.toReadStream(observable);

            Pump pump = Pump.pump(readStream, req.response());
            pump.start();

        }else{
            req.response().write(content.toString());
            req.response().end();
        }
    }

    private JsonElement readContent() throws FileNotFoundException {
        JsonReader reader = new JsonReader(new FileReader(contentFile.toFile()));
        Gson gson = new Gson();
        return gson.fromJson(reader, JsonElement.class);
    }

    @Override
    public void writeDefaultHeader() {
        req.response().putHeader("Connection", "keep-alive");
        req.response().putHeader("Cache-Control","no-cache");
    }
}

package com.edu.mocker.contentwriter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.parsetools.JsonParser;
import io.vertx.core.parsetools.RecordParser;
import io.vertx.core.streams.Pump;
import io.vertx.core.streams.ReadStream;
import io.vertx.rx.java.RxHelper;
import rx.Observable;
import rx.Single;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ContentWriterStreamImpl extends  ContentWriterDefaultImpl {

    public ContentWriterStreamImpl(HttpServerRequest req, Path contentFile) {
        super(req, contentFile);
    }

    @Override
    public int getStatusCode() {
        return 200;
    }

    @Override
    public String getContentType() {
        if ( isAcceptStreamJson() ){
            return "application/stream+json";
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
        JsonElement content = null;
        try {
            content = readContent();
        } catch (FileNotFoundException e) {
            req.response().write("ERROR at read file " + contentFile.toString() + " :");
            req.response().write(e.getMessage());
            req.response().end();
            return;
        }

        if(!isAcceptStreamJson()){
            req.response().write(content.toString());
            req.response().end();
            return;
        }

        if( content instanceof JsonArray){
            JsonArray el = (JsonArray) content;

            AtomicInteger delay = new AtomicInteger(500);
            Observable<Buffer> observable =
                    Observable.from(el)
                    .flatMapSingle(elItem -> {
                        delay.addAndGet(500);
                        return Single.just(Buffer.buffer(elItem.toString()))
                                .delay(delay.get() , TimeUnit.MILLISECONDS);
                    }).doOnCompleted(() -> req.response().end());

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

}

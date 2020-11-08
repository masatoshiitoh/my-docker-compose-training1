package com.example.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.redis.client.*;

import java.util.Arrays;


public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    vertx.createHttpServer().requestHandler(req -> {
      Redis.createClient(vertx, "redis://redis:6379/1")
        .connect(onConnect -> {
          if (onConnect.succeeded()) {
            RedisConnection client = onConnect.result();

            RedisAPI redis = RedisAPI.api(client);
            redis.get("mykey", res -> {
              if (res.succeeded()) {
                Integer curvalue;
                try {
                  curvalue = res .result().toInteger();
                } catch (NullPointerException e) {
                  curvalue = 0;
                }
                Integer nextValue =  curvalue + 1;

                redis.set(Arrays.asList("mykey", nextValue.toString()), setRes -> {
                  req.response()
                    .putHeader("content-type", "text/plain")
                    .end("Hello from Vert.x! with " + nextValue.toString());
                } );
              } else {

                redis.set(Arrays.asList("mykey", "0"), setRes -> {
                  req.response()
                    .putHeader("content-type", "text/plain")
                    .end("Hello from Vert.x! redis no value");
                });
              }
            });



          }
          else {
            req.response()
              .putHeader("content-type", "text/plain")
              .end("Hello from Vert.x! without redis");

          }
        });
    }).listen(8888, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }
}

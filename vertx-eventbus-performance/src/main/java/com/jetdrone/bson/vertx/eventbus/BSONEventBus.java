package com.jetdrone.bson.vertx.eventbus;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BSONEventBus {

    private static Handler<Message<Buffer>> wrapHandler(final Handler<Message<Map>> replyHandler) {
        if (replyHandler == null) {
            return null;
        }

        return new Handler<Message<Buffer>>() {
            @Override
            public void handle(final Message<Buffer> message) {
                // convert to
                Message<Map> bsonMessage = new Message<Map>() {
                    @Override
                    public void reply(Map document, Handler<Message<Map>> messageHandler) {
                        message.reply(BSONCodec.encode(document), wrapHandler(messageHandler));
                    }
                };

                bsonMessage.body = BSONCodec.decode(message.body);
                replyHandler.handle(bsonMessage);
            }
        };
    }



    private final EventBus eventBus;
    private final Map<Object, Handler<Message<Buffer>>> handlerMap;

    // Generic
    public BSONEventBus(Object object) {
        EventBus eventBus = null;

        if (object instanceof org.vertx.java.core.Vertx) {
            eventBus = ((org.vertx.java.core.Vertx) object).eventBus();
        }

        if (object instanceof org.vertx.groovy.core.Vertx) {
            eventBus = (((org.vertx.groovy.core.Vertx) object).toJavaVertx()).eventBus();
        }

        this.eventBus = eventBus;
        this.handlerMap = new ConcurrentHashMap<>();
    }

    public BSONEventBus(org.vertx.java.core.Vertx vertx) {
        this.eventBus = vertx.eventBus();
        this.handlerMap = new ConcurrentHashMap<>();
    }

    // Groovy
    public BSONEventBus(org.vertx.groovy.core.Vertx vertx) {
        this(vertx.toJavaVertx());
    }

    public void registerLocalHandler(String address, Handler<Message<Map>> handler) {
        Handler<Message<Buffer>> wrapped = wrapHandler(handler);
        handlerMap.put(handler, wrapped);
        eventBus.registerLocalHandler(address, wrapped);
    }

 
    public void registerHandler(String address, Handler<Message<Map>> handler) {
        Handler<Message<Buffer>> wrapped = wrapHandler(handler);
        handlerMap.put(handler, wrapped);
        eventBus.registerHandler(address, wrapped);
    }


    public void unregisterHandler(String address, Handler<Message<Map>> handler) {
        Handler<Message<Buffer>> wrapped = handlerMap.remove(handler);
        if (wrapped != null) {
            eventBus.unregisterHandler(address, wrapped);
        }
    }


    public void registerHandler(String address, Handler<Message<Map>> handler, AsyncResultHandler<Void> resultHandler) {
        Handler<Message<Buffer>> wrapped = wrapHandler(handler);
        handlerMap.put(handler, wrapped);
        eventBus.registerHandler(address, wrapped, resultHandler);
    }

 
    public void unregisterHandler(String address, Handler<Message<Map>> handler, AsyncResultHandler<Void> resultHandler) {
        Handler<Message<Buffer>> wrapped = handlerMap.remove(handler);

        if (wrapped != null) {
            eventBus.unregisterHandler(address, wrapped, resultHandler);
        }
    }


    public void send(String address, Map message, Handler<Message<Map>> replyHandler) {
        Buffer _message = BSONCodec.encode(message);
        eventBus.send(address, _message, wrapHandler(replyHandler));
    }


    public void send(String address, Map message) {
        eventBus.send(address, BSONCodec.encode(message));
    }

    public void publish(String address, Map message) {
        eventBus.publish(address, BSONCodec.encode(message));
    }
/*
	public void registerJOHandler(String address, final Handler<Message<JsonObject>> handler,AsyncResultHandler<Void> resultHandler) {
		registerHandler(address, new Handler<Message<Map>>() {
			@Override
			public void handle(final Message<Map> event) {
				Message<JsonObject> msg = new Message<JsonObject>() {
					@Override
					public void reply(JsonObject message,
							Handler<Message<JsonObject>> replyHandler) {
						event.reply(message.toMap());
					}
				};
				msg.body = new JsonObject(event.body);
				
				handler.handle(msg);
			}
		}, resultHandler);
	}
	
	public void sendJO(String address, final JsonObject message, final Handler<Message<JsonObject>> replyHandler) {
		this.send(address, message.toMap(), new Handler<Message<Map>>() {
			
			@Override
			public void handle(final Message<Map> event) {
				Message<JsonObject> msg = new Message<JsonObject>() {
					@Override
					public void reply(JsonObject message,
							Handler<Message<JsonObject>> replyHandler) {
						event.reply(message.toMap());
					}
				};
				msg.body = new JsonObject(event.body);
				
				replyHandler.handle(msg);
			}
		});
    }
*/
}

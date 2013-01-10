package my.test.bson;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.deploy.Verticle;

import com.jetdrone.bson.vertx.eventbus.BSONEventBus;

public class MessagePingPong extends Verticle implements Handler<Message<Map>>{
	private String MONITOR = "monitor";
	private String PROCESSOR = "processor";
	
	private long recv_nr = 1;
	final private int nr = (int) (Math.random() * 100);
	private Date start = new Date();

	private BSONEventBus eventbus;
	
	@Override
	public void handle(Message<Map> event) {
		countMessage();
		
		event.body.put("recv_nr", recv_nr);
		event.reply(event.body);
		
		eventbus.send(PROCESSOR, event.body, new Handler<Message<Map>>() {
			@Override
			public void handle(Message<Map> event) {
				countMessage();
			}
		});

	}
	
	private void countMessage(){
		recv_nr++;
		
		if(recv_nr % 1000000 == 0){
			Date now = new Date();
			long diff = now.getTime() - start.getTime();
			start = now;
			eventbus.publish(MONITOR, new JsonObject().putNumber("name", nr).putNumber("recv_nr", recv_nr).putNumber("time", diff).toMap());
		}
	}

	@Override
	public void start() throws Exception {
		System.out.println("Register "+nr+" to address "+PROCESSOR);
		eventbus = new BSONEventBus(vertx);
		
		eventbus.registerHandler(PROCESSOR, this, new AsyncResultHandler<Void>() {
			@Override
			public void handle(AsyncResult<Void> event) {
				
			}
		});
	}
}

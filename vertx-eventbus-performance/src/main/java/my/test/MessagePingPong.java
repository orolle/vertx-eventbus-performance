package my.test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.deploy.Verticle;

public class MessagePingPong extends Verticle implements Handler<Message<JsonObject>>{
	private String MONITOR = "monitor";
	private String PROCESSOR = "processor";
	
	private long recv_nr = 1;
	final private int nr = (int) (Math.random() * 100);
	private Date start = new Date();

	@Override
	public void handle(Message<JsonObject> event) {
		countMessage();
		
		event.body.putNumber("recv_nr", recv_nr);
		event.reply(event.body);
		
		vertx.eventBus().send(PROCESSOR, event.body, new Handler<Message<JsonObject>>(){
			@Override
			public void handle(Message<JsonObject> event) {
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
			vertx.eventBus().publish(MONITOR, new JsonObject().putNumber("name", nr).putNumber("recv_nr", recv_nr).putNumber("time", diff));
		}
	}

	@Override
	public void start() throws Exception {
		System.out.println("Register "+nr+" to address "+PROCESSOR);
		this.vertx.eventBus().registerHandler(PROCESSOR, this);
	}
}

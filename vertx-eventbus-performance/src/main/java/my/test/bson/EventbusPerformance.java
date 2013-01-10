package my.test.bson;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.deploy.Verticle;
import com.jetdrone.bson.vertx.eventbus.BSONEventBus;

public class EventbusPerformance extends Verticle {
	private int INSTANCES = 1;
	private String MONITOR = "monitor";
	private String PROCESSOR = "processor";

	private BSONEventBus eventbus;

	@Override
	public void start() throws Exception {
		eventbus = new BSONEventBus(vertx);

		container.deployVerticle("my.test.bson.MessagePingPong", new JsonObject(), INSTANCES, new Handler<String>() {
			@Override
			public void handle(String event) {
				System.out.println(event);
				eventbus.registerHandler(MONITOR, new Handler<Message<Map>>() {
					@Override
					public void handle(Message<Map> event) {
						monitor(new JsonObject(event.body));
					}
				}, new AsyncResultHandler<Void>() {
					@Override
					public void handle(AsyncResult<Void> event) {
						System.out.println("Send init messages");
						for(int i = 0; i < 4 * INSTANCES ; i++){
							eventbus.send(PROCESSOR, new JsonObject().putNumber("recv_nr", 0).toMap());
						}
					}
				});
			}
		});

	}

	HashMap<Long, List<Integer>> results = new HashMap<>();

	public void monitor(JsonObject o){
		System.out.println();
		System.out.println(o.getNumber("time")+"ms (total "+o.getNumber("recv_nr")+")");

		if(results.get(Long.parseLong(o.getNumber("recv_nr")+"")) == null){
			results.put(Long.parseLong(o.getNumber("recv_nr")+""), new ArrayList<Integer>());
		}

		List<Integer> data = results.get(Long.parseLong(o.getNumber("recv_nr")+""));
		data.add(Integer.parseInt(o.getNumber("time")+""));


		//results.remove(Integer.parseInt(o.getNumber("time")+""));

		double total = 0;
		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		for (Entry<Long, List<Integer>> e : results.entrySet()) {
			int events = 1000000 * data.size();
			double sum = 0;
			for (Integer integer : e.getValue()) {
				sum += integer;
			}
			sum /= 1000;
			sum = events / sum;

			if(e.getValue() == data){
				System.out.println("      AVG events/second = "+sum);
			}

			total += (1.0 / ((double)results.size())) * sum;
			min = sum < min? sum : min;
			max = sum > max? sum : max;
		}

		System.out.println("TOTAL MIN events/second = "+min);
		System.out.println("TOTAL AVG events/second = "+total);
		System.out.println("TOTAL MAX events/second = "+max);
	}

}

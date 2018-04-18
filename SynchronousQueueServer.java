import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class SynchronousQueueServer {

  private static final String queueName = "platform_synchronous_queue";

  public static void main(String[] argv) {
	  //mqIp  mqUname  mqPasswd  mqPort 
	  if(argv.length<4)
	  {
		  System.out.println("lesss arguments");
		  return ;
	  }
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(argv[0]);
    factory.setPort(Integer.parseInt(argv[3]));
    factory.setUsername(argv[1]);
    factory.setPassword(argv[2]);
    System.out.println(factory);
    Connection connection = null;
    try {
      connection = factory.newConnection();
      final Channel channel = connection.createChannel();
      System.out.println(channel);
      channel.queueDeclare(queueName, false, false, false, null);

      channel.basicQos(1);

      System.out.println(" [x] Awaiting RPC requests");

      Consumer consumer = new DefaultConsumer(channel) {
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
          AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                  .Builder()
                  .correlationId(properties.getCorrelationId())
                  .build();

          String response = "";

          try 
          {
            String message = new String(body,"UTF-8");
            System.out.println("Receive Message is "+message);
            int bpValue = Integer.parseInt(message);
            int randomNumber = 30 + (int)(Math.random() * ((50 - 30) + 1));
            
            response = "Your BP is " + randomNumber + "% more than average BP";
            LoggingService.addLogs("Synchronous_Queue", "syncQueueService", "Output is "+response);
            System.out.println(response);
           
          }
          catch (RuntimeException e){
            System.out.println(" [.] " + e.toString());
          }
          finally {
            channel.basicPublish( "", properties.getReplyTo(), replyProps, response.getBytes("UTF-8"));
            channel.basicAck(envelope.getDeliveryTag(), false);
            // RabbitMq consumer worker thread notifies the RPC server owner thread 
            synchronized(this) {
            	this.notify();
            }
          }
        }
      };

      channel.basicConsume(queueName, false, consumer);
      // Wait and be prepared to consume the message from RPC client.
      while (true) {
      	synchronized(consumer) {
      		try {
      			consumer.wait();
      	    } catch (InterruptedException e) {
      	    	e.printStackTrace();	    	
      	    }
      	}
      }
    } catch (IOException | TimeoutException e) {
      e.printStackTrace();
    }
    finally {
      if (connection != null)
        try {
          connection.close();
        } catch (IOException _ignore) {}
    }
  }
}
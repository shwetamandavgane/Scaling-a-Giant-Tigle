package com.northeastern.bsds.server.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;

import java.util.concurrent.TimeoutException;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
/**
 * Represents a connection with a queue
 * @author syntx
 * References: https://dzone.com/articles/getting-started-rabbitmq-java
 */
public class EndPoint{

  protected Channel channel;
  protected Connection connection;
  protected String endPointName;

  public EndPoint(String endpointName)  {
    try {
      this.endPointName = endpointName;

      ConnectionFactory factory = new ConnectionFactory();
// "guest"/"guest" by default, limited to localhost connections

      factory.setUsername(System.getProperty("queue.username"));
      factory.setPassword(System.getProperty("queue.password"));
      factory.setVirtualHost("/");
      factory.setHost(System.getProperty("queue.host"));
      factory.setPort(Integer.parseInt(System.getProperty("queue.port")));

      connection = factory.newConnection();
      //creating a channel
      channel = connection.createChannel();

      channel.exchangeDeclare(System.getProperty("queue.exchange"),
                      System.getProperty("queue.type"),true);

      //declaring a queue for this channel. If queue does not exist,
      //it will be created on the server.
      channel.queueDeclare(System.getProperty("queue.name"), false, false, false, null);
      channel.queueBind(System.getProperty("queue.name"), System.getProperty("queue.exchange"),
          System.getProperty("queue.typeName"));

    } catch (TimeoutException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Close channel and connection. Not necessary as it happens implicitly any way.
   * @throws IOException
   */
  public void close() throws IOException, TimeoutException {
    this.channel.close();
    this.connection.close();
  }
}
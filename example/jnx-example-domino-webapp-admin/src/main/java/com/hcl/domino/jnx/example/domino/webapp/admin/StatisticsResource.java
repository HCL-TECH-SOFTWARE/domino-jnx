package com.hcl.domino.jnx.example.domino.webapp.admin;

import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.concurrent.ExecutionException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.admin.ServerStatistics;

@Path("statistics")
public class StatisticsResource {
  public static final String FACILITY = "JNXExample";
  public static final String REQUEST_NAME = "RequestCount";
  public static final String STRING_NAME = "String";
  public static final String DOUBLE_NAME = "Double";

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String get() throws InterruptedException, ExecutionException {
    return RestEasyServlet.instance.executor.submit(() -> {
      try(DominoClient client = DominoClientBuilder.newDominoClient().build()) {
        client.getServerStatistics().updateStatistic(FACILITY, REQUEST_NAME, EnumSet.allOf(ServerStatistics.Flag.class), 1);
      }
      return MessageFormat.format("Incremented {0}.{1}", FACILITY, REQUEST_NAME);
    }).get();
  }
  
  @POST
  @Produces(MediaType.TEXT_PLAIN)
  public String updateString(String newValue) throws InterruptedException, ExecutionException {
    return RestEasyServlet.instance.executor.submit(() -> {
      try(DominoClient client = DominoClientBuilder.newDominoClient().build()) {
        client.getServerStatistics().updateStatistic(FACILITY, STRING_NAME, EnumSet.of(ServerStatistics.Flag.UNIQUE), newValue);
      }
      return MessageFormat.format("Set {0}.{1}", FACILITY, STRING_NAME);
    }).get();
  }
  
  @Path("double")
  @POST
  @Produces(MediaType.TEXT_PLAIN)
  public String updateDouble(double newValue) throws InterruptedException, ExecutionException {
    return RestEasyServlet.instance.executor.submit(() -> {
      try(DominoClient client = DominoClientBuilder.newDominoClient().build()) {
        client.getServerStatistics().updateStatistic(FACILITY, DOUBLE_NAME, EnumSet.of(ServerStatistics.Flag.UNIQUE), newValue);
      }
      return MessageFormat.format("Set {0}.{1}", FACILITY, DOUBLE_NAME);
    }).get();
  }
}

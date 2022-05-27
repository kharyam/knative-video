// camel-k: language=java

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.function.Supplier;

import com.google.api.services.sheets.v4.model.ValueRange;

import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.language.SimpleExpression;
import org.apache.camel.support.DefaultRegistry;

public class ChatGoogleSheets extends RouteBuilder {
  @Override
  public void configure() throws Exception {

    String range = "A1";
    String clientId = System.getenv("CLIENT_ID");
    String clientSecret = System.getenv("CLIENT_SECRET");
    String refreshToken =  System.getenv("REFRESH_TOKEN");

    // Write your routes here, for example:
    // RouteDefinition rd = from("timer:java?period=3000")
    RouteDefinition rd = from("knative:channel/chat-channel")
      .routeId("ChatLogGoogleSheets")
      .log("Processing ${body}")
      .setHeader("CamelGoogleSheets.valueInputOption", constant("USER_ENTERED"));

      Supplier s = new Supplier<ValueRange>() {
        @Override
        public ValueRange get() {
          ValueRange vr = new ValueRange();
          ArrayList rows = new ArrayList<ArrayList<String>>();
          ArrayList row = new ArrayList<String>();
          row.add(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).format(ZonedDateTime.now()));
          row.add("This is a new row");
          rows.add(row);
          vr.setValues(rows);
          return vr;
        }
      };
  
      rd.setHeader("CamelGoogleSheets.values", s)
        .to("google-sheets://data/append?range=" + range 
          + "&clientId=" + clientId + "&clientSecret=" + clientSecret
          + "&refreshToken=" + refreshToken);
        }
}

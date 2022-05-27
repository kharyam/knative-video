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

public class ChatGoogleSheets extends RouteBuilder {
  @Override
  public void configure() throws Exception {

    String range = "A1";
    String clientId = System.getenv("CLIENT_ID");
    String clientSecret = System.getenv("CLIENT_SECRET");
    String refreshToken =  System.getenv("REFRESH_TOKEN");

    RouteDefinition rd = from("knative:channel/chat-channel")
      .routeId("ChatLogGoogleSheets")
      .log("Processing ${body}")
      .to("direct:setRow");
      

    Supplier valueSupplier = new Supplier<ValueRange>() {

      private String message;

      public void setMessage(String message) {
        this.message=message;
      }

      @Override
      public ValueRange get() {
        if (message == null) {
          return null;
        }

        ValueRange values = new ValueRange();
        ArrayList rows = new ArrayList<ArrayList<String>>();
        ArrayList row = new ArrayList<String>();
        row.add(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).format(ZonedDateTime.now()));
        String [] components = this.message.split(":");
        row.add(components[0].trim());
        row.add(components[1].trim());
        rows.add(row);
        values.setValues(rows);
        return values;
      }
    };

    getContext().getRegistry().bind("valueSupplier", valueSupplier);

    from("direct:setRow").setHeader("CamelGoogleSheets.valueInputOption", constant("USER_ENTERED"))
      .bean("valueSupplier","setMessage")
      .setHeader("CamelGoogleSheets.values", valueSupplier)
      .to("google-sheets://data/append?range=" + range 
        + "&clientId=" + clientId + "&clientSecret=" + clientSecret
        + "&refreshToken=" + refreshToken);
  }
  
}
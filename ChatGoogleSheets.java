// camel-k: language=java

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.function.Supplier;

import com.google.api.services.sheets.v4.model.ValueRange;

/**
 * This class implements a route which reads data of the format
 * "user: message" from a knative channel then appends the timestamp,
 * username, and message as a new row in a google doc.
 */
public class ChatGoogleSheets extends RouteBuilder {
  @Override
  public void configure() throws Exception {

    // Append data at the end of existing rows starting at cell A1
    String range = "A1";

    // Google authentication credentials.
    String clientId = System.getenv("CLIENT_ID");
    String clientSecret = System.getenv("CLIENT_SECRET");
    String refreshToken =  System.getenv("REFRESH_TOKEN");

    // A Custom Supplier instance to represent a row in a google doc.
    Supplier<ValueRange> valueSupplier = new Supplier<ValueRange>() {

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
        ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();
        ArrayList<String> row = new ArrayList<String>();
        row.add(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).format(ZonedDateTime.now()));
        String [] components = this.message.split(":");
        row.add(components[0].trim());
        row.add(components[1].trim());
        rows.add(row);
        values.setValues(rows);
        return values;
      }
    };

    // Add object to Camel Registry
    getContext().getRegistry().bind("valueSupplier", valueSupplier);

    /////////////////
    // Camel route //
    /////////////////
    from("knative:channel/chat-channel")
      .routeId("ChatLogGoogleSheets")
      .log("Processing ${body}")
      .bean("valueSupplier","setMessage")
      .setHeader("CamelGoogleSheets.values", valueSupplier)
      .setHeader("CamelGoogleSheets.valueInputOption", constant("USER_ENTERED"))
      .to("google-sheets://data/append?range=" + range 
        + "&clientId=" + clientId + "&clientSecret=" + clientSecret
        + "&refreshToken=" + refreshToken);
  }
  
}
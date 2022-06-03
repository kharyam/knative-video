// camel-k: language=java

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
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
    String spreadhseetID = System.getenv("SPREADSHEET_ID");

    // A Custom Supplier instance to represent a row in a google doc.
    Supplier<Object> valueSupplier = new Supplier<Object>() {

      private String message;

      public void setMessage(String message) {
        this.message=message;
      }

      @Override
      public Object get() {
        if (message == null) {
          return null;
        }

        ValueRange values = new ValueRange();
        List<List<Object>> rows = new ArrayList();
        List<Object> row = new ArrayList<Object>();
        row.add(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).format(ZonedDateTime.now()));
        String [] messageParts = this.message.split(":");
        row.add(messageParts[0].trim());
        row.add("'" + messageParts[1].trim());
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
      .bean("valueSupplier", "setMessage")
      .setHeader("CamelGoogleSheets.values", valueSupplier)
      .setHeader("CamelGoogleSheets.valueInputOption", constant("USER_ENTERED"))
      .to("google-sheets://data/append?spreadsheetId=" + spreadhseetID
        + "&range=" + range 
        + "&clientId=" + clientId 
        + "&clientSecret=" + clientSecret
        + "&refreshToken=" + refreshToken);
  }
  
}

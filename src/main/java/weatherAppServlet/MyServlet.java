package weatherAppServlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Servlet implementation class MyServlet
 */
@WebServlet("/MyServlet")
public class MyServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public MyServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String ApiKey = "425c30e1edeb1a942464658be4972923";
        String inputCity = request.getParameter("UserInputCity");
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + inputCity + "&appid=" + ApiKey;

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // 🔹 Check if API request was successful
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) { // 200 means success
                request.setAttribute("error", "City not found! Please enter a valid city.");
                request.getRequestDispatcher("index.jsp").forward(request, response);
                return; // Stop execution if API fails
            }

            // Read API response
            InputStream in = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);
            StringBuilder responseContent = new StringBuilder();
            Scanner sc = new Scanner(reader);

            while (sc.hasNext()) {
                responseContent.append(sc.nextLine());
            }
            sc.close();
            in.close(); // 🔹 Prevent memory leaks

            // Parse JSON response
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(responseContent.toString(), JsonObject.class);

            long dateTimestamp = jsonObject.get("dt").getAsLong() * 1000;
            String date = new Date(dateTimestamp).toString();

            // Temperature
            double temperatureKelvin = jsonObject.getAsJsonObject("main").get("temp").getAsDouble();
            int temperatureCelsius = (int) (temperatureKelvin - 273.15);

            // Humidity
            int humidity = jsonObject.getAsJsonObject("main").get("humidity").getAsInt();

            // Wind Speed
            double windSpeed = jsonObject.getAsJsonObject("wind").get("speed").getAsDouble();

            // Weather Condition (🔹 Fix: Use `.getAsString()` to remove quotes)
            String weatherCondition = jsonObject.getAsJsonArray("weather")
                .get(0).getAsJsonObject().get("main").getAsString();

            // Set attributes for JSP
            request.setAttribute("date", date);
            request.setAttribute("city", inputCity);
            request.setAttribute("temperature", temperatureCelsius);
            request.setAttribute("weatherCondition", weatherCondition);
            request.setAttribute("humidity", humidity);
            request.setAttribute("windSpeed", windSpeed);
            request.setAttribute("weatherData", responseContent.toString());

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            request.setAttribute("error", "An error occurred while fetching weather data.");
        }

        // Forward to JSP
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }
}


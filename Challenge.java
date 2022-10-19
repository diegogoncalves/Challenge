import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

public class Challenge {

    static final String CANDIDATE_ID = "e5f30ed6-c59e-47d9-9d4e-3cdf9da10a65";
    static final String API_PATH = "https://challenge.crossmint.io/api/";
    static final int RETRY_DELAY_MS = 1000;
    static final int HTTP_TOO_MANY_REQUESTS_CODE = 429;

    static final String SOLOON_CONST = "SOLOON";
    static final String COMETH_CONST = "COMETH";
    static final String POLYANET_CONST = "POLYANET";

    public static void main(String[] args) {
        JSONArray jsonArray = getChallenge();
        for (int currentRow = 0; currentRow < jsonArray.length(); currentRow++) {
            JSONArray jsonArrayRow = jsonArray.getJSONArray(currentRow);
            for (int currentCol = 0; currentCol < jsonArrayRow.length(); currentCol++) {
                String currentElement = jsonArrayRow.getString(currentCol);
                if (currentElement.equals(POLYANET_CONST)) {
                    createPolyanets(currentRow, currentCol);
                } else if (currentElement.length() > SOLOON_CONST.length() && currentElement
                        .substring(currentElement.length() - SOLOON_CONST.length()).equals(SOLOON_CONST)) {
                    String color = currentElement.substring(0, currentElement.length() - (SOLOON_CONST.length() + 1))
                            .toLowerCase();
                    createSoloons(currentRow, currentCol, color);
                } else if (currentElement.length() > COMETH_CONST.length() && currentElement
                        .substring(currentElement.length() - COMETH_CONST.length()).equals(COMETH_CONST)) {
                    String direction = currentElement
                            .substring(0, currentElement.length() - (COMETH_CONST.length() + 1)).toLowerCase();
                    createCometh(currentRow, currentCol, direction);
                }
            }
        }
    }

    private static void createPolyanets(int row, int column) {
        String argumentsString = new JSONObject()
                .put("row", row)
                .put("column", column)
                .put("candidateId", CANDIDATE_ID)
                .toString();

        createObject("polyanets", argumentsString);
    }

    private static void createSoloons(int row, int column, String color) {
        String argumentsString = new JSONObject()
                .put("row", row)
                .put("column", column)
                .put("color", color)
                .put("candidateId", CANDIDATE_ID)
                .toString();

        createObject("soloons", argumentsString);
    }

    private static void createCometh(int row, int column, String direction) {
        String argumentsString = new JSONObject()
                .put("row", row)
                .put("column", column)
                .put("direction", direction)
                .put("candidateId", CANDIDATE_ID)
                .toString();

        createObject("comeths", argumentsString);

    }

    private static JSONArray getChallenge() {
        StringBuffer response = doHTTPRequest("map/" + CANDIDATE_ID + "/goal", "GET", null, 0);
        JSONObject jsonObject = new JSONObject(response.toString());
        return jsonObject.getJSONArray("goal");
    }

    private static void createObject(String pathSuffix, String argumentsString) {
        doHTTPRequest(API_PATH + pathSuffix, "POST", null, 0);
    }

    private static StringBuffer doHTTPRequest(String pathSuffix, String requestType, String argumentsString,
            int waitSeconds) {
        try {
            URL url = new URL(API_PATH + pathSuffix);
            HttpURLConnection myConn = (HttpURLConnection) url.openConnection();
            myConn.setRequestMethod(requestType);
            if (requestType.equals("POST")) {
                myConn.setRequestProperty("Content-Type", "application/json");
                myConn.setDoOutput(true);

                OutputStream out = myConn.getOutputStream();
                byte[] input = argumentsString.getBytes("utf-8");
                out.write(input, 0, input.length);
            }

            int responseCode = myConn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        myConn.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response;
            } else if (responseCode == HTTP_TOO_MANY_REQUESTS_CODE) {
                waitSeconds += RETRY_DELAY_MS;
                Thread.sleep(waitSeconds);
                return doHTTPRequest(pathSuffix, requestType, argumentsString, waitSeconds);
            } else {
                System.out.println("Error code: " + responseCode);
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.toString());
        }
        return null;
    }
}
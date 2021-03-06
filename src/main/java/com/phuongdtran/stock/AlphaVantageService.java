package com.phuongdtran.stock;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AlphaVantageService implements IStockService {
    private final String ALPHA_VANTAGE_KEY = "PDOGDFRY2VU8A943";
    private final int HTTP_OK = 200;
    private final Gson gson = new Gson();

    /**
     * Get stock data from a stock API service and parse the data into a Map that each entry has the following format:
     * { date : {
     *          "price" : value1,
     *          "dividend": value2,
     *          "split": value3
     *          }
     * }
     * where date represents a specific date (such as 2019-1-1)
     * @param symbol the stock symbol like MSFT
     * @return stock data if able to get data from the API service. Otherwise, return null.
     * @throws IOException
     */
    @Override
    public Map<String, JsonObject> get(String symbol, OUTPUTSIZE outputsize) throws IOException{
        OkHttpClient client = new OkHttpClient();
        Response response = null;
        HttpUrl url = buildUrl(symbol, outputsize);
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        try {
            response = call.execute();
            if (response.code() != HTTP_OK) {
                return null;
            }
            JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
            if (json.has("Error Message")) {
                return null;
            }
//            JsonObject content = gson.fromJson(response.body().string(), JsonObject.class);
            return parseContent(json);

        }finally {
            response.close();
        }
    }

    // this is how the response content looks like
    // https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=MSFT&outputsize=full&apikey=demo
    // each entry in the map has the following format:
    // { date : {
    //          "price" : value1,
    //          "dividend": value2,
    //          "split": value3
    //          }
    // }
    // where date represents a specific date (such as 2019-1-1)
    private static Map<String, JsonObject> parseContent(JsonObject content) {
        Map<String, JsonObject> parsed = new HashMap<String, JsonObject>();
        JsonObject data = content.getAsJsonObject("Time Series (Daily)");
        for (String date : data.keySet()) {
            // extract appropriate info
            JsonObject singleDateData = data.getAsJsonObject(date);
            String price = singleDateData.getAsJsonPrimitive("4. close").getAsString();
            String dividend = singleDateData.getAsJsonPrimitive("7. dividend amount").getAsString();
            String split = singleDateData.getAsJsonPrimitive("8. split coefficient").getAsString();

            // create new JsonObject with extracted data
            JsonObject newObject = new JsonObject();
            newObject.addProperty("price", price);
            newObject.addProperty("dividend", dividend);
            newObject.addProperty("split", split);

            parsed.put(date, newObject);
        }
        return parsed;
    }

    private HttpUrl buildUrl(String symbol, OUTPUTSIZE outputsize) {
        String size = "full";
        if (outputsize == OUTPUTSIZE.COMPACT) {
            size = "compact";
        }
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("www.alphavantage.co")
                .addPathSegment("query")
                .setQueryParameter("function", "TIME_SERIES_DAILY_ADJUSTED")
                .setQueryParameter("symbol", symbol)
                .setQueryParameter("outputsize", size)
                .setQueryParameter("apikey", ALPHA_VANTAGE_KEY)
                .build();
        return url;
    }


}

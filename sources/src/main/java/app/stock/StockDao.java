package app.stock;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
/**
 * Responsible for request stock data from quandl.com
 * @author PhuongTran
 *
 */
public class StockDao {
	//quandl api key
	private static final String apiKey = "LSHfJJyvzYHUyU9jHpn6";
	private static Map<String,String> summary;

	//https://hc.apache.org/httpcomponents-client-ga/tutorial/html/fundamentals.html#d5e49
	//https://github.com/google/gson/blob/master/UserGuide.md
	public static Map<String,Map<String,Double>> getStockData(long invest, String symbol, String startDate, String endDate) {
		try{
			URI uri = getRequestUri(symbol, startDate, endDate);
			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpGet request = new HttpGet(uri);
			System.out.println("http get: " + request.getURI());//TODO: uri printing, need del later
			ResponseHandler<JsonObject> rh = new ResponseHandler<JsonObject>() {

				@Override
				public JsonObject handleResponse(final HttpResponse response) throws IOException {
					StatusLine statusLine = response.getStatusLine();
					HttpEntity entity = response.getEntity();
					if (statusLine.getStatusCode() >= 300) {
						throw new HttpResponseException(statusLine.getStatusCode(),statusLine.getReasonPhrase());
					}else if (entity == null) {
						throw new ClientProtocolException("Response contains no content");
					}
					Gson gson = new GsonBuilder().create();
					ContentType contentType = ContentType.getOrDefault(entity);
					Charset charset = contentType.getCharset();
					Reader reader = new InputStreamReader(entity.getContent(), charset);
					return gson.fromJson(reader, JsonObject.class);
				}
			};
			JsonObject quandlJson = httpclient.execute(request,rh);
			JsonArray dataArr = quandlJson.getAsJsonObject("datatable").getAsJsonArray("data");
			return reformartAndComputeReturn(dataArr,invest);//TODO: experiencing data loss with large amount.
		} 
		catch(Exception ex){
			System.out.println(ex.getMessage());
			return null;
		}
	}

	private static URI getRequestUri(String symbol, String startDate, String endDate) throws URISyntaxException{
		//https://docs.quandl.com/docs/parameters-1
		URI uri = new URIBuilder()
				.setScheme("https")
				.setHost("quandl.com")
				.setPath("/api/v3/datatables/WIKI/PRICES.json")
				.setParameter("qopts.columns", "date,ticker,close,split_ratio")
				.setParameter("date.gte", startDate)
				.setParameter("date.lte", endDate)
				.setParameter("ticker", symbol)
				.setParameter("api_key", apiKey)
				.build();
		return uri;
	}

	private static Map<String,Map<String,Double>> reformartAndComputeReturn(JsonArray dataArr,long investment){
		summary = new HashMap<String,String>();

		//MUST use TreeMap here to sort dates
		Map<String,Map<String,Double>> balanceMap = new TreeMap<String,Map<String,Double>>();
		//compute number of shares (investment divided by price on starting date)  
		double numberOfShares = investment / dataArr.get(0).getAsJsonArray().get(2).getAsDouble();
		//numberOfShares = round(numberOfShares,6);
		for ( int i=0 ; i<dataArr.size() ; i++) {
			//each element is ["2016-12-28","MSFT",62.99,2.0]
			JsonArray e = dataArr.get(i).getAsJsonArray();
			String date = e.get(0).getAsString();
			String ticker = e.get(1).getAsString(); 
			double price = e.get(2).getAsDouble();
			double split = e.get(3).getAsDouble();			
			if(split != 1d){
				numberOfShares = numberOfShares * split;
			}
			double balance = round(numberOfShares * price, 2);
			if (balanceMap.containsKey(date)){
				Map<String,Double> value = balanceMap.get(date);
				value.put(ticker, balance);
			}else {
				Map<String,Double> newValue = new HashMap<String,Double>();
				newValue.put(ticker, balance);
				balanceMap.put(date, newValue);
			}
			//summary
			if(i==0){
				summary.put("symbol", ticker);
				summary.put("startDate", date);
				summary.put("startPrice", Double.toString(price));
				summary.put("startQuantity",Double.toString(numberOfShares));
				summary.put("startBalance", Double.toString(investment));
			}else if(i==dataArr.size()-1){
				summary.put("endDate", date);
				summary.put("endPrice", Double.toString(price));
				summary.put("endQuantity",Double.toString(numberOfShares));
				summary.put("endBalance",Double.toString(balance));
			}
		}
		
		return balanceMap;
	}
	public static Map<String,String> getSummary(){
		return summary;
	}

	/*
	 * round numbers to nth decimal places
	 */
	private static double round(double number,int n){
		double decimal = Math.pow(10, n);
		return Math.round(number * decimal) / decimal; 
	}
}

package external;
import entity.Item;
import entity.Item.ItemBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;




public class TicketMasterAPI {
	private static final String URL =  "https://app.ticketmaster.com/discovery/v2/events.json";
	private static final String DEFAULT_KEYWORD = "";
	private static final String API_KEY = "3JZE24q9dhoYla2Amf5ymkGDlla3pyZW";
	private static final String size = "50";
	private static final String pageNum = "3";

	public List<Item> search(double lat, double lon, String keyword) {
		if (keyword == null) {
			keyword = DEFAULT_KEYWORD;
		}
		try {
			keyword = URLEncoder.encode(keyword, "UTF-8");
			
		}catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String query = String.format("apikey=%s&latlong=%s,%s&keyword=%s&radius=%s&size=%s&page=%s", 
				API_KEY, lat, lon, keyword, 50, size, pageNum);
		String url = URL + "?" + query;
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestMethod("GET");
			// the first time we connect ticketmaster api. the first two rows just create an instance.
			int responseCode = connection.getResponseCode();
			System.out.println("Sending request to url" + url);
			System.out.println("Response code:" + responseCode);
			if (responseCode != 200) {
				return new ArrayList<>();
			}
			// bufferedreader 一次能读取8K大小 以数据流的形式连续读入   然后用stringbuilder合并存储起来
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			StringBuilder response = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			reader.close();
			JSONObject obj = new JSONObject(response.toString());
			if (!obj.isNull("_embedded")) {
				JSONObject embedded = obj.getJSONObject("_embedded");
				return getItemList(embedded.getJSONArray("events"));
			}
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}
	
	private List<Item> getItemList(JSONArray events) throws JSONException{
		List<Item> itemList = new ArrayList<>();
		for (int i = 0; i < events.length(); ++i) {
			JSONObject event = events.getJSONObject(i);
			ItemBuilder builder = new ItemBuilder();
			if (!event.isNull("id")) {
				builder.setItemId(event.getString("id"));
			}
			if (!event.isNull("name")) {
				builder.setName(event.getString("name"));
			}
			if (!event.isNull("url")) {
				builder.setUrl(event.getString("url"));
			}
			if (!event.isNull("distance")) {
				builder.setDistance(event.getDouble("distance"));
			}
			if (!event.isNull("rating")) {
				builder.setRating(event.getDouble("rating"));
			}
			builder.setAddress(getAddress(event));
			builder.setCategories(getCategories(event));
			builder.setImageUrl(getImageUrl(event));
			itemList.add(builder.build());
		}
		return itemList;
	}
	
	private String getAddress(JSONObject event) throws JSONException{
		if (!event.isNull("_embedded")) {
			JSONObject embedded = event.getJSONObject("_embedded");
			if (!embedded.isNull("venues")) {
				JSONArray venues = embedded.getJSONArray("venues");
				for (int i = 0; i < venues.length(); i++) {
					JSONObject venue = venues.getJSONObject(i);
					StringBuilder addressBuilder = new StringBuilder();
					if (!venue.isNull("address")) {
						JSONObject address = venue.getJSONObject("address");
						if (!address.isNull("line1")) {
							addressBuilder.append(address.getString("line1"));
						}
						if (!address.isNull("line2")) {
							addressBuilder.append(",");
							addressBuilder.append(address.getString("line2"));
						}
						if (!address.isNull("line3")) {
							addressBuilder.append(",");
							addressBuilder.append(address.getString("line3"));
						}
					}
					if (!venue.isNull("city")) {
						JSONObject city = venue.getJSONObject("city");
						if (!city.isNull("name")) {
							addressBuilder.append(",");
							addressBuilder.append(city.getString("name"));
						}
					}
					
					// we only need the first address so if it's not null just return and don't do the loop
					String addressStr = addressBuilder.toString();
					if (!addressStr.equals("")) {
						return addressStr;
					}
				}
			}
		}
		return "";
	}
	
	private String getImageUrl(JSONObject event) throws JSONException {
		if (!event.isNull("images")) {
			JSONArray array = event.getJSONArray("images");
			for (int i = 0; i < array.length(); i++) {
				JSONObject image = array.getJSONObject(i);
				if (!image.isNull("url")) {
					return image.getString("url");
				}
			}
		}
		return "";
	}

	private Set<String> getCategories(JSONObject event) throws JSONException{
		Set<String> categories = new HashSet<>();
		if (!event.isNull("classifications")) {
			JSONArray classifications = event.getJSONArray("classifications");
			for (int i = 0; i < classifications.length(); i++) {
				JSONObject classification = classifications.getJSONObject(i);
				if (!classification.isNull("segment")) {
					JSONObject segment = classification.getJSONObject("segment");
					if (!segment.isNull("name")) {
						categories.add(segment.getString("name"));
					}
				}
			}
		}
		return categories;
	}
	
	private void queryAPI(double lat, double lon, String keyword) {
		List<Item> events = search(lat, lon, keyword);
		System.out.println(events.size());
//		for (Item event : events) {
//			System.out.println(event.toJSONObject());
//		}
	}
	
	public static void main(String[] args) {
		TicketMasterAPI tmApi = new TicketMasterAPI();
		// Mountain View, CA
		// tmApi.queryAPI(37.38, -122.08);
		// London, UK
		// tmApi.queryAPI(51.503364, -0.12);
		// Houston, TX
		tmApi.queryAPI(29.682684, -95.295410, null);
	}


}

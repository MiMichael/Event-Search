package RPC;

import entity.Item;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;

import db.*;

//import org.json.JSONException;

/**
 * Servlet implementation class SearchItem
 */
@WebServlet("/search")
public class SearchItem extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchItem() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());
		//append 只能添加字符 print可以打印很多类型
		// allow access only if session exists
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.setStatus(403);
			return;
		}
		String userId = session.getAttribute("user_id").toString();

		double lat = Double.parseDouble(request.getParameter("lat"));
		double lon = Double.parseDouble(request.getParameter("lon"));
		String term = request.getParameter("term");
		DBConnection connection = DBConnectionFactory.getConnection();
		//TicketMasterAPI tmAPI = new TicketMasterAPI();
		//List<Item> items = tmAPI.search(lat, lon, null);
		try {
			List<Item> items = connection.searchItems(lat, lon, term);
			Set<String> favoritedItemIds = connection.getFavoriteItemIds(userId);
			JSONArray array = new JSONArray();
			for (Item item : items) {
				JSONObject obj = item.toJSONObject();
				obj.put("favorite", favoritedItemIds.contains(item.getItemId()));
				array.put(obj);
			}
		RpcHelper.writeJsonArray(response, array);	
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connection.close();
		}
		
		// apache jemeter test code --- no login
//		JSONObject input = RpcHelper.readJSONObject(request);
// 		String userId;
//		try {
//			userId = input.getString("user_id");
//			double lat = Double.parseDouble(request.getParameter("lat"));
//			double lon = Double.parseDouble(request.getParameter("lon"));
//			String term = request.getParameter("term");
//			DBConnection connection = DBConnectionFactory.getConnection();
//			//TicketMasterAPI tmAPI = new TicketMasterAPI();
//			//List<Item> items = tmAPI.search(lat, lon, null);
//			try {
//				List<Item> items = connection.searchItems(lat, lon, term);
//				Set<String> favoritedItemIds = connection.getFavoriteItemIds(userId);
//				JSONArray array = new JSONArray();
//				for (Item item : items) {
//					JSONObject obj = item.toJSONObject();
//					obj.put("favorite", favoritedItemIds.contains(item.getItemId()));
//					array.put(obj);
//				}
//			RpcHelper.writeJsonArray(response, array);	
//			} catch (Exception e) {
//				e.printStackTrace();
//			} finally {
//				connection.close();
//			}
//		} catch (JSONException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}	
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

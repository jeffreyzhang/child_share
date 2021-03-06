package com.ling.child_share.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.ling.child_share.constants.Constants;
import com.ling.child_share.db.ImageDao;
import com.ling.child_share.model.Image;
import com.ling.child_share.model.User;

/**
 * Servlet implementation class ImageServlet
 */
public class ImageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ImageServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String cmd = request.getParameter("cmd");
		ImageDao imageDao = new ImageDao();
		if ("add".equals(cmd)) {
			List photos = uploadPhoto(request);
			for (Object obj : photos) {
				Image image = (Image) obj;
				imageDao.addImage(image);
			}
		} else if ("delete".equals(cmd)) {

		} else if ("query".equals(cmd)) {
			String userId = request.getParameter("userId");
			response.setCharacterEncoding("UTF-8");
			PrintWriter writer = response.getWriter();
			if (userId == null || "".equals(userId)) {
				writer.write("getPhotos({'ret':'-1', 'msg':'user not exist'});");
				return;
			}
			ResultSet rs = imageDao.getImages(userId);
			String html = "getPhotos(" + buildjson(rs) + ");";
			writer.write(html);
		} else if ("queryjson".equals(cmd)) {
			String userId = request.getParameter("userId");
			response.setCharacterEncoding("UTF-8");
			PrintWriter writer = response.getWriter();
			if (userId == null || "".equals(userId)) {
				writer.write("{'ret':'-1', 'msg':'user not exist'};");
				return;
			}
			ResultSet rs = imageDao.getImages(userId);
			String html = buildjson(rs);
			writer.write(html);
		} else if ("hot".equals(cmd)) {
			response.setCharacterEncoding("UTF-8");
			PrintWriter writer = response.getWriter();
			ResultSet rs = imageDao.getHotImages();
			ArrayList<String> users = new ArrayList<String>();
			String html = "getHotImages(" + buildjson(rs) +  ")";
			writer.write(html);
			return;
		} else if ("hotjson".equals(cmd)) {
			response.setCharacterEncoding("UTF-8");
			PrintWriter writer = response.getWriter();
			ResultSet rs = imageDao.getHotImages();
			String html = buildjson(rs);
			writer.write(html);
		}
		
	}

	private List uploadPhoto(HttpServletRequest request) {
		List<Image> result = new ArrayList<Image>();
		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		try {
		    List items = upload.parseRequest(request);
		    Iterator itr = items.iterator();
		    FileItem fileItem = null;
		    String userId = "";
		    String description = "";
		    String url = "";
		    while (itr.hasNext()) {
		        FileItem item = (FileItem) itr.next();
		        String name = item.getName();
		        if (item.isFormField()) {
		        	
		        	String fieldName = item.getFieldName();
		        	String fieldValue = URLDecoder.decode(item.getString("UTF-8"),"UTF-8");
		        	if ("description".equals(fieldName)) {
		        		description = fieldValue;
		        	}
		        	if ("userId".equals(fieldName)) {
		        		userId = fieldValue;
		        	}
		        } else if (item.getName() != null && !"".equals(item.getName())) {
	            	String filePath = request.getSession().getServletContext().getRealPath("/") + "pics" + File.separator + userId + File.separator;
	            	File f = new File(filePath);
					if (!f.exists()) f.mkdirs();
	                File file = new File(filePath + item.getName() + ".jpg");
	                url = "child_share/" + "pics" + "/" + userId + "/" + item.getName() + ".jpg";
	                item.write(file);
		        }
		    }
		    Image image = new Image();
		    image.setUser(new User(userId));
		    image.setUpload_time(new Date());
		    image.setDescription(description);
		    image.setImg_path(url);
		    result.add(image);
		} catch (Exception e) {
		    e.printStackTrace();
		}
		return result;
	}
	
	private String buildjson(ResultSet rs) {
		String html = "";
		try {
			html = "{'ret':'0', 'msg':'load success', 'data':[";
			while (rs.next()) {
				html += "{'description':'" + rs.getString("description")
						+ "', 'path':'" + Constants.PHOTO_PATH_DOMAIN + rs.getString("img_path")
						+ "', 'upload_time':'" + rs.getDate("upload_time")
						+ "'}";
				if (!rs.isLast()) {
					html += ",";
				}
			}
			html += "]}";
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return html;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}

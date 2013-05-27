package com.zedmedia.gravity;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class WebService {
	private HttpClient httpclient = null;
	private static WebService instance = null;

	private WebService() {
		httpclient = new DefaultHttpClient();
	}

	public static WebService getInstance() {
		if (instance == null) {
			instance = new WebService();
		}
		return instance;
	}

	public static WebService release() {
		if (instance == null) {
			instance = new WebService();
		}
		return instance;
	}

	public String getCredit() throws IOException {
		String result = "";
		HttpGet httpGet = new HttpGet(
				"http://dev.seeme.com:6018/credit/api/get_credit/");
		HttpResponse response = httpclient.execute(httpGet);
		// System.out.println(response.getStatusLine());
		HttpEntity entity = response.getEntity();
		result = EntityUtils.toString(entity);
		return result;
	}

	public void login(String username,String email, String password) throws IOException {
		HttpPost httpPost = new HttpPost(
				"http://dev.seeme.com:6018/credit/api/login_user/");
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("username", username));
		nvps.add(new BasicNameValuePair("email", email));		
		nvps.add(new BasicNameValuePair("password", WebService.getPass(username, email)));
		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		httpclient.execute(httpPost);

		// System.out.println(response.getStatusLine());
		// HttpEntity entity = response.getEntity();
	}

	public void createTransaction() throws IOException {
		HttpPost httpPost = new HttpPost(
				"http://dev.seeme.com:6018/credit/api/create_transaction/");
		List<NameValuePair> nvps1 = new ArrayList<NameValuePair>();
		nvps1.add(new BasicNameValuePair("amount", "3"));
		nvps1.add(new BasicNameValuePair("description", "from java"));
		nvps1.add(new BasicNameValuePair("transaction_class", "award"));
		httpPost.setEntity(new UrlEncodedFormEntity(nvps1));
		httpclient.execute(httpPost);
		// System.out.println(response.getStatusLine());
		// HttpEntity entity = response.getEntity();
	}

	static String toSHA1(byte[] convertme) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		final byte[] hash = md.digest(convertme);
		Formatter formatter = new Formatter();
		for (byte b : hash) {
			formatter.format("%02x", b);
		}
		String result = formatter.toString();
		formatter.close();
		return result;
	}

	static String getPass(String username, String email){
		String pSource = username + "gravity" + email;
		return toSHA1(pSource.getBytes()).substring(11, 19);
	}
}

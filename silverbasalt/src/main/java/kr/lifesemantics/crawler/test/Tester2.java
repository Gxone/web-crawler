package kr.lifesemantics.crawler.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tester2 {

	private static final String PATTERN = "<a.*?href=\"([http|\\/][^\"]*)\"[^>]*>(.*)<\\/a>";
	// /로 시작하는 내부 url -> key <a>태그 사이 값 -> value
	private static final String PATTERN2 = "<a.*?href=\"([\\.|\\/][^\"]*)\"[^>]*>(.*)<\\/a>";
	// http로 시작하는 외부 url -> key <a>태그 사이 값 -> value
	private static final String PATTERN3 = "<a.*?href=\"(http[^\"]*)\"[^>]*>(.*)<\\/a>";
	
	static ArrayList<String> usedList = new ArrayList<String>();
//	static Set<String> visitTemp = new HashSet<String>();
	
//	static String bodyUrl = "https://lifesemantics.kr";
	static String bodyUrl = "http://new.sungshin.ac.kr";
	
	// url, value 저장
	public Map<String, String> run(String url) {
		Map<String, String> tempMap = new HashMap<String, String>();
		String body = connectURL(url);
		if(body.length() > 0) {
			tempMap = matchPattern2(body, PATTERN2);
		}
		
		return tempMap;
	}
	
	// 방문 set 저장 후 url
	public void tt(Map<String, String> tempMap) {
		if(tempMap.size() > 0) {
			Set<String> visitT = putVisit(tempMap);
		}
		
		
	}
	
	// 방문 url set 저장
	public Set<String> putVisit(Map<String, String> map) {
		Set<String> visitTemp = new HashSet<String>();
		for(Entry<String, String> entry : map.entrySet()) {
			if(visitURL(entry.getKey())) {
				visitTemp.add(entry.getKey());
			}
		}
		
		return visitTemp;
	}
	
	//방문 url 판별
	public boolean visitURL(String url) {
		if(usedList.contains(url)) {
			return false;
		} else {
			return true;
		}
	}
	
	// url 방문
	public String connectURL(String urlPath) {

		StringBuilder textBuilder = new StringBuilder();

		String line;
		try {
			URL url = new URL(urlPath);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			InputStreamReader reader = new InputStreamReader(con.getInputStream(), "utf-8");

			BufferedReader buffer = new BufferedReader(reader);

			while ((line = buffer.readLine()) != null) {
				// pageHtml += line +"\n";
				textBuilder.append(line).append("\n");
			}

			buffer.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("error");
		}

		return textBuilder.toString();
	}
	
	//링크 가져오기
	public Map<String, String> matchPattern2(String body, String pattern) {
		Pattern hrefPat = Pattern.compile(pattern);
		Matcher hrefM = hrefPat.matcher(body);
		Map<String, String> result = new HashMap<String, String>();
		while (hrefM.find()) {

			result.put(hrefM.group(1), hrefM.group(2));
		}

		return result;
	}
}

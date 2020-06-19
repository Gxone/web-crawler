package kr.lifesemantics.crawler.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tester {
	private static final String PATTERN = "<a.*?href=\"([http|\\/][^\"]*)\"[^>]*>(.*)<\\/a>";
	// /로 시작하는 내부 url -> key <a>태그 사이 값 -> value
	private static final String PATTERN2 = "<a.*?href=\"([\\.|\\/][^\"]*)\"[^>]*>(.*)<\\/a>";
	// http로 시작하는 외부 url -> key <a>태그 사이 값 -> value
	private static final String PATTERN3 = "<a.*?href=\"(http[^\"]*)\"[^>]*>(.*)<\\/a>";

//	static String bodyUrl = "https://lifesemantics.kr";
	static String bodyUrl = "http://new.sungshin.ac.kr";
//	static String bodyUrl = "http://kormedi.com";
//	static String bodyUrl = "https://www.nate.com";
//	static String bodyUrl = "https://www.naver.com";
	static ArrayList<String> usedList = new ArrayList<String>();

	static Map<String, String> resultMap = new HashMap<String, String>(); // 최종 map
	static Map<String, String> temp = new HashMap<String, String>();

	int cnt = 0;
	int fst = 0;
	String fileName2 = "tempMap.txt";

	public void run(String url) {
		if (fst == 0 && bodyUrl.equals("http://new.sungshin.ac.kr")) {
			url += "/web";
		}
		fst++;
		System.out.println("URL : " + url);
		String body = connectURL(url);
//		System.out.println("BODY : " + body);
		temp = matchPattern2(body, PATTERN2);
		
		if (temp.size() > 0) {
			putResult(url);
			temp.clear();
//			System.out.println("TEMP : " + temp.toString());
		} else {
			System.err.println("cnt : " + cnt);
			cnt++;
		}
	}
	
	public void putResult(String url) {

//		System.out.println("temp : " + temp.size());
		System.out.println("Resultmap Size:" + resultMap.size());
		
		for (Map.Entry<String, String> entry : temp.entrySet()) {
			// key ./ 제거용
			String tt = "";
			
			ArrayList<String> path = new ArrayList<String>();
			int a = 0;
			// 포함 여부 체크
			for (int i = 0; i < usedList.size(); i++) {
				
				if (entry.getKey().equals(usedList.get(i))) {
					a++;
					break;
				}
			}
			// 방문하지 않았을 때
			if (a == 0) {
				System.out.println("PT - URL : " + url);
				tt = entry.getKey();
				// ./ 제거
				if (entry.getKey().startsWith("./")) { // ./ 또는 ../로 시작하는 URL이면
					StringTokenizer st = new StringTokenizer(url, "/");
					while (st.hasMoreTokens()) {
						path.add(st.nextToken());
					}
					
					// 마지막 경로에 파일 또는 경로가  포함되는 경우에는 파일명 또는 경로 제거
					if(!url.endsWith("/")) {
						String str = path.get(path.size() - 1);
						url = url.replaceAll(str, "");
					}
					
					// ./ 제거
					tt = tt.replaceAll("./", "");
					writeFile(fileName2, temp);
					resultMap.putAll(temp);
					usedList.add(entry.getKey());
					run(url + tt);
				} else {
					writeFile(fileName2, temp);
					resultMap.putAll(temp);
					usedList.add(entry.getKey());
					run(bodyUrl + entry.getKey());
				}

			}
		} 	
	}

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

	public ArrayList<String> matchPattern(String body, String pattern) {
		Pattern hrefPat = Pattern.compile(pattern);
		ArrayList<String> hrefResult = new ArrayList<String>();
		Matcher hrefM = hrefPat.matcher(body);

		while (hrefM.find()) {
			hrefResult.add(hrefM.group(1));
		}

		return hrefResult;
	}

	public Map<String, String> matchPattern2(String body, String pattern) {
		Pattern hrefPat = Pattern.compile(pattern);
		Matcher hrefM = hrefPat.matcher(body);
		Map<String, String> result = new HashMap<String, String>();
		while (hrefM.find()) {

			result.put(hrefM.group(1), hrefM.group(2));
		}

		return result;
	}

	public static void writeFile(String name, Map<String, String> map) {

		try {
			File file = new File(name);
			FileWriter fWriter = new FileWriter(file, true);
			BufferedWriter bWriter = new BufferedWriter(fWriter);
			for (Map.Entry<String, String> entry2 : map.entrySet()) {
				bWriter.write(entry2.getKey() + " " + entry2.getValue() + "\n");
			}
			bWriter.flush();
			bWriter.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		long t1 = System.currentTimeMillis();
		usedList.add(bodyUrl);
		Tester test = new Tester();
		test.run(bodyUrl);

		long t2 = System.currentTimeMillis();

		System.out.println("Performace : " + (t2 - t1));

		String fileName = "resultMap.txt";

		try {
			File file = new File(fileName);
			FileWriter fWriter = new FileWriter(file, false);
			BufferedWriter bWriter = new BufferedWriter(fWriter);
			for (Map.Entry<String, String> entry : resultMap.entrySet()) {
				bWriter.write(entry.getKey() + " " + entry.getValue() + "\n");
			}
			bWriter.flush();
			bWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

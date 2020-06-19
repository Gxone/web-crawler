package kr.lifesemantics.crawler.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tester3 {
	// http or /로 시작하는 url
	private static final String PATTERN = "<a.*?href=\"([http|\\/].*?[^\"])\".*?>([\\s]*?.*?)<\\/a>"; // <a.*?href=\"([http|\\/][^\"]*)\"[^>]*>(.*)<\\/a>
	// /로 시작하는 내부 url -> key <a>태그 사이 값 -> value
	private static final String PATTERN2 = "<a.*?href=\"([\\.|\\/][^\"]*)\"[^>]*>(.*)<\\/a>";
	// http로 시작하는 외부 url -> key <a>태그 사이 값 -> value
	private static final String PATTERN3 = "<a.*?href=\"(http[^\"]*)\"[^>]*>(.*)<\\/a>";
	// news url 패턴 -> 도메인 + 숫자7개
	private static final String PATTERN4 = "http:\\/\\/kormedi\\.com.*?([0-9]{7}).*";
	
	static String KEYWORD = "";
	static String seed = "http://kormedi.com";
	static ArrayList<String> usedList = new ArrayList<String>();

	static Map<String, String> resultMap = new HashMap<String, String>(); // 최종 map
	static Map<String, String> temp = new HashMap<String, String>();

	int cnt = 0;
	int fst = 0;
	String fileName2 = "tempMap.txt";

	public void run(String url) {
		if (fst == 0 && seed.equals("http://new.sungshin.ac.kr")) {
			url += "/web";
		}
		fst++;
		String body = connectUrl(url);
		temp = matchPattern2(body, PATTERN);

		if (temp.size() > 0) {
			putResult(url);
			temp.clear();
		} else {
			System.err.println("cnt : " + cnt);
			cnt++;
		}
	}

	public void putResult(String url) {
		System.out.println("Resultmap Size:" + resultMap.size());
		for (Map.Entry<String, String> entry : temp.entrySet()) {
			// key ./ 제거용
			String tt = "";
			ArrayList<String> path = new ArrayList<String>();
			int a = 0;
			// 포함 여부 체크, url에 page가 포함되었는지 체크
			for (int i = 0; i < usedList.size(); i++) {
				if (entry.getKey().equals(usedList.get(i))) {
					a++;
					break;
				}
			}
			 //healthnews와 기사만
			/*
			 * ArrayList<String> chkNewsUrl = matchPattern(entry.getKey(), PATTERN4); if
			 * (!entry.getKey().contains("healthnews/page") && chkNewsUrl.isEmpty()) { a++;
			 * }
			 */
			// healthnews카테고리 내부에서 기사만 가져오도록
			/*
			 * if (!entry.getKey().contains("healthnews/page")) { a++; }
			 */
			// 방문하지 않았을 때
			if (a == 0) {
				usedList.add(entry.getKey());
				System.out.println(entry.getKey());
				tt = entry.getKey();
				// ./ 제거
				if (entry.getKey().startsWith("./")) { // ./ 또는 ../로 시작하는 URL이면
					StringTokenizer st = new StringTokenizer(url, "/");
					while (st.hasMoreTokens()) {
						path.add(st.nextToken());
					}
					/**
					 * // 마지막 경로에 파일 또는 경로가 포함되는 경우에는 파일명 또는 경로 제거 if(!url.endsWith("/")) { String
					 * str = path.get(path.size() - 1); url = url.replaceAll(str, ""); }
					 */
					selectNews(entry.getKey(), entry.getValue());	
					tt = tt.replaceAll("./", "");
					run(url + tt);
				} else {
					if (entry.getKey().contains(seed)) { // seed로 시작
						selectNews(entry.getKey(), entry.getValue());
						run(entry.getKey());
					} else { // /로 시작
						run(seed + entry.getKey());
					}
				}
			}
		}
	}

	public String connectUrl(String urlPath) {
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
			System.out.println("URL connect error");
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
		String urlPt = "";
		String contentPt = "";
		while (hrefM.find()) {
			urlPt = hrefM.group(1).trim();
			contentPt = hrefM.group(2).trim();
			// System.out.println(contentPt);

			if (urlPt.startsWith("http") && urlPt.contains(seed)) {
				result.put(urlPt, contentPt);
			} else if (urlPt.startsWith("/")) {
				result.put(urlPt, contentPt);
			}
		}

		return result;
	}

	public void selectNews(String urlPath, String value) {
		String body = connectUrl(urlPath);
		ArrayList<String> authorPattern = matchPattern(body, "<span class=\"post-author-name\"><b>(.*?)<\\/b>");
		ArrayList<String> datePattern = matchPattern(body, "datetime=\"([0-9]*[^\\-]).*?\">");
		if (authorPattern.size() > 0 && datePattern.size() > 0 && !authorPattern.get(0).equals("코메디닷컴") && datePattern.get(0).equals("2019")) {
			System.out.println(datePattern.get(0));
			System.out.println(authorPattern.get(0));
			if (urlPath.contains(KEYWORD)) {
				resultMap.put(urlPath, value);
				insertDB(urlPath, value);
			}
		}
	}

	public static void writeFile(String name, Map<String, String> map) {
		try {
			File file = new File(name);
			FileWriter fWriter = new FileWriter(file, true); // 기존 데이터에 이어서 출력
			BufferedWriter bWriter = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
			for (Map.Entry<String, String> entry2 : map.entrySet()) {
				bWriter.write(entry2.getKey() + " " + entry2.getValue() + "\n");
			}
			bWriter.flush();
			bWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void insertDB(String key, String value) {
		PreparedStatement pstmt = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			// SSL 미사용 경고 -> ?useSSL=false 추가
			Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/crawler?useSSL=false", "root", "qweasd123");
			String sql = "INSERT INTO CrawlerTable(url, value) VALUES(?, ?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, key);
			pstmt.setString(2, value);
			int count = pstmt.executeUpdate();
			if (count == 0) {
				System.out.println("데이터 입력 실패");
			} else {
				System.out.println("데이터 입력 성공");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		long t1 = System.currentTimeMillis();
		usedList.add("http://kormedi.com/healthnews/page/0");
		Tester3 test = new Tester3();
		test.run("http://kormedi.com/healthnews/page/0"); //"http://kormedi.com/healthnews/page/0"	

		long t2 = System.currentTimeMillis();

		System.out.println("Performace : " + (t2 - t1));

		String fileName = "resultMap.txt";
		try {
			File file = new File(fileName);
			FileWriter fWriter = new FileWriter(file, false);
			BufferedWriter bWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
			for (Map.Entry<String, String> entry : resultMap.entrySet()) {
				bWriter.write(entry.getKey() + " " + entry.getValue() + "\n");
			}
			bWriter.flush();
			bWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		String fileName = "resultMap";
//		try {
//			File file = new File(fileName);
//			file.createNewFile();
//			
//			BufferedWriter bWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getPath()), "UTF-8"));
//			int y = 1;
//			for(String str : list) {
//				bWriter.write(y+": "+str + "\n");
//				y++;
//			}
//			bWriter.write("COUNT: " + list.size());
//			bWriter.flush();
//			bWriter.close();
//			System.out.println("list size : " + list.size());
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
	}
}

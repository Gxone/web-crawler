package kr.lifesemantics.crawler.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.lifesemantics.crawler.domain.ArticleDto;

public class Tester4 {
	// 건강뉴스 url 패턴 -> 도메인 + 숫자7개 , <a, title 사이 공백 1개 이상 인식 위해 .*? 
	private static final String PATTERN4 = "<a.*?title=\"(.*?)\".*?href=\"(http:\\/\\/kormedi.com\\/[0-9]{7}.*?[^\"])\".*?><\\/a>";
	
	static String seed = "http://kormedi.com";
	static List<String> list = new LinkedList<String>();
	
	int cnt = 0;
	String fileName2 = "tempMap.txt";
	
	public List<String> run(String url) {
		System.out.println(">>> URL: " + url);
		String body = connectUrl(url);
		List<String> tempList = matchPattern(body, PATTERN4);
		return tempList;
	}

	public static String connectUrl(String urlPath) {
		StringBuilder textBuilder = new StringBuilder();
		String line;
		try {
			URL url = new URL(urlPath);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			InputStreamReader reader = new InputStreamReader(con.getInputStream(), "utf-8");
			BufferedReader buffer = new BufferedReader(reader);
			while ((line = buffer.readLine()) != null) {
				textBuilder.append(line);
			}
			buffer.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("URL connect error");
		}
		return textBuilder.toString();
	}

	public List<String> matchPattern(String body, String pattern) {
		Pattern hrefPat = Pattern.compile(pattern);
		Matcher hrefM = hrefPat.matcher(body);
		Map<String, String> result = new HashMap<String, String>();
		String urlPt = "";
		List<String> list = new LinkedList<String>();
		while (hrefM.find()) {
			urlPt = hrefM.group(2).trim();
			list.add(urlPt);
		}
		return list;
	}

	public static void writeFile(String name, Map<String, String> map) {
		try {
			File file = new File(name);
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

	public static void insertDB(List<ArticleDto> articleDtos) {
		PreparedStatement pstmt = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			// SSL 미사용 경고 -> ?useSSL=false 추가
			Connection conn = DriverManager.getConnection("jdbc:mysql://172.16.0.21:3306/SilverBasalt?useSSL=false", "lifesemantics", "forhealth!");
			String sql = "INSERT INTO article(url, title, content, img_url, img_caption, issued_at) VALUES(?, ?, ?, ?, ?, ?)";
			pstmt = conn.prepareStatement(sql);
			for(ArticleDto acDto : articleDtos) {
				pstmt.setString(1, acDto.getUrl());
				pstmt.setString(2, acDto.getTitle());
				pstmt.setString(3, acDto.getContent());
				pstmt.setString(4, acDto.getImgUrl());
				pstmt.setString(5, acDto.getImgCaption());
				pstmt.setString(6, acDto.getIssuedAt());
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void makeArticleList() {
		// list[0] -> url connect
		// parser -> title, img, content, date => console 출력
		List<ArticleDto> articleDtos = new LinkedList<ArticleDto>();
		for(int i = 0; i < list.size(); i++) {
			System.out.println("PARSE NUM " + i);
			String articleUrl = list.get(i);
			ArticleDto articleDto = new ArticleDto();
			Document articleDoc = null;
			try {
				articleDoc = Jsoup.connect(list.get(i)).get();			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			String articleTitle = articleDoc.select("article span.post-title").text();
			// 기사에 이미지가 1개 이상 있을 경우 첫번째 이미지만 가져오기 위해 Elements에서 get(0)
			Elements tempArticleImg = articleDoc.select("div.entry-content img");
			// img 태그의 속성 date-src or src
			String articleImg = tempArticleImg.get(0).attr("data-src");
			if(articleImg == "") {
				articleImg = tempArticleImg.get(0).attr("src");
			}
			
			String articleImgCaption = articleDoc.select("figcaption.wp-caption-text").text();
			String articleContents = articleDoc.select("article p").text();
		    String tempDate = articleDoc.select("time.post-published").attr("datetime");
		    String articleDate = tempDate.replace("T", " ").substring(0, 19);
		    
		    articleDto.setContent(articleContents);
		    articleDto.setCreateAt(articleDate);
		    articleDto.setImgCaption(articleImgCaption);
		    articleDto.setImgUrl(articleImg);
		    articleDto.setTitle(articleTitle);
		    articleDto.setUrl(articleUrl);
		    
		    articleDtos.add(articleDto);
		}
		System.out.println("articleDtos size : " + articleDtos.size());
		insertDB(articleDtos);
	}
	
	public static void main(String[] args) {
//		long t1 = System.currentTimeMillis();
//		Tester4 test = new Tester4();
//		for (int i = 101; i <= 200; i++) {
//			List<String> tList = test.run("http://kormedi.com/healthnews/page/"+i); //"http://kormedi.com/healthnews/page/0"
//			list.addAll(tList);
//		}
//		long t2 = System.currentTimeMillis();
//		makeArticleList();
//		long t3 = System.currentTimeMillis();
//		System.out.println("url 가져오기 : " + (t2 - t1));
//		System.out.println("Dto 생성 후 저장 : " + (t3 - t2));
//		System.out.println("총 시간 : " + (t3 - t1));

		KAnalyzer kAnalyzer = new KAnalyzer();
		kAnalyzer.extractNoun();
	}
}

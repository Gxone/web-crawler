package kr.lifesemantics.crawler.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.lifesemantics.crawler.domain.ArticleDto;

/**
 *
 *
 * @author LS-COM-00044(jw.ko@lifesemantics.kr)
 *
 */
public class KAnalyzer {
	List<String> titleList = new LinkedList<String>();
	List<String> contentList = new LinkedList<String>();

	public void extractNoun() {
		Komoran komoran = new Komoran(DEFAULT_MODEL.FULL);

		for (int idx = 1; idx <= 100; idx++) {
			List<ArticleDto> articleDtos = selectDB(idx);
			for(ArticleDto articleDto : articleDtos) {
				String title = articleDto.getTitle();
				String content = articleDto.getContent();

				KomoranResult titleResults = komoran.analyze(title);
				KomoranResult contentResults = komoran.analyze(content);

				// 기사의 제목과 내용에서 각각 명사 추출
				List<String> titleResultNouns = titleResults.getNouns();
				List<String> contentResultNouns = contentResults.getNouns();

				Map<String, Integer> weightMap = new HashMap<String, Integer>();

				// 제목에 있을 경우 가중치 +2
				for(String titleNoun : titleResultNouns) {
					int weight = 2;
					if(weightMap.containsKey(titleNoun)) {
						weight =  weightMap.get(titleNoun) + 2;
					}
					weightMap.put(titleNoun, weight);
				}
				// 내용에 있을 경우 가중치 +1
				for(String contentNoun : contentResultNouns) {
					int weight = 1;
					if(weightMap.containsKey(contentNoun)) {
						weight =  weightMap.get(contentNoun) + 1;
					}
					weightMap.put(contentNoun, weight);
				}
				rankTags(idx, weightMap);
				// System.out.println(idx + " >> " + weightMap.toString());
			}
		}
	}

	public void rankTags(int idx, final Map<String, Integer> map) {
		List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(map.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				int comparision = (o1.getValue() - o2.getValue()) * -1;
				return comparision == 0 ? o1.getKey().compareTo(o2.getKey()) : comparision;
			}
		});

		// 순서유지를 위해 LinkedHashMap을 사용
		Map<String, Integer> sortedTempMap = new LinkedHashMap<String, Integer>();
		for(Iterator<Map.Entry<String, Integer>> iter = list.iterator(); iter.hasNext();){
			Map.Entry<String, Integer> entry = iter.next();
			sortedTempMap.put(entry.getKey(), entry.getValue());
		}
		// System.out.println(idx + " >> "  + sortedTempMap);

		Collection<String> k = sortedTempMap.keySet();
		Iterator<String> itr = k.iterator();
		Collection<Integer> v = sortedTempMap.values();
		Iterator<Integer> itrv = v.iterator();

		insertDB(idx, sortedTempMap);
	}

	public void insertDB(int articleIdx, Map<String, Integer> map) {
		int idx = 0;

		for(Entry<String, Integer> entry : map.entrySet()) {
			String tag = entry.getKey();
			Integer weight = entry.getValue();

			// INSERT TAG2
			PreparedStatement pstmt = null;
			try {
				Class.forName("com.mysql.jdbc.Driver");
				Connection conn = DriverManager.getConnection("jdbc:mysql://172.16.0.21:3306/SilverBasalt?useSSL=false", "lifesemantics", "forhealth!");
				String sql = "INSERT INTO tags2(article_idx, tag, weight) VALUES(?,?,?)";
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, articleIdx);
				pstmt.setString(2, tag);
				pstmt.setInt(3, weight);
				pstmt.addBatch();
				pstmt.executeBatch();
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			idx++;
			if(idx == 5) {
				break;
			}
		}
	}
	
	public List<ArticleDto> selectDB(int idx) {
		List<ArticleDto> articleDtos = new LinkedList<>();

		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = DriverManager.getConnection("jdbc:mysql://172.16.0.21:3306/SilverBasalt?useSSL=false", "lifesemantics", "forhealth!");
			String sql = "SELECT idx, title, content FROM article WHERE idx = " + idx;
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(sql);
			while(rs.next()) {
				// titleList.add(idx-1, rs.getString(1));
				// contentList.add(idx-1, rs.getString(2));
				ArticleDto articleDto = new ArticleDto();
				articleDto.setIdx(rs.getInt(1));
				articleDto.setTitle(rs.getString(2));
				articleDto.setContent(rs.getString(3));
				articleDtos.add(articleDto);
			}
			conn.close();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return articleDtos;
	}
}

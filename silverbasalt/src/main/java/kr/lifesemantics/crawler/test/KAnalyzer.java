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
	
	public void extractNoun1() {
		KomoranResult analyzeTList;
		KomoranResult analyzeCList;
		// 분석 대상 text에서 명사만 추출한 List
		List<String> analyzeTResultList;
		List<String> analyzeCResultList;
		Komoran komoran = new Komoran(DEFAULT_MODEL.FULL);
		
		for (int idx = 1; idx <= 100; idx++) {
			Map<String, Integer> weight = new HashMap<String, Integer>();
			selectDB(idx);
			// 분석할 문장
			String titleToAnalyze = titleList.get(idx-1);
			String contentToAnalyze = contentList.get(idx-1);
			// analyze()
			analyzeTList = komoran.analyze(titleToAnalyze);
			analyzeCList = komoran.analyze(contentToAnalyze);
			analyzeTResultList = analyzeTList.getNouns();
			analyzeCResultList = analyzeCList.getNouns();
			
			for (int i = 0; i < analyzeCResultList.size(); i++) {
				weight.put(analyzeCResultList.get(i), 0);
			}
			for (int i = 0; i < analyzeTResultList.size(); i++) {
				weight.put(analyzeTResultList.get(i), 0);
			}
			
			// content에 있는 단어일 경우 가중치 +1
			for (Entry<String, Integer> entry : weight.entrySet()) {
				for (int n = 0; n < analyzeCResultList.size(); n++) {
					if (entry.getKey().equals(analyzeCResultList.get(n))) {
						int temp = entry.getValue();
						temp += 1;
						entry.setValue(temp);
					}
				}
			}
			// title에 있는 단어일 경우 가중치 +2
			for (Entry<String, Integer> entry : weight.entrySet()) {
				for (int n = 0; n < analyzeTResultList.size(); n++) {
					if (entry.getKey().equals(analyzeTResultList.get(n))) {
						int temp = entry.getValue();
						temp += 2;
						entry.setValue(temp);
					}
				}
			}
			rankTags(idx, weight);
			System.out.println("idx : " + idx);
		}
	}
	
	public void extractNounByJay() {

		Komoran komoran = new Komoran(DEFAULT_MODEL.FULL);
		
		for (int idx = 1; idx <= 100; idx++) {
			List<ArticleDto> articleDtos = selectDBByJay(idx);
			for(ArticleDto articleDto : articleDtos) {
				String title = articleDto.getTitle();
				String content = articleDto.getContent();
				
				KomoranResult titleResults = komoran.analyze(title);
				KomoranResult contentResults = komoran.analyze(content);
				
				List<String> titleResultNouns = titleResults.getNouns();
				List<String> contentResultNouns = contentResults.getNouns();
				
				Map<String, Integer> weightMap = new HashMap<String, Integer>();
				for(String titleNoun : titleResultNouns) {
					int weight = 2;
					if(weightMap.containsKey(titleNoun)) {
						weight =  weightMap.get(titleNoun) + 2;
					}
					weightMap.put(titleNoun, weight);
				}
				for(String contentNoun : contentResultNouns) {
					int weight = 1;
					if(weightMap.containsKey(contentNoun)) {
						weight =  weightMap.get(contentNoun) + 1;
					}
					weightMap.put(contentNoun, weight);
				}
				rankTags(idx, weightMap);
//				System.out.println(idx + " >> " + weightMap.toString());
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
        System.out.println(idx + " >> "  + sortedTempMap);
        
        Collection<String> k = sortedTempMap.keySet();
        Iterator<String> itr = k.iterator();
        Collection<Integer> v = sortedTempMap.values();
        Iterator<Integer> itrv = v.iterator();
       
        insertDBByJay(idx, sortedTempMap);
	}
	
	public void insertDBByJay(int articleIdx, Map<String, Integer> map) {
		
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
	
	public void insertDB (int idx, Iterator<String> tags, Iterator<Integer> values) {
		 List<String> topTags = new LinkedList<String>();
	     List<Integer> topWeight = new LinkedList<Integer>();
        // 5개 
        int rmt = 0;
        	while(tags.hasNext()){
        		if(rmt == 5) {
        			break;
        		}
        		rmt++;
        		String tag = (String) tags.next();
        		topTags.add(tag);
//        		System.out.println("top" + rmt + " Tag : "+tag);
        }
        	rmt = 0;
        	while(values.hasNext()){
        		if(rmt == 5) {
        			break;
        		}
        		rmt++;
        		Integer weight = (Integer) values.next();
        		topWeight.add(weight);
//        		System.out.println("weight : "+weight);
        }
        	System.out.println(topTags + ", " + topWeight);
        	insertTag(idx, topTags,topWeight);	
	}
	
	public void insertTag(int idx, List<String> tags, List<Integer> weights) {
		PreparedStatement pstmt = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = DriverManager.getConnection("jdbc:mysql://172.16.0.21:3306/SilverBasalt?useSSL=false", "lifesemantics", "forhealth!");// SSL 誘몄궗�슜 寃쎄퀬 -> ?useSSL=false 異붽�
			String sql = "INSERT INTO tags(tag1, tag2, tag3, tag4, tag5, tag1_weight, tag2_weight, tag3_weight, tag4_weight, tag5_weight, idx) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			pstmt = conn.prepareStatement(sql);
			for(int i = 0; i < tags.size(); i++) {
				pstmt.setString(i+1, tags.get(i));
			}
			for(int i = 0; i < weights.size(); i++) {
				pstmt.setInt(i+6, weights.get(i));
			}	
			pstmt.setInt(11, idx);
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
	}

	public void selectDB(int idx) {
		PreparedStatement pstmt = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = DriverManager.getConnection("jdbc:mysql://172.16.0.21:3306/SilverBasalt?useSSL=false", "lifesemantics", "forhealth!");
			String sql = "SELECT title, content FROM article WHERE idx = " + idx;
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(sql);
			while(rs.next()) {
				titleList.add(idx-1, rs.getString(1));
				contentList.add(idx-1, rs.getString(2));
			}
			conn.close();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<ArticleDto> selectDBByJay(int idx) {
		
		List<ArticleDto> articleDtos = new LinkedList<>();
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = DriverManager.getConnection("jdbc:mysql://172.16.0.21:3306/SilverBasalt?useSSL=false", "lifesemantics", "forhealth!");
			String sql = "SELECT idx, title, content FROM article WHERE idx = " + idx;
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(sql);
			while(rs.next()) {
//				titleList.add(idx-1, rs.getString(1));
//				contentList.add(idx-1, rs.getString(2));
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


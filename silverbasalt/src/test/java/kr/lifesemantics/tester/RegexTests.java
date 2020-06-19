package kr.lifesemantics.tester;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.junit.Test;

import kr.lifesemantics.crawler.test.Tester;

public class RegexTests {

	Tester tester = new Tester();

	// a tag ~~~
	private static final String PATTERN1 = "<a.*?href=\"([http|\\/][^\"]*)\"[^>]*>(.*)<\\/a>";

	// @Test
	public void run() {
		String body = "<a href=\"https://lifesemantics.kr\" style=\"aa\"></a>";
		Map<String, String> list = tester.matchPattern2(body, PATTERN1);

		System.out.println(list.toString());

	}

	/*@Test
	public void connectionURLTest() {
		String str = tester.connectURL(
				"https://data.kma.go.kr/apiData/getData?type=xml&dataCd=ASOS&dateCd=HR&startDt=20190717&startHh=00&endDt=20190717&endHh=23&stnIds=108&schListCnt=200&pageIndex=1&apiKey=f9POsM1BAS%2Bq9IJUwWeI5hvxy088TE%2BVBCn9hWgznghe2113wdNWdQk0M4x2VkK7");
		System.out.println(str);
	}*/
	
	@Test
	public void connectionURLTest2() {
		String str2 = tester.connectURL("http://newsky2.kma.go.kr/service/SecndSrtpdFrcstInfoService2/ForecastTimeData?serviceKey=1Bp42fqQD0Z7GyrSy72v8QSY4vB3lVUejBDr8R8p%2FvV3O0hDZLhC3fzfFeWHa43vEZzi%2BJrWCyuIgtyMOnjZnA%3D%3D&base_date=20190719&base_time=0630&nx=60&ny=127&numOfRows=200&pageNo=1&_type=json");
		System.out.println("str2 : " + str2);
	}

	/*
	 * @Test public void connectionURLTest() throws Exception { String str =
	 * httpsGet(
	 * "https://data.kma.go.kr/apiData/getData?type=json&dataCd=ASOS&dateCd=HR&startDt=20190717&startHh=00&endDt=20190717&endHh=23&stnIds=108&schListCnt=200&pageIndex=1&apiKey=f9POsM1BAS%2Bq9IJUwWeI5hvxy088TE%2BVBCn9hWgznghe2113wdNWdQk0M4x2VkK7"
	 * ); System.out.println(str); }
	 * 
	 *//**
		 * https://ram2ram2.tistory.com/16 참고
		 *//*
			 * public String httpsGet(String strURL) throws Exception { URL url = new
			 * URL(strURL); // ssl 인증서 무시 ignoreSsl(); HttpsURLConnection con =
			 * (HttpsURLConnection) url.openConnection(); String ret = "";
			 * 
			 * BufferedReader br = null; br = new BufferedReader(new
			 * InputStreamReader(con.getInputStream()));
			 * 
			 * String input = null;
			 * 
			 * while ((input = br.readLine()) != null) { ret += input; }
			 * 
			 * br.close();
			 * 
			 * return ret; }
			 * 
			 * // 모든 hostname 승인 public void ignoreSsl() throws Exception { HostnameVerifier
			 * hv = new HostnameVerifier() { public boolean verify(String urlHostName,
			 * SSLSession session) { if(urlHostName.equalsIgnoreCase("data.kma.go.kr")) {
			 * return true; } return false; } }; trustAllHttpsCertificates();
			 * HttpsURLConnection.setDefaultHostnameVerifier(hv); }
			 * 
			 * private void trustAllHttpsCertificates() throws Exception { TrustManager[]
			 * trustAllCerts = new TrustManager[1]; TrustManager tm = new miTM();
			 * trustAllCerts[0] = tm; SSLContext sc = SSLContext.getInstance("SSL");
			 * sc.init(null, trustAllCerts, null);
			 * HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory()); }
			 * 
			 * static class miTM implements TrustManager, X509TrustManager { public
			 * X509Certificate[] getAcceptedIssuers() { return null; }
			 * 
			 * public boolean isServerTrusted(X509Certificate[] certs) { return true; }
			 * 
			 * public boolean isClientTrusted(X509Certificate[] certs) { return true; }
			 * 
			 * public void checkServerTrusted(X509Certificate[] certs, String authType)
			 * throws CertificateException { return; }
			 * 
			 * public void checkClientTrusted(X509Certificate[] certs, String authType)
			 * throws CertificateException { return; } }
			 */

}

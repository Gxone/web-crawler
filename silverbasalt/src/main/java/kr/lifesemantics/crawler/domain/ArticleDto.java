package kr.lifesemantics.crawler.domain;

public class ArticleDto {

	private int idx;
	private String url;
	private String title;
	private String content;
	private String imgUrl;
	private String imgCaption;
	private String issuedAt;
	private String createAt;
	private String updateAt;

	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public String getImgCaption() {
		return imgCaption;
	}

	public void setImgCaption(String imgCaption) {
		this.imgCaption = imgCaption;
	}

	public String getIssuedAt() {
		return issuedAt;
	}

	public void setIssuedAt(String issuedAt) {
		this.issuedAt = issuedAt;
	}

	public String getCreateAt() {
		return createAt;
	}

	public void setCreateAt(String createAt) {
		this.createAt = createAt;
	}

	public String getUpdateAt() {
		return updateAt;
	}

	public void setUpdateAt(String updateAt) {
		this.updateAt = updateAt;
	}

	@Override
	public String toString() {
		return "ArticleDto [idx=" + idx + ", url=" + url + ", title=" + title + ", content=" + content + ", imgUrl="
				+ imgUrl + ", imgCaption=" + imgCaption + ", issuedAt=" + issuedAt + ", createAt=" + createAt
				+ ", updateAt=" + updateAt + "]";
	}

}

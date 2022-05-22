package backendservice.entities;

import java.util.Date;
import java.util.LinkedHashMap;

import org.bson.codecs.pojo.annotations.BsonId;
import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonFormat;

public class AbstractEntity {
	@BsonId
    private String id;
	
	private String url;
     
    private String title;

	private String type;
    
    private String description;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date  createdate;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date  expiredate;
    
    private LinkedHashMap<String,String[]> categories;
         

	public String getid() {
		return id;
	}

	public void setid(String id) {
		this.id = id;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getCreateDate() {
		return createdate;
	}

	public void setCreateDate(Date createdate) {
		this.createdate = createdate;
	}

	public Date getExpireDate() {
		return expiredate;
	}

	public void setExpireDate(Date expiredate) {
		this.expiredate = expiredate;
	}

	public LinkedHashMap<String, String[]> getCategories() {
		return categories;
	}

	public void setCategories(LinkedHashMap<String, String[]> categories) {
		this.categories = categories;
	}


}

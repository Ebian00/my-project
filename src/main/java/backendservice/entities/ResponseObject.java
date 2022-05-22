package backendservice.entities;

//this entity is set for the return type to the frontend
public class ResponseObject {
	
	public ResponseObject() {
		super();
	}

	public ResponseObject(String type, Object response, String description) {
		super();
		this.type = type;
		this.response = response;
	}

	String description;
	
	String type;
	
	Object response;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Object getResponse() {
		return response;
	}

	public void setResponse(Object response) {
		this.response = response;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}

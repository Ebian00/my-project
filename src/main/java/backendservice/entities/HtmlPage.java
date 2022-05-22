package backendservice.entities;

public class HtmlPage extends AbstractEntity  {
	
    private String htmlElement;
    private String selector;

	
	public String getHtmlElement() {
		return htmlElement;
	}

	public void setHtmlElement(String htmlElement) {
		this.htmlElement = htmlElement;
	}

	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}
}

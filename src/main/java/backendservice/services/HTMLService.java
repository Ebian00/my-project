package backendservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.jsoup.select.Elements;
import backendservice.entities.HtmlPage;
import backendservice.repositories.HTMLRepository;

import java.io.File;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
@Service
public class HTMLService {
	
	private final Logger logger = Logger.getLogger(this.getClass().getName());

	@Autowired
	private HTMLRepository htmlRep;
	public ResponseEntity<String> saveElement(HtmlPage htmlPage) {
		htmlRep.insert(this.getElement(htmlPage));
		logger.log(Level.INFO, "Html element has been successfully saved" );
		return new ResponseEntity<>(
			      "Html element has been successfully saved",HttpStatus.OK);
	}
	
	public HtmlPage getHTML(String id) {
		logger.log(Level.INFO, "Html element has been successfully extracted from DB" );
		return htmlRep.findById(id).get();
	}
	
	
	private HtmlPage getElement(HtmlPage htmlPage)  {
			Elements elementsOnHTMLPage = new Elements();
			try {
				Document document ;
				//this code is for a page on Internet
				if(htmlPage.getUrl().contains("http")) {
				 document = Jsoup.connect(htmlPage.getUrl()).get();

				}				
				//on a local hard drive the method is as follows
				else {
					File file = new File(htmlPage.getUrl());
				   document = Jsoup.parse(file, "UTF-8");
				}
				
			if(htmlPage.getSelector().isEmpty()) {
				logger.log(Level.SEVERE,"No selector was set, thus selecting div");
				 elementsOnHTMLPage = document.select("div");
			}
			 elementsOnHTMLPage = document.select(htmlPage.getSelector());
			}
			catch(Exception e){
				System.out.println(e);
				logger.log(Level.SEVERE, "Html could not be read from the url" );
			}
			if (elementsOnHTMLPage.hasText()) {
				htmlPage.setHtmlElement(elementsOnHTMLPage.toString());
				}
		return htmlPage;
	}

}

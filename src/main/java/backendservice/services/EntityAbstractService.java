package backendservice.services;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import backendservice.entities.AbstractEntity;
import backendservice.entities.HtmlPage;
import backendservice.entities.Image;
import backendservice.entities.Pdf;
import backendservice.entities.ResponseObject;
import backendservice.repositories.CustomElementRepository;

@Service
//this service saves all kind of contents into the DB, a so called abstract service
public class EntityAbstractService {

	@Autowired
	private CustomElementRepository entityRep ;
	@Autowired private HTMLService htmlService;
	
	@Autowired private ImageService imageService;
	
	@Autowired private PDFService pdfService;
	private final Logger logger = Logger.getLogger(this.getClass().getName());


	public ResponseEntity<String> saveElement(AbstractEntity entityAbstract) {
		if(entityAbstract.getType().equals("image")) {
			imageService.saveImage((Image) entityAbstract, "");
			 logger.log(Level.INFO,"The image has been successfully saved");
			 return new ResponseEntity<>(
				      "The image has been successfully saved",HttpStatus.OK);
		}
		else if(entityAbstract.getType().equals("html")) {
			htmlService.saveElement((HtmlPage) entityAbstract);
			 logger.log(Level.INFO, "The Html element has been successfully saved");
			 return new ResponseEntity<>(
				      "The Html element has been successfully saved",HttpStatus.OK);
		}
		else if(entityAbstract.getType().equals("pdf")) {
			pdfService.savePDF( (Pdf) entityAbstract,"");
			 logger.log(Level.INFO, "The Pdf file has been successfully saved");
			 return new ResponseEntity<>(
				      "The Pdf file has been successfully saved",HttpStatus.OK);
		}
		else {
			 logger.log(Level.WARNING,"The type : " + entityAbstract.getType() + " is not supported");
			 return new ResponseEntity<>(
				      "The type : " + entityAbstract.getType() + " is not supported",HttpStatus.BAD_REQUEST);
		}
		
	}
	
	public ResponseObject getEntity(String id) {
		ResponseObject entity = entityRep.getEntity(id);
		return entity;
	}}



package backendservice;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import backendservice.entities.HtmlPage;
import backendservice.entities.Image;
import backendservice.entities.Pdf;
import backendservice.entities.ResponseObject;
import backendservice.services.EntityAbstractService;
import backendservice.services.HTMLService;
import backendservice.services.ImageService;
import backendservice.services.PDFService;

@CrossOrigin(origins ="https://127.0.0.1:5501",allowedHeaders = "*" )
@RequestMapping("/caas")
@RestController
public class ApplicationController {



	private final Logger logger = Logger.getLogger(this.getClass().getName());

	@Autowired
	private HTMLService htmlService;

	@Autowired
	private ImageService imageService;

	@Autowired
	private PDFService pdfService;

	@Autowired
	private EntityAbstractService entityService;

	@PostMapping(value = "/saveImage", consumes = "application/json")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<String> saveImage(@RequestBody String json) {
		logger.log(Level.INFO, "trying to save an image in DB");
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		Image image = null;
		// mapping the json object to an Image entity
		try {
			image = objectMapper.readValue(json, Image.class);
		} catch (Exception e) {
			logger.log(Level.WARNING, "The Json could not be mapped to an image entity");
			return new ResponseEntity<>("The Json syntax is not valid", HttpStatus.BAD_REQUEST);
		}
		return imageService.saveImage(image, json);

	}

	// this method return the image entity that is smaller than 16MB
	@GetMapping("/getImage/{id}")
	@PreAuthorize("hasRole('USER')")
	public Image getImage(@PathVariable String id) {
		Image image = new Image();
		image = imageService.getImage(id);
		if (image == null)
			logger.log(Level.INFO, "The id" + id + " does not exists");
		else
			logger.log(Level.INFO, "The image has been extraceted form the DB");
		return image;

	}

	@PostMapping(value = "/saveHtml", consumes = "application/json")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<String> saveHTML(@RequestBody String json) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		HtmlPage element = null;
		try {
			element = objectMapper.readValue(json, HtmlPage.class);
			logger.log(Level.INFO, "The Json Html has been successfully  mapped");
			return htmlService.saveElement(element);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "The Json object could not be mapped, or dublicated ID");
			return new ResponseEntity<>("The Json object could not be mapped", HttpStatus.BAD_REQUEST);
		}

	}

	// this method return any entity with a given ID if it exist in the DB
	@GetMapping("/getEntity/{id}")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<ResponseObject> getEntity(@PathVariable String id) {
		ResponseObject responseObject = entityService.getEntity(id);
		HttpHeaders responseHeaders = new HttpHeaders();
		return ResponseEntity.ok().body(responseObject);
	}

	@GetMapping("/getHTML/{id}")
	@PreAuthorize("hasRole('USER')")
	public HtmlPage getHTML(@PathVariable String id) {
		return htmlService.getHTML(id);
	}

	@PostMapping(value = "/savePdf", consumes = "application/json")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<String> savePDF(@RequestBody String json) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		Pdf pdfFile = null;
		// mapping the JSON object to a Pdf object
		try {
			pdfFile = objectMapper.readValue(json, Pdf.class);
			logger.log(Level.INFO, "The Json Pdf has been successfully  mapped");
			return pdfService.savePDF(pdfFile, json);
		} catch (IOException e) {
			logger.log(Level.INFO, "The Json Pdf cound not be mapped");
			return new ResponseEntity<>("The Json object could not be mapped", HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/getPdfStream/{id}/{titel}")
	@PreAuthorize("hasRole('USER')")
	// returns a PDF as an input stream
	public ResponseEntity<InputStreamResource> getPdfStream(@PathVariable String id, @PathVariable String titel) {

		InputStream is = pdfService.getPDFFileAsStream(titel, id);
		HttpHeaders responseHeaders = new HttpHeaders();
		InputStreamResource inputStreamResource = new InputStreamResource(is);
		responseHeaders.setContentType(MediaType.valueOf("application/pdf"));
		// just in case you need to support browsers
		return new ResponseEntity<InputStreamResource>(inputStreamResource, responseHeaders, HttpStatus.OK);

	}

	@GetMapping("/getPdfByte/{id}")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<Object> getPdfByte(@PathVariable String id) {

		byte[] pdfByte = pdfService.getPDFFileAsBytes(id);
		HttpHeaders responseHeaders = new HttpHeaders();
		// responseHeaders.setContentType(MediaType.valueOf("application/pdf"));
		ByteArrayResource resource = new ByteArrayResource(pdfByte);
		// just in case you need to support browsers
		return ResponseEntity.ok().contentLength(pdfByte.length).contentType(MediaType.APPLICATION_PDF).body(pdfByte);

	}
}

package backendservice.repositories;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Repository;

import com.google.common.io.ByteStreams;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;

import backendservice.entities.HtmlPage;
import backendservice.entities.Image;
import backendservice.entities.Pdf;
import backendservice.entities.ResponseObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
//this custom repository connects to a MongoDB and saves and extracts contents for this we need the properties from the 
//application.properties and the other repositories
public class CustomElementRepository {

	private final Logger logger = Logger.getLogger(this.getClass().getName());
	//initializing the MongoDB with the configuration in the application.properties file
	@Value("${spring.data.mongodb.database}")
	private String db;
	//initializing the MongoDB with the configuration in the application.properties file
	@Value("${spring.data.mongodb.host}")
	private String host;
	//initializing the MongoDB with the configuration in the application.properties file
	@Value("${spring.data.mongodb.port}")
	private String port;
	
	//we need the other repositories in case we have an html content or image with the size < 16MB
	@Autowired
	private ImageRespository imageRepo;
	@Autowired
	private HTMLRepository htmlRepo;
	@Autowired
	private PDFRepository pdfRepo;
	
	//this function gets an id and initializes the MongoDB and registers the entites in the codec provider and searchs in 
	//the DB after the content with the given ID
	public ResponseObject getEntity(String uuid) {

		logger.log(Level.INFO, "Trying to get an object with the id =" + uuid + "from the MongoDB");

		//we do registert our entities
		CodecProvider pojoCodecProvider = PojoCodecProvider.builder().register("backendservice.entities").build();
		CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(),
				fromProviders(PojoCodecProvider.builder().automatic(true).build()));
		//initializing the DB
		MongoClient mongoClient = new MongoClient(host,
				MongoClientOptions.builder().codecRegistry(pojoCodecRegistry).build());
		
		// we get our Dataset of the MongoDB
		MongoDatabase mongoDB = mongoClient.getDatabase(db);

		MongoIterable<String> collections = mongoDB.listCollectionNames();
		Document document = null;
		//the content find, if any, is saved in the responseObject entity
		ResponseObject responseObject = new ResponseObject();
		/* the for loop, iterates over our collections in Mongo and searches for the content with the ID giving
		* this process makes since if we do not know in which collection the data is stored, in case we know the collection
		where the data is stored, it is faster to ask straight the collection for the content
		*/
		for (String collectionsName : collections) {
			//there are chunk collections created for those contents > 16MB so we do not need to them
			if (!collectionsName.contains("chunk")) {
//				System.out.println(collectionsName);
				//initializing the GridFS
				GridFSBucket gridFSBucket;
				GridFSFindIterable content;
				
				if (collectionsName.equals("image")) {
					/*if the collection name is image, first we try to get the content form the image repository
					 * if the repository return does return the content, we map it to the responseObject
					*/
					Image image =  imageRepo.findById(uuid).orElse(null);
					if(!(image == null)) {
						 responseObject.setType(image.getType());
						 responseObject.setResponse(image.getImage());
						 return responseObject;
					}
				} 

				else if (collectionsName.equals("pdf")) {
					/*if the collection name is image, first we try to get the content form the image repository
					 * if the repository return does return the content, we map it to the responseObject
					*/
					Pdf pdf =  pdfRepo.findById(uuid).orElse(null);
					if(!(pdf == null)) {
						 responseObject.setType(pdf.getType());
						 responseObject.setResponse(pdf.getPdfFile());
						 return responseObject;
					}
				} 
				else if ( collectionsName.equals("htmlPage")) {
					/*if the collection name is htmlPage, first we try to get the content form the html repository
					 * if the repository return does return the content, we map it to the responseObject
					*/
					HtmlPage htmlpage =  htmlRepo.findById(uuid).orElse(null);
					if(!(htmlpage == null)) {
						 responseObject.setType(htmlpage.getType());
						 responseObject.setResponse(htmlpage.getHtmlElement());
						 return responseObject;
					}
				}
				else if ( collectionsName.equals("largePdf.files")) {
					/*if the collection name is largePdf, first we create a GridBucket to get the content form the Pdf directly form the 
					 * MongoDB
					 * the point about this collection is that the id is saved in the metadata of the object in the DB so 
					 * we have to use a filter in order to get the content
					 * if the repository return does return the content, we map it to the responseObject
					*/
					gridFSBucket = GridFSBuckets.create(mongoDB, "largePdf");
					content = gridFSBucket.find(Filters.eq("metadata.id", uuid));

					GridFSFile gridFile = content.first();
					try {
						if (!(gridFile == null)) {

							GridFSDownloadStream gfsFile = gridFSBucket.openDownloadStream(gridFile.getId());
							InputStream is = gfsFile;
							try {
								//since the Downloadstream return a stream, we need to convert the stream to byte array
								//and map it to  responseObject
								responseObject.setResponse(ByteStreams.toByteArray(is));
								responseObject.setType(gridFile.getMetadata().getString("type"));
							} catch (IOException e) {
								e.printStackTrace();
							}
							return responseObject;

						}
					} finally {

					}
				}else {
					/*if the collection name is largeImage, first we create a GridBucket to get the content form the largeImage directly form 
					 * MongoDB
					 * the point about this collection is that the id is saved in the metadata of the object in the DB so 
					 * we have to use a filter in order to get the content
					 * if the repository return does return the content, we map it to the responseObject
					*/
					gridFSBucket = GridFSBuckets.create(mongoDB, "largeImage");
					content = gridFSBucket.find(Filters.eq("metadata.id", uuid));

					GridFSFile gridFile = content.first();
					try {
						if (!(gridFile == null)) {

							GridFSDownloadStream gfsFile = gridFSBucket.openDownloadStream(gridFile.getId());
							InputStream is = gfsFile;
							try {
								//since the Downloadstream return a stream, we need to convert the stream to byte array
								//and map it to  responseObject
								responseObject.setResponse(ByteStreams.toByteArray(is));
								responseObject.setType(gridFile.getMetadata().getString("type"));
							} catch (IOException e) {
								e.printStackTrace();
							}
							return responseObject;

						}
					} finally {

					}
				}
			}
		}

		return responseObject;

	}
}

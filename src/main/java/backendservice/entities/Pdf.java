package backendservice.entities;

import java.io.File;

import org.bson.types.Binary;

public class Pdf  extends AbstractEntity{
	
         
    private byte[] pdfFile;

	public byte[] getPdfFile() {
		return pdfFile;
	}

	public void setPdfFile(byte[] binary) {
		this.pdfFile = binary;
	}
    
    
}

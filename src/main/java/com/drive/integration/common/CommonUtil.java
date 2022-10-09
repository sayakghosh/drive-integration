package com.drive.integration.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.springframework.stereotype.Component;

import com.drive.integration.model.FileMetadata;
import com.google.api.services.drive.Drive;
import com.google.gson.Gson;

@Component
public class CommonUtil {

	public void writeMetadataListIntoFile(List<FileMetadata> fileMetadataList) {
		try {
			File myObj = new File(Constant.METADATA_FILE_LOCATION);
			FileWriter fr = new FileWriter(myObj, true);

			//Create a new file if it does not exist
			if (myObj.createNewFile()) {
				System.out.println("\nJSON File created: " + myObj.getName());
			} else {
				System.out.println("\nJSON file already exists.");
			}

			System.out.println("Writing the metadata JSON into file.");
			//Write JSON metadata list to a file
			fileMetadataList.forEach(fileMetadata -> writeJsonIntoFile(fr, fileMetadata));

			fr.close();
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}

	public void writeJsonIntoFile(FileWriter fr, FileMetadata fileMetadata) {

		try {
			//Write each JSON object to the file
			fr.write(new Gson().toJson(fileMetadata) + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void downloadFileIntoLocal(Drive service, FileMetadata fileMetadata) throws IOException {

		System.out.println("\nStarting download of file: " + fileMetadata.getFilename());
		//Create empty file to be downloaded.
		OutputStream outputStream = new FileOutputStream(Constant.FILES_DOWNLOAD_LOCATION + fileMetadata.getFilename());  

		//Download the given file.
		service.files().get(fileMetadata.getId())
		.executeMediaAndDownloadTo(outputStream);

//		ByteArrayOutputStream byteOutStream = null;  
//		try {  	   
//			byteOutStream = new ByteArrayOutputStream();    
//			byteOutStream.writeTo(outputStream);  
//		} catch (IOException e) {  
//			e.printStackTrace();  
//		} finally {  
//			outputStream.close();  
//		}
		outputStream.close(); 
		System.out.println("Completed download of file: " + fileMetadata.getFilename());
	}

}

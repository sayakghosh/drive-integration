package com.drive.integration.common;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Component;

import com.drive.integration.model.FileMetadata;
import com.google.gson.Gson;

@Component
public class CommonUtil {
	
	public void writeIntoFile(List<FileMetadata> fileMetadataList){
		try {
			File myObj = new File(Constant.DEFAULT_FILE_LOCATION);
			FileWriter fr = new FileWriter(myObj, true);
			
			if (myObj.createNewFile()) {
				System.out.println("File created: " + myObj.getName());
			} else {
				System.out.println("File already exists.");
			}
			
			System.out.println("Writing the metadata JSON into file.");
			fileMetadataList.forEach(fileMetadata -> writeJsonIntoFile(fr, fileMetadata));
			
			fr.close();
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}
	
	public void writeJsonIntoFile(FileWriter fr, FileMetadata fileMetadata) {
		
		try {
			fr.write(new Gson().toJson(fileMetadata));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

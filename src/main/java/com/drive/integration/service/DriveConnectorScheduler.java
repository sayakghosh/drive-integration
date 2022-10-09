package com.drive.integration.service;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.drive.integration.client.GoogleDriveCredentials;
import com.drive.integration.common.Constant;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;

@Service
public class DriveConnectorScheduler {
	
	@Autowired
	GoogleDriveCredentials googleDriveCredentials;
	
	@Autowired
	IAllDocumentListService allDocumentListService;
	
	@Autowired
	INewEventListenerService newEventListenerService;
	
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	
	private static final String APPLICATION_NAME = "Oslash Test Project";
	
	@Scheduled(fixedDelay = Constant.DEFAULT_FIXED_DELAY)
	public void fetchAllFiles() throws IOException, GeneralSecurityException {
		
		// Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, googleDriveCredentials.getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        
        //Fetch all existing documents in Google Drive
        allDocumentListService.fetchAllDocuments(service);
        
        //New events listener method
        newEventListenerService.newFileEventProcessor(service);
	}
}

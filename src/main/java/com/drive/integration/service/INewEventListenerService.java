package com.drive.integration.service;

import java.io.IOException;

import com.google.api.services.drive.Drive;

public interface INewEventListenerService {

	public void newFileEventProcessor(Drive service) throws IOException;
}

# DRIVE-INTEGRATION

## APPROACH

- As the application starts, all the files already existing in the Google Drive are fetched and printed in the console.
- After all the existing files are fetched, the listener is started.
- The listener listens for any file changes in the Drive.
- Once a change is found, the file's metadata is stored in an ArrayList.
- The metadata of the new files are accumulated till they reach a configured threshold(new.file.events.batch.size).
- Once the threshold is reached, all the accumulated metadata is written into a file(location configurable). The files are then downloaded into a configured folder aswell.

## FUTURE IMPROVEMENTS

- Add support for multiple JVMs.
- Add support for application to resume from last checkpoint. This is to be done to minimize the impact of a JVM crash.
- Integrate with Google OAuth to add support without using credentials.json file.

## STEPS TO RUN THE APPLICATION LOCALLY

- Clone the repository.
- Follow the pre-requisite steps for accessing Google Drive APIs. URL below:
 ```bash
   https://developers.google.com/drive/api/quickstart/java
 ```
 - Place the `credentials.json` file, generated after following the above link, in the `src/main/resources/` folder. 
 - A Gradle refresh is required.
 - Update the `application.properties` file with the values as required.
 ```properties
all.file.fetch.batch.size=1000

new.file.events.batch.size=10
 ```
 - Update the METADATA_FILE_LOCATION and FILES_DOWNLOAD_LOCATION in Constant.java, if required.
 - Run as a Java/SpringBoot Application.

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package elius.virtualoperator.task.job.log;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import elius.virtualoperator.task.job.Job;
import elius.virtualoperator.task.job.JobAttributes;
import elius.virtualoperator.task.job.JobStep;
import elius.webapp.framework.properties.PropertiesManager;
import elius.webapp.framework.properties.PropertiesManagerFactory;

/**
 * JobLogFile.
 * <p>
 * Job log for scripts or file in general.
 */
public class JobLogFile extends JobLog {
	
	/** Get logger */
	protected static Logger logger = LogManager.getLogger(JobLogFile.class);	

	// Properties file
	private final PropertiesManager appProperties;
	
	// Path where find logs
	private String logsPath;
	
	// Filename mask
	private String mask;
	
	// Max log size (bytes)
	private long maxLogSize;

	// Job step
	private final JobStep jobStep;
	
	/**
	 * Constructor
	 * @param job Job to fetch
	 */
	public JobLogFile(Job job) {
		super(job);
		
		// Application properties
		appProperties = PropertiesManagerFactory.getInstance(JobAttributes.DEFAULT_JOB_PROPERTIES_FILE);
		
		// Job step
		jobStep = new JobStep();
	}

	
	/**
	 * Initialize
	 * @return 0 OK, otherwise error
	 */
	@Override
	public int initialize() {
		
		// Verify that properties are loaded
		if(!appProperties.isLoaded())
			return -1;
		
		// Path where find logs
		logsPath = appProperties.get(JobAttributes.PROP_TASK_JOB_LOG_FILE_PATH, JobAttributes.DEFAULT_TASK_JOB_LOG_FILE_PATH);
		
		// Filename mask
		mask = appProperties.get(JobAttributes.PROP_TASK_JOB_LOG_FILE_MASK, JobAttributes.DEFAULT_TASK_JOB_LOG_FILE_MASK);
		
		// Get Log max size to prevent overload
		maxLogSize = appProperties.getLong(JobAttributes.PROP_TASK_JOB_LOG_FILE_MAXSIZE, JobAttributes.DEFAULT_TASK_JOB_LOG_FILE_MAXSIZE);
			
		return 0;
	}

	
	/**
	 * Fetch log
	 * @return 0 OK, otherwise error
	 */
	@Override
	public int fetch() {
		
		// Get real file name
		String fileName = getLogFileName();

		// Create File object with logs path and file name
		File file = new File(logsPath, fileName);
		
		// Check if file exist
		if (!file.exists()) {
			
        	// Log the error
        	logger.error("Job filename={} not found", fileName);
        	// Return error
        	return 1;
			
		}

        // Get file size
        long fileSize = file.length();
        
        // Log file size
        logger.debug("File size={} bytes", fileSize);
        
        
        // Verify if the file is too big and prevent an overload
        if(fileSize > maxLogSize) {
        	
        	// Log the error
        	logger.error("Log file is too big (bytes): file={} limit={}", fileSize, maxLogSize);
        	// Return error
        	return 2;
        	
        } 


        // Set complete filename path
    	Path path = Paths.get(job.getUrl());
    	
		try {
	        // Create job step
	        // Scripts or daemon output in general has one step

			// Set content
			jobStep.setLog(Files.readAllLines(path, StandardCharsets.UTF_8));

			// Set the number of lines readed
	        jobStep.setnLines(jobStep.getLog().size());

	        // Because mono-step, set the same job information
	        // Name
	        jobStep.setName(job.getName());
	        // Return code
	        jobStep.setRc(job.getRc());
	        // Log URL
	        jobStep.setUrl(job.getUrl());
			
		} catch (IOException e) {
			
			// Log error
			logger.error("Error reading log={}",job.getUrl(), e);
			
			// Return error
			return 1;
		}
	
		// Return OK
		return 0;
	}
	
	
	
	/**
	 * Retrieve real job log filename using job info, mask and path
	 * @return Blank in case of errors, otherwise the filename
	 */
	public String getLogFileName() {
		
		// Initialize final path with log mask
		String fileName = mask;

		// Replace name
		fileName = fileName.replace("<name>", job.getName());
		
		// Replace id
		fileName = fileName.replace("<id>", job.getId());
		
		// Replace return code
		fileName = fileName.replace("<rc>", job.getRc());

		// Replace path
		fileName = fileName.replace("<url>", job.getUrl());
		
		// Replace start time
		if (job.getStarted() != null) {
			LocalDateTime startTime = LocalDateTime.ofInstant(
					job.getStarted().toInstant(), 
					ZoneId.systemDefault()
			);

			fileName = fileName
				.replace("<st-yyyy>", DateTimeFormatter.ofPattern("yyyy").format(startTime))
				.replace("<st-MM>",   DateTimeFormatter.ofPattern("MM").format(startTime))
				.replace("<st-dd>",   DateTimeFormatter.ofPattern("dd").format(startTime))
				.replace("<st-HH>",   DateTimeFormatter.ofPattern("HH").format(startTime))
				.replace("<st-mm>",   DateTimeFormatter.ofPattern("mm").format(startTime))
				.replace("<st-ss>",   DateTimeFormatter.ofPattern("ss").format(startTime));
		}	
		
		// Replace end time
		if (job.getEnded() != null) {
			LocalDateTime endTime = LocalDateTime.ofInstant(
					job.getEnded().toInstant(), 
					ZoneId.systemDefault()
			);

			fileName = fileName
				.replace("<et-yyyy>", DateTimeFormatter.ofPattern("yyyy").format(endTime))
				.replace("<et-MM>",   DateTimeFormatter.ofPattern("MM").format(endTime))
				.replace("<et-dd>",   DateTimeFormatter.ofPattern("dd").format(endTime))
				.replace("<et-HH>",   DateTimeFormatter.ofPattern("HH").format(endTime))
				.replace("<et-mm>",   DateTimeFormatter.ofPattern("mm").format(endTime))
				.replace("<et-ss>",   DateTimeFormatter.ofPattern("ss").format(endTime));
		}

		logger.traceExit("Job log fileName={}", fileName);
		
		// Return the real job log file path
		return fileName;
		
	}

	
	
	/**
	 * Get job step for selected job
	 * @return JobStep with all information related to the selected job
	 */
	public JobStep getJobStep() {
		return jobStep;
	}

}

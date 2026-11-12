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

import java.util.List;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import elius.virtualoperator.task.job.Job;
import elius.virtualoperator.task.job.JobAttributes;
import elius.virtualoperator.task.job.JobStep;
import elius.webapp.framework.properties.PropertiesManager;
import elius.webapp.framework.properties.PropertiesManagerFactory;
import elius.webapp.framework.security.secret.SecretCredentials;
import elius.webapp.framework.http.HttpConnection;


/**
 * JobLogBetaSystems.
 * <p>
 * Job log for BetaSystems.
 */
public class JobLogBetaSystems extends JobLog {
	
	/** Get logger */
	protected static Logger logger = LogManager.getLogger(JobLogBetaSystems.class);	

	// Properties file
	private final PropertiesManager appProperties;
	
	// Base URI
	private String baseUri;
	
	// Select URI
	private String selectUri;
	
	// HTTP Connection 
	private final HttpConnection httpConnection;
	
	// HTTP Credentials
	private final SecretCredentials httpCredentials;
	
	// Step lines
	List<String> jobLogList;

	// Tags
	private static String BETA_RETURN = "a4dp:return";
	private static String BETA_LINE = "a4dp:line";
	private static String BETA_ROW = "a4dp:row";
	private static String BETA_COL = "a4dp:col";
	private static String BETA_PAGE = "a4dp:page";
	private static String BETA_CONTENT = "content";
	private static String BETA_RESULT_TABLE = "RESULT_TABLE";
	
	
	/**
	 * Constructor
	 * @param job Job to fetch
	 */
	public JobLogBetaSystems(Job job) {
		super(job);
		
		// Application properties
		appProperties = PropertiesManagerFactory.getInstance(JobAttributes.DEFAULT_JOB_PROPERTIES_FILE);
		
		// Job step
		jobSteps = new ArrayList<JobStep>();
		
		// HTTP Connection 
		httpConnection = new HttpConnection();
		
		// HTTP Credentials
		httpCredentials = new SecretCredentials();
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
		
		// Base URI
		baseUri = appProperties.get(JobAttributes.PROP_TASK_JOB_LOG_BETASYSTEMS_BASE_URI);
		
		// Select URI
		selectUri = appProperties.get(JobAttributes.PROP_TASK_JOB_LOG_BETASYSTEMS_SELECT_URI);
		
		// UserId
		httpCredentials.setUserId(appProperties.get(JobAttributes.PROP_TASK_JOB_LOG_BETASYSTEMS_USERID));
		
		// Password
		httpCredentials.setPassword(appProperties.get(JobAttributes.PROP_TASK_JOB_LOG_BETASYSTEMS_PASSWORD));
		
		return 0;
	}

	
	/**
	 * Fetch log from BetaSystems Control
	 * @return 0 OK, otherwise error
	 */
	@Override
	public int fetch() {
		
		// Clear previous data
		jobSteps.clear();
		
		
		// Get job URI
		String jobUri = getJobUri();
		
		// Check for errors
		if("".equals(jobUri)) {
			logger.error("Error getting job URI for {}", job);
			// Return error
			return 1;
		}
		
		// Get Steps
		if(0 != getJobStepsInfo(jobUri)) {
			logger.error("Error getting step info for {}", job);
			
			// Return error
			return 2;
		}
		
		return 0;
	}
	

	/**
	 * Get the steps of the selected jobs
	 * @return The list of job steps
	 */
	public List<JobStep> getJobSteps() {
		return jobSteps;
	}
	
	
	/**
	 * Get the job URI from BetaSystems Control
	 * @return The URI of job inside the BetaSystems Control
	 */
	private String getJobUri() {
		
		// Initialize job URI
		String jobUri = "";	

		// Convert timestamps using modern Java Time API
        String dateFrom = DateTimeFormatter.ISO_LOCAL_DATE.format(
            job.getStarted().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        );
        String dateTo = DateTimeFormatter.ISO_LOCAL_DATE.format(
            job.getEnded().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        );
        
        // Prepare URI safely using modern URLEncoder (no try-catch needed)
        String uri = selectUri + "?p:mode=save"
            + "&p:fromdate=" + URLEncoder.encode(dateFrom, StandardCharsets.UTF_8)
            + "&p:fromtime="
            + "&p:todate=" + URLEncoder.encode(dateTo, StandardCharsets.UTF_8)
            + "&p:totime="
            + "&p:jobname=" + URLEncoder.encode(job.getName(), StandardCharsets.UTF_8)
            + "&p:jobid=" + URLEncoder.encode(job.getId(), StandardCharsets.UTF_8)
            + "&p:joberrt="
            + "&p:jobqueue=ALL"
            + "&p:jobnetid="
            + "&p:ljobname="
            + "&p:hostname="
            + "&format=json";

        // Get job info
        if (0 != httpConnection.get(uri, httpCredentials)) {
            return jobUri;
        }

		
		// Get job info
		if(0 != httpConnection.get(uri, httpCredentials))
			return jobUri;	
		
		try {
			// Parse JSON from text to object
			JSONObject jo = new JSONObject(httpConnection.getResponseContent());
		
			// Get the item with the job info	
			JSONObject a4dp = jo.getJSONObject(BETA_RETURN).getJSONObject(BETA_RESULT_TABLE).getJSONObject(BETA_ROW);
			
			// Get job details
			JSONArray ja = a4dp.getJSONArray(BETA_COL);
			
			// Read details
			for (int i = 0; i < ja.length(); i++) {
				
				// Get URI if exist
				if("URI".equals(ja.getJSONObject(i).get("name"))) {
					
					// Set final URI
					jobUri = baseUri + ja.getJSONObject(i).get(BETA_CONTENT).toString() + "&format=json";
					
				}
				
			}	
		} catch (JSONException e) {
			// Return error 
			return "";
		}

		logger.traceExit("Job Uri={}", jobUri);

		// Return job URI
		return jobUri;
	}
	
		
	/**
	 * Return the job steps list information.
	 * 
	 * @param jobUri The job URI
	 * @return 0 OK, otherwise the error code
	 */
	private int getJobStepsInfo(String jobUri) {
		// Get job info
		if (0 != httpConnection.get(jobUri, httpCredentials)) {
			return 1;
		}
		
		try {
			JSONObject jo = new JSONObject(httpConnection.getResponseContent());
			JSONObject a4dp = jo.getJSONObject(BETA_RETURN).getJSONObject("JOB_LOGTYPES");
			JSONArray ja = a4dp.getJSONArray(BETA_ROW);
			
			for (int i = 0; i < ja.length(); i++) {
				JSONObject rowObj = ja.getJSONObject(i);
				JobStep js = parseJobStep(rowObj);
				jobSteps.add(js);
			}
		} catch (JSONException e) {
			logger.error("Error parsing job steps JSON", e);
			return 1;
		}
		
		return 0;
	}

	/**
	 * Parses a single job step row from the JSON array.
	 */
	private JobStep parseJobStep(JSONObject rowObj) {
		JSONArray steps = rowObj.getJSONArray(BETA_COL);
		JobStep js = new JobStep();
		StringBuilder stepNameBuilder = new StringBuilder();
		
		for (int s = 0; s < steps.length(); s++) {
			JSONObject stepCol = steps.getJSONObject(s);
			String name = stepCol.optString("name");
			
			// Handle URI processing
			if ("URI".equals(name)) {
				processStepUri(js, stepCol);
			}
			
			// Handle step name components
			if (isStepNameComponent(name) && stepCol.has(BETA_CONTENT)) {
				if (stepNameBuilder.isEmpty()) {
					stepNameBuilder.append('.');
				}
				stepNameBuilder.append(stepCol.get(BETA_CONTENT).toString());
			}
		}
		
		js.setName(stepNameBuilder.toString());
		return js;
	}

	/**
	 * Helper to check if the column represents a step name component.
	 */
	private boolean isStepNameComponent(String name) {
		return "STEPNAME".equals(name) || "PROCSTEP".equals(name) || "DDNAME".equals(name);
	}

	/**
	 * Processes the step URI and fetches its logs.
	 */
	private void processStepUri(JobStep js, JSONObject stepCol) {
		String stepUrl = baseUri + stepCol.optString(BETA_CONTENT) + "&format=json";
		js.setUrl(stepUrl);
		
		if (0 == httpConnection.get(stepUrl, httpCredentials)) {
			jobLogList = new ArrayList<>();
			try {
				JSONObject joStep = new JSONObject(httpConnection.getResponseContent());
				Object oPages = joStep.getJSONObject(BETA_RETURN).getJSONObject(BETA_RESULT_TABLE).get(BETA_PAGE);
				parsePages(oPages);
				js.setLog(jobLogList);  
			} catch (JSONException e) {
				logger.warn("Failed to parse step log JSON from URI={}", stepUrl, e);
			}
		} else {
			logger.warn("Unable to get step info from URI={}", stepUrl);
		}
	}

	
	/**
     * Parse pages from a4dp JSON Object
     * 
     * @param oPages Pages object
     * @throws JSONException
     */
    private void parsePages(Object oPages) throws JSONException {
        for (JSONObject page : toJsonObjectList(oPages)) {
            if (page.has(BETA_LINE)) {
                parseLines(page.get(BETA_LINE));
            }
        }
    }
    
    /**
     * Parse lines from a4dp JSON object
     * 
     * @param oLines Lines object
     * @throws JSONException
     */
    private void parseLines(Object oLines) throws JSONException {
        for (JSONObject lineObj : toJsonObjectList(oLines)) {
            if (lineObj.has(BETA_CONTENT)) {
                jobLogList.add(lineObj.get(BETA_CONTENT).toString());
            }
        }
    }

    /**
     * Helper method to normalize a JSON object or array into a list of JSONObjects
     * 
     * @param obj the object that can be a JSONObject or JSONArray
     * @return a list of JSONObjects
     */
    private List<JSONObject> toJsonObjectList(Object obj) {
        List<JSONObject> list = new ArrayList<>();
        if (obj instanceof JSONObject jsonObject) {
            list.add(jsonObject);
        } else if (obj instanceof JSONArray jsonArray) {
            for (int i = 0; i < jsonArray.length(); i++) {
                list.add(jsonArray.getJSONObject(i));
            }
        }
        return list;
    }

}

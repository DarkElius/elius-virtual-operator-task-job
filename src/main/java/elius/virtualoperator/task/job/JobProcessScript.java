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

package elius.virtualoperator.task.job;

import elius.virtualoperator.task.job.flow.JobFlow;
import elius.virtualoperator.task.job.flow.JobFlowScript;


/**
 * JobProcessScript.
 * <p>
 * Define the task type, description and version for scripts and file in general.
 */
public class JobProcessScript extends AbstractJobTaskProcess {

	/**
	 * Default constructor.
	 */
	public JobProcessScript() {
		/* Nothing to initialize */
	}

	@Override
    public String getName() {
        return "EVO_JOB_SCRIPT";
    }

	@Override
    public String getDescription() {
        return "Job Script Process for Elius Virtual Operator";
    }

    @Override
    public String getVersion() {
        return "0.0.3";
    }	
	
	@Override
    protected JobFlow createJobFlow(Job job) {
        return new JobFlowScript(job);
    }

}

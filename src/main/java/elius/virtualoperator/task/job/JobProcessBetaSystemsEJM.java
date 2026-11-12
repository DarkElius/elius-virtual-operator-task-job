package elius.virtualoperator.task.job;

import elius.virtualoperator.task.job.flow.JobFlow;
import elius.virtualoperator.task.job.flow.JobFlowBetaSystems;

/**
 * JobProcessBetaSystemsEJM.
 * <p>
 * Define the task type, description and version for BetaSystems EJM jobs.
 */
public class JobProcessBetaSystemsEJM extends AbstractJobTaskProcess {

    /**
	 * Default constructor.
	 */
	public JobProcessBetaSystemsEJM() {
        /* Nothing to initialize */
	}

    @Override
    public String getName() {
        return "EVO_JOB_BETASYSTEMS_EJM";
    }

	@Override
    public String getDescription() {
        return "Job-EJM BetaSystems Process for Elius Virtual Operator";
    }

    @Override
    public String getVersion() {
        return "0.0.3";
    }
    
    @Override
    protected JobFlow createJobFlow(Job job) {
        return new JobFlowBetaSystems(job, JobType.EJM);
    }
}

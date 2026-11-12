package elius.virtualoperator.task.job;

import elius.virtualoperator.task.job.flow.JobFlow;
import elius.virtualoperator.task.job.flow.JobFlowBetaSystems;

/**
 * JobProcessBetaSystemsMainframe.
 * <p>
 * Define the task type, description and version for BetaSystems mainframe jobs.
 */
public class JobProcessBetaSystemsMainframe extends AbstractJobTaskProcess {

    /**
	 * Default constructor.
	 */
	public JobProcessBetaSystemsMainframe() {
        /* Nothing to initialize */
	}

    @Override
    public String getName() {
        return "EVO_JOB_BETASYSTEMS_MAINFRAME";
    }
    
	@Override
    public String getDescription() {
        return "Job-Mainframe BetaSystems Process for Elius Virtual Operator";
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

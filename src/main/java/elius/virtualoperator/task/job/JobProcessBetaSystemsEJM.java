package elius.virtualoperator.task.job;

import elius.virtualoperator.task.Task;
import elius.virtualoperator.task.TaskProcess;
import elius.virtualoperator.task.TaskStatus;

/**
 * JobProcessBetaSystemsEJM.
 * <p>
 * Define the task type, description and version for BetaSystems EJM jobs.
 */
public class JobProcessBetaSystemsEJM implements TaskProcess {
    
    JobProcessBetaSystems jbs = new JobProcessBetaSystems();

    /**
	 * Default constructor.
	 */
	public JobProcessBetaSystemsEJM() {
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
        return "0.0.2";
    }
    
  	@Override
	public void execute(Task task) {
        jbs.execute(task, JobType.EJM);
    }

	@Override
	public String getProcessResult() {
		return jbs.getProcessResult();
	}
	
	@Override
	public TaskStatus getTaskStatusResult() {
		return jbs.getTaskStatusResult();
	}    
}

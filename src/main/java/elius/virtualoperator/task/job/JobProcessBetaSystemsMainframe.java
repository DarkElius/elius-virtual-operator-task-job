package elius.virtualoperator.task.job;

import elius.virtualoperator.task.Task;
import elius.virtualoperator.task.TaskProcess;
import elius.virtualoperator.task.TaskStatus;

public class JobProcessBetaSystemsMainframe implements TaskProcess {
    
    // Beta Systems Job Process
    JobProcessBetaSystems jbs = new JobProcessBetaSystems();

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
        return "1.0";
    }
    
  	@Override
    public void execute(Task task) {
        jbs.execute(task, JobType.MAINFRAME);
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

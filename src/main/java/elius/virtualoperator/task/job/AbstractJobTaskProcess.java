package elius.virtualoperator.task.job;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;

import elius.virtualoperator.task.Task;
import elius.virtualoperator.task.TaskProcess;
import elius.virtualoperator.task.TaskStatus;
import elius.virtualoperator.task.job.flow.JobFlow;
import elius.virtualoperator.task.job.flow.JobFlowResult;

/**
 * Abstract base class for job-based task processes, eliminating code duplication 
 * in parsing and result mapping.
 */
public abstract class AbstractJobTaskProcess implements TaskProcess {

    protected static final Logger logger = LogManager.getLogger(AbstractJobTaskProcess.class);

    private String pResult;
    private TaskStatus tStatus;

    @Override
    public void execute(Task task) {
        logger.traceEntry("task={}", task);

        Job job = parseTaskDetails(task);
        if (job == null) {
            return;
        }

        // Initialize and execute the correct flow
        JobFlow flow = createJobFlow(job);
        flow.run();

        // Map result codes from flow to task
        pResult = flow.getFlowResultMessage();
        tStatus = switch (flow.getFlowResultCode()) {
            case JobFlowResult.OK -> TaskStatus.ENDED;
            case JobFlowResult.WARNING -> TaskStatus.WARNING;
            case JobFlowResult.ERROR -> TaskStatus.ERROR;
            default -> {
                pResult = "Flow execution is uncompleted / unknown error";
                yield TaskStatus.ERROR;
            }
        };

        logger.traceExit("status={}", tStatus);
    }

    /**
     * Factory method to be implemented by subclasses to supply the specific JobFlow.
     * 
     * @param job the parsed job object
     * @return the specific job flow instance
     */
    protected abstract JobFlow createJobFlow(Job job);

    @Override
    public String getProcessResult() {
        return pResult;
    }

    @Override
    public TaskStatus getTaskStatusResult() {
        return tStatus;
    }

    /**
     * Helper method to parse task details JSON into a Job object.
     * 
     * @param task the task to parse
     * @return the Job object, or null if parsing fails
     */
    private Job parseTaskDetails(Task task) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(task.getDetails(), Job.class);
        } catch (Exception e) {
            logger.error("Failed to parse task details JSON", e);
            pResult = "Invalid data: please check task details";
            tStatus = TaskStatus.ERROR;
            return null;
        }
    }
}
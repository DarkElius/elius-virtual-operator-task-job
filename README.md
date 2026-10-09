Elius Virtual Operator Task - Job
-----


Description
-----
Job Process Task for Elius Virtual Operator.



Run
-----
This project cannot be executed because it's a Task Process plugin for Elius Virtual Operator.

Please copy the *jar* file under *tasks* directory, the *evo-job.properties* and *search-entries* under *config*.


EVO_JOB_SCRIPT
-----
Fill the details with a JSON string like the following (remember to escape double quotes with slash)
*{"name": "", "id": "", "started": "", "ended": "", "rc": "", "url": ""}*
Values *started* and *ended* must be a valid timestamps, the other values are free text.
Use *evo-job.properties* to set the log directory path and to create the log file name.



EVO_JOB_BETASYSTEMS_MAINFRAME and EVO_JOB_BETASYSTEMS_EJM
-----
Fill the details with a JSON string like the following (remember to escape double quotes with slash)
*{"name": "", "id": "", "started": "", "ended": "", "rc": "", "url": ""}*
Values *started* and *ended* must be a valid timestamps, the other values are free text.
Use *evo-job.properties* to set the BetaSystems Control parameters.


### License

Apache 2.0 - <https://www.apache.org/licenses/LICENSE-2.0>
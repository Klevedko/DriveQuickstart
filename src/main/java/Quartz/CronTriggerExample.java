package Quartz;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import api.api_v1_v3_cron;
public class CronTriggerExample 
{
    public static void main( String[] args ) throws Exception
    {
    	
    	//JobDetail job = new JobDetail();
    	//job.setName("dummyJobName");
    	//job.setJobClass(HelloJob.class);    	
    	JobDetail job = JobBuilder.newJob(api_v1_v3_cron.class)
				.withIdentity("dummyJobName", "group1").build();

    	//CronTrigger trigger = new CronTrigger();
    	//trigger.setName("dummyTriggerName");
    	//trigger.setCronExpression("0/5 * * * * ?");
    	
    	Trigger trigger = TriggerBuilder
				.newTrigger()
				.withIdentity("dummyTriggerName", "group1")
				.withSchedule(
						CronScheduleBuilder.cronSchedule("1 * * * * ?"))
				.build();
    	
    	//schedule it
    	Scheduler scheduler = new StdSchedulerFactory().getScheduler();
    	scheduler.start();
    	scheduler.scheduleJob(job, trigger);
    
    }
}

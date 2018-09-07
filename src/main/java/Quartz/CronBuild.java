package Quartz;

import api.Apiv1v3crontest;
import org.quartz.*;
import org.quartz.impl.QuartzServer;
import org.quartz.impl.SchedulerRepository;
import org.quartz.impl.StdSchedulerFactory;
import api.Apiv1v3cron;

import java.util.List;

public class CronBuild {
    public static void main(String[] args) throws Exception {

        JobDetail job = JobBuilder.newJob(Apiv1v3crontest.class)
                .withIdentity("dummyJobName", "group1").build();

        Trigger trigger = TriggerBuilder
                .newTrigger()
                .withIdentity("dummyTriggerName", "group1")
                .withSchedule(
                        CronScheduleBuilder.cronSchedule("0/10 * * * * ?"))
                .build();

        //schedule it
        Scheduler scheduler = new StdSchedulerFactory().getScheduler();
        scheduler.start();
        scheduler.scheduleJob(job, trigger);
    }

}
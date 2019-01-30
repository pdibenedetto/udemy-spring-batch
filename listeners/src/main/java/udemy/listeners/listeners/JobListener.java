package udemy.listeners.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Slf4j
public class JobListener implements JobExecutionListener {
    private final JavaMailSender mailSender;
    private final String recipient;

    public JobListener(JavaMailSender mailSender, String recipient) {
        this.mailSender = mailSender;
        this.recipient = recipient;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();

        SimpleMailMessage mail = getSimpleMailMessage(String.format("%s is starting", jobName),
                String.format("per your request, %s is starting", jobName));

        sendMail(mail);
    }


    @Override
    public void afterJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();

        SimpleMailMessage mail = getSimpleMailMessage(String.format("%s ended", jobName),
                String.format("per your request, %s has ended", jobName));

        sendMail(mail);
    }

    private SimpleMailMessage getSimpleMailMessage(String subject, String body) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(recipient);
        mail.setSubject(subject);
        mail.setText(body);
        return mail;
    }

    private void sendMail(SimpleMailMessage message){
        log.warn("would have sent mail: {}", message.getSubject());
//        mailSender.send(message);
    }
}

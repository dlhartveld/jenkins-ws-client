package nl.tudelft.jenkins.dev;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nl.tudelft.jenkins.auth.User;
import nl.tudelft.jenkins.auth.UserImpl;
import nl.tudelft.jenkins.client.JenkinsClient;
import nl.tudelft.jenkins.client.JenkinsClientFactory;
import nl.tudelft.jenkins.jobs.Job;

import com.google.common.collect.Lists;

public class CreateJobApp {

	public static void main(String[] args) throws Exception {

		URL url = new URL("http://devhub.ewi.tudelft.nl/jenkins");
		String user = "test";
		String password = "pKPkPqhc";
		String job = "my-first-job";
		String scmUrl = "git://github.com/octocat/Hello-World.git";

		User david = new UserImpl("david", "david@hartveld.net");
		User test = new UserImpl("test", "team@devhub.nl");
		List<User> owners = Lists.newArrayList(david, test);

		ExecutorService executor = Executors.newSingleThreadExecutor();

		JenkinsClientFactory factory = new JenkinsClientFactory(url, user, password, executor);
		JenkinsClient client = factory.getJenkinsClient();

		// Job retrievedJob = client.retrieveJob(job);
		// String xml = retrievedJob.asXml();

		Job createdJob = client.createJob(job, scmUrl, owners);
		String xml = createdJob.asXml();

		System.out.println("Got XML:");
		System.out.println(xml);

		client.close();

		executor.shutdown();
		executor.awaitTermination(10, SECONDS);
	}

}

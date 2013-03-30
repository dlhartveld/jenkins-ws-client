package nl.tudelft.jenkins.tests.integration;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import nl.tudelft.jenkins.auth.User;
import nl.tudelft.jenkins.auth.UserImpl;
import nl.tudelft.jenkins.client.JenkinsClient;
import nl.tudelft.jenkins.guice.JenkinsWsClientGuiceModule;
import nl.tudelft.jenkins.jobs.Job;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Subscription;
import rx.util.functions.Action0;
import rx.util.functions.Action1;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class JobCreationAndRemovalAsyncIntegrationTest extends AbstractJenkinsIntegrationTestBase {

	private static final Logger LOG = LoggerFactory.getLogger(JobCreationAndRemovalAsyncIntegrationTest.class);

	private Injector injector;

	private JenkinsClient client;

	private ExecutorService executor;

	@Override
	@Before
	public void setUp() {
		executor = Executors.newSingleThreadExecutor();

		injector = Guice.createInjector(new JenkinsWsClientGuiceModule(getJenkinsURL(), getUserName(), getPassword(), executor));

		client = injector.getInstance(JenkinsClient.class);
	}

	@Override
	@After
	public void tearDown() throws Exception {
		client.close();

		executor.shutdown();
		executor.awaitTermination(10, SECONDS);
	}

	@Test
	public void testCreationAndRemovalOfJob() throws Exception {
		final List<User> owners = new ArrayList<>();
		owners.add(new UserImpl("test", "team@devhub.nl"));

		LOG.info("Creating job asynchronously ...");
		final Future<Job> job = client.createJobAsync("Async-Job", JOB_SCM_URL, owners);
		LOG.info("Waiting for job creation to complete ...");
		final Job actualJob = job.get();
		LOG.info("Job created.");

		final Future<Void> deletion = client.deleteJobAsync(actualJob);
		final Observable<Void> observable = Observable.toObservable(deletion);
		final Subscription subscription = observable.subscribe(
				new Action1<Void>() {
					@Override
					public void call(Void t1) {
						LOG.info("Got result.");
					}
				},
				new Action1<Exception>() {
					@Override
					public void call(Exception t1) {
						LOG.info("Something went wrong: {}", t1.getMessage(), t1);
					}
				},
				new Action0() {
					@Override
					public void call() {
						LOG.info("Completed.");
					}
				});

		LOG.info("Waiting for deletion to complete ...");
		subscription.unsubscribe();
		LOG.info("Deletion complete.");

	}
}

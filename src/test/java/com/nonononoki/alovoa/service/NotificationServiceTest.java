package com.nonononoki.alovoa.service;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.nonononoki.alovoa.entity.User;
import com.nonononoki.alovoa.entity.user.UserWebPush;
import com.nonononoki.alovoa.repo.ConversationRepository;
import com.nonononoki.alovoa.repo.UserRepository;
import com.nonononoki.alovoa.repo.UserWebPushRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class NotificationServiceTest {

	@Autowired
	private RegisterService registerService;

	@Autowired
	private CaptchaService captchaService;

	@Value("${app.vapid.max}")
	private int vapidMax;
	
	@Value("${app.age.min}")
	private int minAge;

	@Value("${app.message.size}")
	private int maxMessageSize;
	
	@Value("${app.first-name.length-max}")
	private int firstNameLengthMax;

	@Value("${app.first-name.length-min}")
	private int firstNameLengthMin;

	@MockBean
	private AuthService authService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private ConversationRepository conversationRepo;

	@Autowired
	private UserWebPushRepository userWebPushRepository;

	private List<User> testUsers;
	
	@BeforeEach
	public void before() throws Exception {
		testUsers = RegisterServiceTest.getTestUsers(captchaService, registerService, firstNameLengthMax, firstNameLengthMin);
	}
	
	@AfterEach
	public void after() throws Exception {
		RegisterServiceTest.deleteAllUsers(userService, authService, captchaService, conversationRepo, userRepo);
	}
	
	@Test
	public void test() throws Exception {

		User user1 = testUsers.get(1);

		Mockito.when(authService.getCurrentUser()).thenReturn(user1);
		
		Date oldDate = new Date();

		UserWebPush wp = new UserWebPush();
		wp.setDate(oldDate);
		for(int i = 0; i < vapidMax; i++) {
			notificationService.subscribe(wp);
		}
		Assert.assertEquals(userWebPushRepository.count(), vapidMax);
		
		
		Date newDate = new Date();
		wp.setDate(newDate);
		notificationService.subscribe(wp);
		Assert.assertEquals(userWebPushRepository.count(), vapidMax);
		
		user1 = userRepo.findByEmail(user1.getEmail());
		UserWebPush newWebPush = user1.getWebPush().get(vapidMax-1);
		Assert.assertEquals(newWebPush.getDate(), newDate);

	}

}
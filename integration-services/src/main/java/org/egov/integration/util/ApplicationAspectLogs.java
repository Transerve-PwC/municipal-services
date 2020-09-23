package org.egov.integration.util;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Configuration
@Slf4j
public class ApplicationAspectLogs {

	// Controller
	@Before("execution(* org.egov.integration.web.controller.*.*(..))")
	public void beforeController(JoinPoint joinPoint) {
		long startTime = System.currentTimeMillis();
		log.info("integration-services logs :: beforeController = Method::{}, Execution Time::{}", joinPoint.getSignature(),
				startTime);
		log.debug("integration-services logs :: beforeController = Method::{}, Execution Time::{}", joinPoint.getSignature(),
				startTime);
	}

	@After("execution(* org.egov.integration.web.controller.*.*(..))")
	public void afterController(JoinPoint joinPoint) {
		long startTime = System.currentTimeMillis();
		log.info("integration-services logs :: afterController = Method::{}, Execution Time::{}", joinPoint.getSignature(),
				startTime);
		log.debug("integration-services logs :: afterController = Method::{}, Execution Time::{}", joinPoint.getSignature(),
				startTime);
	}

	@AfterReturning(value = "execution(* org.egov.integration.web.controller.*.*(..))", returning = "result")
	public void afterReturningController(JoinPoint joinPoint, Object result) {
		long startTime = System.currentTimeMillis();
		log.info("integration-services logs :: afterReturningController = Method::{}, Execution Time::{}, Results::{}",
				joinPoint.getSignature(), startTime, result);
		log.debug("integration-services logs :: afterReturningController = Method::{}, Execution Time::{}, Results::{}",
				joinPoint.getSignature(), startTime, result);
	}

	@AfterThrowing(value = "execution(* org.egov.integration.web.controller.*.*(..))", throwing = "exception")
	public void afterThrowingController(JoinPoint joinPoint, Throwable exception) {
		long startTime = System.currentTimeMillis();
		log.info("integration-services logs :: afterThrowingController = Method::{}, Execution Time::{}, Exception::{}",
				joinPoint.getSignature(), startTime, exception.getMessage());
		log.debug("integration-services logs :: afterThrowingController = Method::{}, Execution Time::{}, Exception::{}",
				joinPoint.getSignature(), startTime, exception.getMessage());
	}


	// Service
		@Before("execution(* org.egov.integration.service.*.*(..))")
		public void beforeService(JoinPoint joinPoint) {
			long startTime = System.currentTimeMillis();
			log.info("integration-services logs :: beforeService = Method::{}, Execution Time::{}", joinPoint.getSignature(),
					startTime);
			log.debug("integration-services logs :: beforeService = Method::{}, Execution Time::{}", joinPoint.getSignature(),
					startTime);
		}

		@After("execution(* org.egov.integration.service.*.*(..))")
		public void afterService(JoinPoint joinPoint) {
			long startTime = System.currentTimeMillis();
			log.info("integration-services logs :: afterService = Method::{}, Execution Time::{}", joinPoint.getSignature(),
					startTime);
			log.debug("integration-services logs :: afterService = Method::{}, Execution Time::{}", joinPoint.getSignature(),
					startTime);
		}

		@AfterReturning(value = "execution(* org.egov.integration.service.*.*(..))", returning = "result")
		public void afterReturningService(JoinPoint joinPoint, Object result) {
			long startTime = System.currentTimeMillis();
			log.info("integration-services logs :: afterReturningService = Method::{}, Execution Time::{}, Results::{}",
					joinPoint.getSignature(), startTime, result);
			log.debug("integration-services logs :: afterReturningService = Method::{}, Execution Time::{}, Results::{}",
					joinPoint.getSignature(), startTime, result);
		}

		@AfterThrowing(value = "execution(* org.egov.integration.service.*.*(..))", throwing = "exception")
		public void afterThrowingService(JoinPoint joinPoint, Throwable exception) {
			long startTime = System.currentTimeMillis();
			log.info("integration-services logs :: afterThrowingService = Method::{}, Execution Time::{}, Exception::{}",
					joinPoint.getSignature(), startTime, exception.getMessage());
			log.debug("integration-services logs :: afterThrowingService = Method::{}, Execution Time::{}, Exception::{}",
					joinPoint.getSignature(), startTime, exception.getMessage());

			log.error("integration-services logs :: afterThrowingService = Method::{}, Execution Time::{}, Exception::{}",
					joinPoint.getSignature(), startTime, exception.getMessage());
		}

	// Repository
	@Before("execution(* org.egov.integration.repository.*.*(..))")
	public void beforeRepository(JoinPoint joinPoint) {
		long startTime = System.currentTimeMillis();
		log.info("integration-services logs :: beforeRepository = Method::{}, Execution Time::{}", joinPoint.getSignature(),
				startTime);
		log.debug("integration-services logs :: beforeRepository = Method::{}, Execution Time::{}", joinPoint.getSignature(),
				startTime);
	}

	@After("execution(* org.egov.integration.repository.*.*(..))")
	public void afterRepository(JoinPoint joinPoint) {
		long startTime = System.currentTimeMillis();
		log.info("integration-services logs :: afterRepository = Method::{}, Execution Time::{}", joinPoint.getSignature(),
				startTime);
		log.debug("integration-services logs :: afterRepository = Method::{}, Execution Time::{}", joinPoint.getSignature(),
				startTime);
	}

	@AfterReturning(value = "execution(* org.egov.integration.repository.*.*(..))", returning = "result")
	public void afterReturningRepository(JoinPoint joinPoint, Object result) {
		long startTime = System.currentTimeMillis();
		log.info("integration-services logs :: afterReturningRepository = Method::{}, Execution Time::{}, Results::{}",
				joinPoint.getSignature(), startTime, result);
		log.debug("integration-services logs :: afterReturningRepository = Method::{}, Execution Time::{}, Results::{}",
				joinPoint.getSignature(), startTime, result);
	}

	@AfterThrowing(value = "execution(* org.egov.integration.repository.*.*(..))", throwing = "exception")
	public void afterThrowingRepository(JoinPoint joinPoint, Throwable exception) {
		long startTime = System.currentTimeMillis();
		log.info("integration-services logs :: afterThrowingRepository = Method::{}, Execution Time::{}, Exception::{}",
				joinPoint.getSignature(), startTime, exception.getMessage());
		log.debug("integration-services logs :: afterThrowingRepository = Method::{}, Execution Time::{}, Exception::{}",
				joinPoint.getSignature(), startTime, exception.getMessage());
	}

}

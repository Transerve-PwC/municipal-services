package org.egov.ps.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ps.annotation.ApplicationValidator;
import org.egov.ps.model.Application;
import org.egov.ps.model.ApplicationCriteria;
import org.egov.ps.model.Property;
import org.egov.ps.repository.PropertyRepository;
import org.egov.ps.service.MDMSService;
import org.egov.ps.util.PSConstants;
import org.egov.ps.validator.application.OwnerValidator;
import org.egov.ps.web.contracts.ApplicationRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;

@Service
@Slf4j
public class ApplicationValidatorService {

	/**
	 * A map whose keys are annotation names and whose values are the validator
	 * beans.
	 */
	Map<String, IApplicationValidator> validators;

	ApplicationContext context;

	MDMSService mdmsService;

	PropertyRepository propertyRepository;

	ObjectMapper objectMapper;

	@Autowired
	OwnerValidator ownerValidator;

	@Autowired
	ApplicationValidatorService(ApplicationContext context, MDMSService mdmsService,
			PropertyRepository propertyRepository, ObjectMapper objectMapper, OwnerValidator ownerValidator) {
		this.context = context;
		this.mdmsService = mdmsService;
		this.propertyRepository = propertyRepository;
		this.objectMapper = objectMapper;
		this.ownerValidator = ownerValidator;
		Map<String, Object> beans = this.context.getBeansWithAnnotation(ApplicationValidator.class);

		/**
		 * Construct validators object by reading annotations from beans and discarding
		 * all beans that does not implement IApplicationValidator.
		 */
		this.validators = beans.entrySet().stream().filter(entry -> entry.getValue() instanceof IApplicationValidator)
				.collect(Collectors.toMap(e -> {
					ApplicationValidator annotation = this.context.findAnnotationOnBean(e.getKey(),
							ApplicationValidator.class);
					return annotation.value();
				}, e -> (IApplicationValidator) e.getValue()));
	}

	public void validateCreateRequest(ApplicationRequest request) {
		List<Application> applications = request.getApplications();
		applications.stream().forEach(application -> {
			String propertyId = application.getProperty().getId();
			validatePropertyExists(request.getRequestInfo(), propertyId);
			JsonNode applicationDetails = application.getApplicationDetails();
			try {
				String applicationDetailsString = this.objectMapper.writeValueAsString(applicationDetails);
				Configuration conf = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS);
				DocumentContext applicationObjectContext = JsonPath.using(conf).parse(applicationDetailsString);
				Map<String, List<String>> errorMap = this.performValidationsFromMDMS(application.getMDMSModuleName(),
						applicationObjectContext, request.getRequestInfo(), application.getTenantId(), propertyId);

				if (!errorMap.isEmpty()) {
					throw new CustomException("INVALID_FIELDS", "Please enter the valid fields " + errorMap.toString());
				}
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}

		});
	}

	private void validatePropertyExists(RequestInfo requestInfo, String propertyId) {
		Property property = propertyRepository.findPropertyById(propertyId);
		if (property == null) {
			throw new CustomException("INVALID_PROPERTY", "Could not find property with the given id:" + propertyId);
		}
		if (!property.getState().contentEquals(PSConstants.PM_APPROVED)) {
			throw new CustomException("INVALID_PROPERTY", "Property with the given " + propertyId + " is not approved");
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, List<String>> performValidationsFromMDMS(final String applicationType,
			DocumentContext applicationObject, RequestInfo RequestInfo, final String tenantId,
			final String propertyId) {
		List<Map<String, Object>> fieldConfigurations = this.mdmsService.getApplicationConfig(applicationType,
				RequestInfo, tenantId);
		Map<String, List<String>> errorsMap = new HashMap<String, List<String>>();
		for (int i = 0; i < fieldConfigurations.size(); i++) {
			Map<String, Object> fieldConfigMap = fieldConfigurations.get(i);

			/**
			 * Get field path from fieldConfig.
			 */
			String path = (String) fieldConfigMap.get("path");

			/**
			 * Get Value from applicationObject.
			 */
			Object value = applicationObject.read(path);
			List<Map<String, Object>> validationObjects = (List<Map<String, Object>>) fieldConfigMap.get("validations");

			/**
			 * Build IValidation object by reading from field configuration.
			 */
			List<IValidation> validations = validationObjects.stream()
					.map(validationObject -> ApplicationValidation.builder().type((String) validationObject.get("type"))
							.errorMessageFormat((String) validationObject.get("errorMessageFormat"))
							.params((Map<String, Object>) validationObject.get("params")).build())
					.collect(Collectors.toList());

			if (path.contains("..") && value instanceof JSONArray) {
				JSONArray valueArray = (JSONArray) value;
				valueArray.forEach(val -> {
					/**
					 * Construct the ApplicationField object.
					 */
					IApplicationField field = ApplicationField.builder().path(path)
							.required((boolean) fieldConfigMap.get("required")).rootObject(applicationObject).value(val)
							.validations(validations).build();

					/**
					 * Perform validations.
					 */
					List<String> errorMessages = this.performFieldValidations(applicationObject, field, propertyId);
					if (!CollectionUtils.isEmpty(errorMessages)) {
						errorsMap.put(path, errorMessages);
					}
				});

			} else {
				/**
				 * Construct the ApplicationField object.
				 */
				IApplicationField field = ApplicationField.builder().path(path)
						.required((boolean) fieldConfigMap.get("required")).rootObject(applicationObject).value(value)
						.validations(validations).build();

				/**
				 * Perform validations.
				 */
				List<String> errorMessages = this.performFieldValidations(applicationObject, field, propertyId);
				if (!CollectionUtils.isEmpty(errorMessages)) {
					errorsMap.put(path, errorMessages);
				}
			}
		}
		return errorsMap;
	}

	private static final String TYPE_REQUIRED = "required";

	private List<String> performFieldValidations(DocumentContext applicationObject, IApplicationField field,
			String propertyId) {

		Object value = field.getValue();

		/**
		 * Perform required validator validation first.
		 */
		IApplicationValidator requiredValidator = validators.get(TYPE_REQUIRED);
		List<String> requiredValidationErrors = requiredValidator
				.validate(ApplicationValidation.builder().type(TYPE_REQUIRED).build(), field, value, applicationObject);
		boolean isFieldEmpty = requiredValidationErrors != null && !requiredValidationErrors.isEmpty();

		if (field.isRequired() && isFieldEmpty) {
			log.debug("{} validator failed validating {} for path {}", TYPE_REQUIRED, value, field.getPath());
			return requiredValidationErrors;
		} else if (isFieldEmpty) {
			// field is not required and is empty. No validations to perform.
			return null;
		}

		/**
		 * Perform other validations and combine all the indivial error messages into a
		 * single list.
		 */
		return field.getValidations().stream().map(validation -> {
			if (validation.getType().equalsIgnoreCase("owner")) {
				return ownerValidator.validateOwner(propertyId, (String) value);
			}
			IApplicationValidator validator = validators.get(validation.getType());
			if (validator == null) {
				log.error("No validator found for {} for path {}", validation.getType(), field.getPath());
				return null;
			}
			log.debug("{} validator validating {} for path {}", validation.getType(), value, field.getPath());
			return validator.validate(validation, field, value, applicationObject);
		}).filter(validationErrors -> validationErrors != null && !validationErrors.isEmpty())
				.reduce(new ArrayList<String>(), (a, b) -> {
					a.addAll(b);
					return a;
				});
	}

	public List<Application> getApplications(ApplicationRequest applicationRequest) {
		ApplicationCriteria criteria = getApplicationCriteria(applicationRequest);
		List<Application> applications = propertyRepository.getApplications(criteria);

		boolean ifApplicationExists = ApplicationExists(applications);
		if (!ifApplicationExists) {
			throw new CustomException("APPLICATION NOT FOUND", "The application to be updated does not exist");
		} else {
			return null;
		}
	}

	private boolean ApplicationExists(List<Application> applicationRequest) {
		return (!CollectionUtils.isEmpty(applicationRequest) && applicationRequest.size() == 1);
	}

	private ApplicationCriteria getApplicationCriteria(ApplicationRequest request) {
		ApplicationCriteria applicationCriteria = new ApplicationCriteria();
		if (!CollectionUtils.isEmpty(request.getApplications())) {
			request.getApplications().forEach(application -> {
				if (application.getId() != null)
					applicationCriteria.setApplicationId(application.getId());
			});
		}
		return applicationCriteria;
	}
}

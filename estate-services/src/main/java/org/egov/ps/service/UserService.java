package org.egov.ps.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.ps.config.Configuration;
import org.egov.ps.model.UserResponse;
import org.egov.ps.model.UserSearchRequestCore;
import org.egov.ps.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {

    @Autowired
    private Configuration config;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private ObjectMapper mapper;

    public User getUserByUUID(String uuid, RequestInfo requestInfo) {
        List<org.egov.ps.model.User> applicationUser = null;
        Set<String> uuids = new HashSet<>();
        uuids.add(uuid);

        UserSearchRequestCore userSearchRequest = UserSearchRequestCore.builder().requestInfo(requestInfo).uuid(uuids)
                .build();

        String url = config.getUserHost().concat(config.getUserSearchEndpoint());
        applicationUser = mapper
                .convertValue(serviceRequestRepository.fetchResult(url, userSearchRequest), UserResponse.class)
                .getUser();

        log.info("applicationUser:" + applicationUser);

        // User requestUser = requestInfo.getUserInfo(); // user from request
        // information
        User requestUser = applicationUser.get(0).toCommonUser();
        return requestUser;
    }
}

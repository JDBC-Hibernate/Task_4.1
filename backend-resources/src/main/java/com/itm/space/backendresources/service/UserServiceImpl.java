package com.itm.space.backendresources.service;

import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.exception.BackendResourcesException;
import com.itm.space.backendresources.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final Keycloak keycloakClient;

    private final UserMapper userMapper;

    @Value("${keycloak.realm}")
    private String realm;

    private UserRepresentation userRepresentation;
    private List<RoleRepresentation> userRoles = new ArrayList<>();
    private List<GroupRepresentation> userGroups = new ArrayList<>();

    public UserResponse createUser(UserRequest userRequest) {
        CredentialRepresentation password = preparePasswordRepresentation(
                userRequest.getPassword()
        );
        userRepresentation = prepareUserRepresentation(
                userRequest, password
        );
        try {
            Response response = keycloakClient
                    .realm(realm)
                    .users()
                    .create(userRepresentation);
            String userId = CreatedResponseUtil.getCreatedId(response);
            log.info("UserService -> Created UserId: {}", userId);
        } catch (WebApplicationException ex) {
            log.error("Exception on \"createUser\": ", ex);
            throw new BackendResourcesException(
                    ex.getMessage(),
                    HttpStatus.resolve(ex.getResponse().getStatus())
            );
        }
        return userMapper.userRepresentationToUserResponse(
                userRepresentation, userRoles, userGroups);
    }

    @Override
    public UserResponse getUserById(UUID id) {
        try {
            userRepresentation = keycloakClient.realm(realm)
                    .users()
                    .get(String.valueOf(id))
                    .toRepresentation();
            userRoles = keycloakClient.realm(realm)
                    .users()
                    .get(String.valueOf(id))
                    .roles().getAll()
                    .getRealmMappings();
            userGroups = keycloakClient.realm(realm)
                    .users()
                    .get(String.valueOf(id))
                    .groups();
        } catch (RuntimeException ex) {
            log.error("Exception on \"getUserById\": ", ex);
            throw new BackendResourcesException(
                    ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return userMapper.userRepresentationToUserResponse(
                userRepresentation, userRoles, userGroups);
    }

    public CredentialRepresentation preparePasswordRepresentation(String password) {
        CredentialRepresentation credentialRepresentation =
                new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue(password);
        return credentialRepresentation;
    }

    public UserRepresentation prepareUserRepresentation(
            UserRequest userRequest,
            CredentialRepresentation credentialRepresentation) {
        UserRepresentation newUser = new UserRepresentation();
        newUser.setUsername(userRequest.getUsername());
        newUser.setEmail(userRequest.getEmail());
        newUser.setCredentials(List.of(credentialRepresentation));
        newUser.setEnabled(true);
        newUser.setFirstName(userRequest.getFirstName());
        newUser.setLastName(userRequest.getLastName());
        return newUser;
    }
}

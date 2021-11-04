package com.ebp.openQuarterMaster.baseStation.service.mongo;

import com.ebp.openQuarterMaster.baseStation.data.pojos.User;
import com.ebp.openQuarterMaster.baseStation.data.pojos.UserLoginRequest;
import com.ebp.openQuarterMaster.baseStation.service.JwtService;
import com.ebp.openQuarterMaster.baseStation.utils.AuthMode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.or;

@ApplicationScoped
public class UserService extends MongoService<User> {

    private Validator validator;
    private AuthMode authMode;

    UserService() {//required for DI
        super(null, null, null, null, null, null);
    }

    @Inject
    UserService(
            Validator validator,
            ObjectMapper objectMapper,
            MongoClient mongoClient,
            @ConfigProperty(name = "quarkus.mongodb.database")
                    String database,
            @ConfigProperty(name = "service.authMode")
                    AuthMode authMode
    ) {
        super(
                objectMapper,
                mongoClient,
                database,
                User.class
        );
        this.validator = validator;
        this.authMode = authMode;
    }

    /**
     * Gets a user from either their username or email.
     *
     * @param usernameOrEmail The username or email of the user.
     * @return
     */
    public User getFromUsernameEmail(String usernameOrEmail) {
        return this.getCollection()
                .find(
                        or(
                                eq("email", usernameOrEmail),
                                eq("username", usernameOrEmail)
                        )
                )
                .limit(1)
                .first();
    }

    public User getFromLoginRequest(UserLoginRequest loginRequest) {
        return this.getFromUsernameEmail(loginRequest.getUsernameEmail());
    }

    private User getExternalUser(String externalSource, String externalId) {
        return this.getCollection().find(eq("externIds." + externalSource, externalId)).limit(1).first();
    }

    private User getOrCreateExternalUser(JsonWebToken jwt) {
        String externalSource = jwt.getIssuer();
        String externalId = jwt.getClaim(JwtService.JWT_USER_ID_CLAIM);
        User user = this.getExternalUser(externalSource, externalId);

        if (user != null) {
            //TODO:: update from given jwt, if needed
            return user;
        }

        User.Builder userBuilder = User.builder(jwt);

        userBuilder.externIds(new HashMap<>() {{
            put(externalSource, externalId);
        }});

        user = userBuilder.build();

        Set<ConstraintViolation<User>> validationErrs = this.validator.validate(user);
        if (!validationErrs.isEmpty()) {
            throw new IllegalStateException(
                    "Resulting user from jwt wasn't valid: " +
                            validationErrs.stream().map(ConstraintViolation<User>::getMessage).collect(Collectors.joining(", "))
            );
        }
        //TODO:: check if username or email already exists

        this.add(user);
        return user;
    }

    /**
     * Gets a user from the given jwt.
     * <p>
     * If {@link #authMode} is set to {@link AuthMode#SELF}, simple lookup.
     * <p>
     * If {@link #authMode} is set to {@link AuthMode#EXTERNAL}, the service will lookup based on the external id in the
     * jwt, creating the user if they don't exist yet.
     *
     * @param jwt The jwt to get the user for
     * @return The user the jwt was for. Null if no user found.
     */
    public User getFromJwt(JsonWebToken jwt) {
        String userId = jwt.getClaim(JwtService.JWT_USER_ID_CLAIM);

        switch (this.authMode) {
            case SELF:
                return this.get(userId);
            case EXTERNAL:
                return this.getOrCreateExternalUser(jwt);
        }
        return null;
    }
}

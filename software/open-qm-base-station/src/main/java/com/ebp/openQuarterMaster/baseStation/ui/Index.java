package com.ebp.openQuarterMaster.baseStation.ui;

import com.ebp.openQuarterMaster.baseStation.service.mongo.UserService;
import com.ebp.openQuarterMaster.lib.core.rest.user.UserGetResponse;
import com.ebp.openQuarterMaster.lib.core.user.User;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

@Traced
@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class Index extends UiProvider {

    @Inject
    @Location("webui/pages/index")
    Template index;
    @Inject
    @Location("webui/pages/overview")
    Template overview;
    @Inject
    @Location("webui/pages/storage")
    Template storage;
    @Inject
    @Location("webui/pages/items")
    Template items;

    @Inject
    UserService userService;

    @Inject
    JsonWebToken jwt;

    @GET
    @PermitAll
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance index(
            @Context SecurityContext securityContext
    ) {
        logRequestContext(jwt, securityContext);
        return index.instance();
    }

    @GET
    @Path("overview")
    @RolesAllowed("user")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance overview(
            @Context SecurityContext securityContext
    ) {
        logRequestContext(jwt, securityContext);
        User user = userService.getFromJwt(this.jwt);
        return overview.data("userInfo", UserGetResponse.builder(user).build());
    }

    @GET
    @Path("storage")
    @RolesAllowed("user")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance storage(
            @Context SecurityContext securityContext
    ) {
        logRequestContext(jwt, securityContext);
        User user = userService.getFromJwt(this.jwt);
        return storage.data("userInfo", UserGetResponse.builder(user).build());
    }

    @GET
    @Path("items")
    @RolesAllowed("user")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance items(
            @Context SecurityContext securityContext
    ) {
        logRequestContext(jwt, securityContext);
        User user = userService.getFromJwt(this.jwt);
        return items.data("userInfo", UserGetResponse.builder(user).build());
    }

}

package tech.ebp.oqm.baseStation.interfaces.endpoints;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.utils.AuthMode;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.core.SecurityContext;

@Traced
@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class EndpointProvider {
	
	protected static void assertSelfAuthMode(AuthMode authMode) {
		if (!AuthMode.SELF.equals(authMode)) {
			//TODO:: throw custom exception, handle to return proper response object
			throw new ForbiddenException("Service not set to authenticate its own users.");
		}
	}
	
	protected static void logRequestContext(JsonWebToken jwt, SecurityContext context) {
		if (!hasJwt(jwt)) {
			log.info("Processing request with no JWT; ssh:{}", context.isSecure());
		} else {
			log.info(
				"Processing request with JWT; User:{} ssh:{} jwtIssuer: {}",
				context.getUserPrincipal().getName(),
				context.isSecure(),
				jwt.getIssuer()
			);
			if (context.isSecure()) {
				log.warn("Request with JWT made without HTTPS");
			}
		}
		
	}
	
	protected static boolean hasJwt(JsonWebToken jwt) {
		return jwt != null && jwt.getClaimNames() != null;
	}
}
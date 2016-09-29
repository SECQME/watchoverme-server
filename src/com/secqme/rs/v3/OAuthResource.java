package com.secqme.rs.v3;

import com.secqme.CoreException;
import com.secqme.domain.model.UserVO;
import com.secqme.rs.BaseResource;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.GrantType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by edward on 12/08/2015.
 */
@Path("/v3/oauth")
public class OAuthResource extends BaseResource {

    public static final String INVALID_CLIENT_DESCRIPTION = "Client authentication failed (e.g., unknown client, no client authentication included, or unsupported authentication method).";
    public static final String INVALID_GRANT_TYPE_DESCRIPTION = "Unsupported grant type.";

    @POST
    @Path("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authorize(@Context HttpServletRequest request) throws OAuthSystemException {
        try {
            OAuthTokenRequest oauthRequest = new OAuthTokenRequest(request);

            // Check if clientid is valid
            if (!sysAdminUtil.isValidClientId(oauthRequest.getClientId())) {
                return buildInvalidClientResponse();
            }

            // Check if client_secret is valid
            if (!sysAdminUtil.isValidClientAccess(oauthRequest.getClientId(), oauthRequest.getClientSecret())) {
                return buildUnauthorizedClient();
            }

            // Do checking for different grant types
            if (GrantType.PASSWORD.equals(oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE))) {
                try {
                    UserVO userVO = userManager.authenticateUser(oauthRequest.getUsername(), oauthRequest.getPassword());
                    return buildAuthenticatedUserResponse(userVO);
                } catch (CoreException ex) {
                    return buildInvalidUsernameOrPasswordResponse();
                }
            } else  {
                return buildUnsupportedGrantTypeResponse();
            }
        } catch (OAuthProblemException ex) {
            return buildOAuthProblemExceptionResponse(ex);
        }
    }

    private Response buildInvalidClientResponse() throws OAuthSystemException {
        OAuthResponse response =
                OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_CLIENT)
                        .setErrorDescription(INVALID_CLIENT_DESCRIPTION)
                        .buildJSONMessage();
        return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
    }

    private Response buildUnauthorizedClient() throws OAuthSystemException {
        OAuthResponse response =
                OAuthASResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                        .setError(OAuthError.TokenResponse.UNAUTHORIZED_CLIENT)
                        .setErrorDescription(INVALID_CLIENT_DESCRIPTION)
                        .buildJSONMessage();
        return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
    }

    private Response buildUnsupportedGrantTypeResponse() throws OAuthSystemException {
        OAuthResponse response =
                OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.UNSUPPORTED_GRANT_TYPE)
                        .setErrorDescription(INVALID_GRANT_TYPE_DESCRIPTION)
                        .buildJSONMessage();
        return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
    }

    private Response buildInvalidUsernameOrPasswordResponse() throws OAuthSystemException {
        OAuthResponse response = OAuthASResponse
                .errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                .setError(OAuthError.TokenResponse.INVALID_GRANT)
                .setErrorDescription("invalid username or password")
                .buildJSONMessage();
        return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
    }
    
    private Response buildAuthenticatedUserResponse(UserVO userVO) throws OAuthSystemException {
        OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());

        OAuthResponse response = OAuthASResponse
                .tokenResponse(HttpServletResponse.SC_OK)
                .setAccessToken(oauthIssuerImpl.accessToken())
                .setExpiresIn("3600")
                .buildJSONMessage();
        return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
    }

    private Response buildOAuthProblemExceptionResponse(OAuthProblemException ex) throws OAuthSystemException {
        OAuthResponse res =
                OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .error(ex)
                        .buildJSONMessage();
        return Response.status(res.getResponseStatus()).entity(res.getBody()).build();
    }
}

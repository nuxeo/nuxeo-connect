/*
 * (C) Copyright 2016 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Nuxeo
 */

package org.nuxeo.connect.registration.response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

/**
 * @author <a href="mailto:ak@nuxeo.com">Arnaud Kervern</a>
 * @since 1.4.24
 */
public class TestResponses {

    protected String SAMPLE_ERROR = "{\"message\":\"There were some errors in your form:\",\"value\":[{\"message\":\"You must accept the Nuxeo Trial Terms and Conditions\",\"field\":\"termsAndConditions\"}],\"type\":\"error\"}";

    protected String SAMPLE_ERROR_VALUELESS = "{\"message\":\"A server error occurred and we can not currently process your request, but your request has been logged and an Administrator will handle it.\",\"type\":\"error\"}";

    protected String SAMPLE_MESSAGE = "{\"type\":\"message\",\"message\":\"Registration complete.\",\"value\":{\"username\":\"test@toto.com\",\"email\":\"test@toto.com\",\"company\":\"blalba\",\"wizardToken\":\"cmVnaXN0cmF0aW9uT0s6dHJ1ZQpDTElEOjJiMmFiZjUwLTU3ZjEtNGQ1OS1iMGY2LTRiMmRhMmUxMzBhOS0tYTczZjMzZTctM2NmZC00N2YxLWIzMjktOGIxNWIzOTkxNDI1Cg==\"}}";

    @Test
    public void testResponseRead() throws IOException {
        TrialRegistrationResponse res = TrialRegistrationResponse.read(SAMPLE_ERROR);
        assertTrue(res instanceof TrialErrorResponse);

        assertEquals("error", res.getType());
        assertTrue(res.getMessage().startsWith("There were some errors in "));

        res = TrialRegistrationResponse.read(SAMPLE_ERROR_VALUELESS);
        assertTrue(res instanceof TrialErrorResponse);
        res = TrialRegistrationResponse.read(SAMPLE_MESSAGE);
        assertTrue(res instanceof TrialSuccessResponse);
    }

    @Test
    public void testErrorParseValue() throws IOException {
        TrialErrorResponse err = (TrialErrorResponse) TrialRegistrationResponse.read(SAMPLE_ERROR);
        assertEquals(1, err.getErrors().size());

        TrialErrorResponse.Error error = err.getErrors().get(0);
        assertTrue(error.getMessage().startsWith("You must accept the"));
        assertEquals("termsAndConditions", error.getField());
    }

    @Test
    public void testErrorMessageLessParseValue() throws IOException {
        TrialErrorResponse err = (TrialErrorResponse) TrialRegistrationResponse.read(SAMPLE_ERROR_VALUELESS);
        assertEquals(0, err.getErrors().size());
    }

    @Test
    public void testSucessParseValue() throws IOException {
        TrialSuccessResponse res = (TrialSuccessResponse) TrialRegistrationResponse.read(SAMPLE_MESSAGE);
        assertEquals("test@toto.com", res.getEmail());
        assertEquals("blalba", res.getCompany());
        assertEquals(2, res.getToken().keySet().size());
        assertTrue(res.getToken().containsKey("CLID"));
        assertTrue(res.getToken().containsKey("registrationOK"));
    }
}

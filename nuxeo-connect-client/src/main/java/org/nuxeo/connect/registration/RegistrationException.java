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

package org.nuxeo.connect.registration;

import java.util.ArrayList;
import java.util.List;

import org.nuxeo.connect.registration.response.TrialErrorResponse;

/**
 * @author <a href="mailto:ak@nuxeo.com">Arnaud Kervern</a>
 * @since 1.4.25
 */
public class RegistrationException extends Exception {

    private static final long serialVersionUID = -8586515796778400401L;

    private TrialErrorResponse error;

    public RegistrationException(TrialErrorResponse error) {
        super(error.getMessage());
        this.error = error;
    }

    public RegistrationException(String message) {
        super(message);
        this.error = TrialErrorResponse.UNKNOWN();
    }

    public RegistrationException(Throwable e) {
        super(e);
        this.error = TrialErrorResponse.UNKNOWN();
    }

    public List<TrialErrorResponse.Error> getErrors() {
        return error.getErrors();
    }

    public List<String> getErrorsMessages() {
        List<String> msg = new ArrayList<>();
        for (TrialErrorResponse.Error err : error.getErrors()) {
            msg.add(err.getMessage());
        }
        return msg;
    }
}

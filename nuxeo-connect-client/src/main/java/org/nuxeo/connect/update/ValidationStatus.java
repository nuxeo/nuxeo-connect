/*
 * (C) Copyright 2006-2010 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     bstefanescu
 */
package org.nuxeo.connect.update;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides information about task validation.
 *
 * A task cannot be executed if the validation status has errors. It may be
 * executed (the user should be prompted) if the task has warnings.
 *
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class ValidationStatus {

    public static final ValidationStatus OK = new ValidationStatus();

    protected List<String> errors;

    protected List<String> warnings;

    public ValidationStatus() {
        errors = new ArrayList<String>();
        warnings = new ArrayList<String>();
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public void addWarning(String warn) {
        warnings.add(warn);
    }

    public void addError(String error) {
        errors.add(error);
    }
}

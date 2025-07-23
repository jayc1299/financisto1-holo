/*
 * Copyright (c) 2014 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package tw.tib.financisto.export;

/**
 * Created with IntelliJ IDEA.
 * User: dsolonenko
 * Date: 1/27/14
 * Time: 10:44 PM
 */
public class ImportExportException extends Exception {

    public final int errorResId;
    public final Object[] formatArgs;

    public ImportExportException(int errorResId) {
        this(errorResId, null);
    }

    public ImportExportException(int errorResId, Throwable cause) {
        this(errorResId, cause, (Object[]) null);
    }

    public ImportExportException(int errorResId, Throwable cause, Object... formatArgs) {
        super(cause);
        this.errorResId = errorResId;
        this.formatArgs = formatArgs;
    }

}

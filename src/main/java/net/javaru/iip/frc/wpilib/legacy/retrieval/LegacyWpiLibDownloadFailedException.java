/*
 * Copyright 2015-2019 the original author or authors
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package net.javaru.iip.frc.wpilib.legacy.retrieval;

public class LegacyWpiLibDownloadFailedException extends RuntimeException
{
    public static final String DEFAULT_MESSAGE = "Download of WPILib failed.";
    private static final long serialVersionUID = 1979652530740028621L;


    public LegacyWpiLibDownloadFailedException()
    {
        super(DEFAULT_MESSAGE);
    }


    public LegacyWpiLibDownloadFailedException(Throwable cause)
    {
        super(DEFAULT_MESSAGE, cause);
    }


    public LegacyWpiLibDownloadFailedException(String message)
    {
        super(message);
    }


    public LegacyWpiLibDownloadFailedException(String message, Throwable cause)
    {
        super(message, cause);
    }
}

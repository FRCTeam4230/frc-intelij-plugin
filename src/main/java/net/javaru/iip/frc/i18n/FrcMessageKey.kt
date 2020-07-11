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

package net.javaru.iip.frc.i18n;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;



public class FrcMessageKey
{
    @NotNull
    @PropertyKey(resourceBundle = FrcBundle.BUNDLE_NAME)
    private final String key;
    private static final String[] EMPTY_PARAMS = new String[0];
    
    
    @NotNull
    private final Object[] params;
    
    public static FrcMessageKey of(@NotNull @PropertyKey(resourceBundle = FrcBundle.BUNDLE_NAME) String key, @Nullable Object... params)
    {
        return new FrcMessageKey(key, params);
    }
    
    private FrcMessageKey(@NotNull @PropertyKey(resourceBundle = FrcBundle.BUNDLE_NAME) String key, @Nullable Object... params)
    {
        this.key = key;
        this.params = params == null ? EMPTY_PARAMS : params;
    }
    
    
    @NotNull
    public String getKey() { return key; }
    
    
    @NotNull
    public Object[] getParams() { return params; }
}

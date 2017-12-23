/*
 * Copyright 2015-2017 Mark Vedder
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package net.javaru.iip.frc.wpilib.version;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiLiteralExpression;

import net.javaru.iip.frc.i18n.FrcMessageBundle;
import net.javaru.iip.frc.util.FindClassUtils;
import net.javaru.iip.frc.wpilib.WpiLibConstants;
import net.javaru.iip.frc.wpilib.WpiLibLibrariesUtils;



public class DetermineWpiLibVersion
{
    private static final Logger LOG = Logger.getInstance(DetermineWpiLibVersion.class);
   


    public static String determineVersion(@NotNull Project project)
    {
        if (!WpiLibLibrariesUtils.isWpilibPresent(project))
        {
            return FrcMessageBundle.message("frc.wpilib.not.attached");
        }

        
        final PsiClass[] verClass = FindClassUtils.findClass(project, WpiLibConstants.VERSION_CLASS_FQN);
        
        if (verClass.length == 0)
        {
            return FrcMessageBundle.message("frc.wpilib.version.unavailable", WPILIB_VERSION_CLASS_FQN);
        }

        String version = null;
        for (PsiClass aClass : verClass)
        {
            @Nullable
            final PsiField versionField = aClass.findFieldByName("Version", false);
            if (versionField != null)
            {
                final PsiExpression initializer = versionField.getInitializer();
                
                if (initializer instanceof PsiLiteralExpression)
                {
                    Object value = ((PsiLiteralExpression) initializer).getValue();
                    if (value != null && value instanceof String)
                    {
                        version = value.toString();
                        break;
                    }
                }
            }
        }
        
        return version != null ? version : FrcMessageBundle.message("frc.wpilib.version.undetermined");
    }
    
}

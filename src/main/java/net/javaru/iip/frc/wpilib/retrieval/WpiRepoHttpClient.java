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

package net.javaru.iip.frc.wpilib.retrieval;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;



class WpiRepoHttpClient
{
    private static final Logger LOG = Logger.getInstance(WpiRepoHttpClient.class);


    public static Document fetchXmlResourceAsDocument(@NotNull URI uri) throws IOException, JDOMException
    {
        final String xmlString = fetchResourceContentAsString(uri);
        try (final ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8)))
        {
            return new SAXBuilder().build(inputStream);
        }
    }


    public static String fetchResourceContentAsString(URI uri) throws IOException
    {
        final CloseableHttpClient httpClient = getHttpClient();
        final HttpClientContext httpClientContext = HttpClientContext.create();
        final HttpGet httpRequest = new HttpGet(uri);
        try (final CloseableHttpResponse httpResponse = httpClient.execute(httpRequest, httpClientContext))
        {
            LOG.info("[FRC] http response status line was '" + httpResponse.getStatusLine() + "' for URI '" + uri + '\'');
            final HttpEntity httpEntity = httpResponse.getEntity();
            final String xml = EntityUtils.toString(httpEntity, StandardCharsets.UTF_8);
            EntityUtils.consumeQuietly(httpEntity);
            return xml;
        }
    }


    @NotNull
    protected static CloseableHttpClient getHttpClient()
    {
        return HttpClients.custom()
                          .setDefaultRequestConfig(createRequestConfig())
                          .disableCookieManagement()
                          //.setSslcontext(CertificateManager.getInstance().getSslContext())
                          //.setDefaultCredentialsProvider(createCredentialsProvider())
                          //.addInterceptorLast(createRequestInterceptor())
                          .build();
    }


    @NotNull
    protected static RequestConfig createRequestConfig()
    {
        RequestConfig.Builder builder = RequestConfig.custom()
                                                     .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                                                     .setConnectTimeout(15_000)
                                                     .setSocketTimeout(15_000);

        //TODO: Add a use Proxy option ?
        //if (isUseProxy())
        {
            //IdeHttpClientHelpers.ApacheHttpClient4.setProxyForUrlIfEnabled(builder, getRepoBaseUri().toString());
        }

        return builder.build();
    }


    /**
     * Custom request interceptor can be used for modifying outgoing requests. One possible usage is to
     * add specific header to each request according to authentication scheme used.
     *
     * @return specific request interceptor or null by default
     */
    @Nullable
    protected HttpRequestInterceptor createRequestInterceptor()
    {
        return null;
    }


    public static Path downloadAndSaveToTemp(URI uri) throws IOException
    {
        //uri.getPath returns the post main URL portion, so for example "/foo/lib.jar" for "http://example.com/foo/lib.jar?key=value" 
        final Path resourceFileName = Paths.get(uri.getPath()).getFileName();

        final Path destinationPath = Paths.get(FileUtil.getTempDirectory()).resolve(resourceFileName).toAbsolutePath();
        try
        {
            destinationPath.toFile().deleteOnExit();
            LOG.debug("[FRC] temp file marked for deletion on exit: " + destinationPath);
        }
        catch (Exception e)
        {
            LOG.warn("[FRC] Could not mark temp file '" + destinationPath + "' for deletion on exit due to an exception: " + e.toString(), e);
        }

        final CloseableHttpClient httpClient = getHttpClient();
        final HttpClientContext httpClientContext = HttpClientContext.create();
        final HttpGet httpRequest = new HttpGet(uri);
        try (final CloseableHttpResponse httpResponse = httpClient.execute(httpRequest, httpClientContext))
        {
            LOG.info("[FRC] http response status line was '" + httpResponse.getStatusLine() + "' for URI '" + uri + '\'');
            final HttpEntity httpEntity = httpResponse.getEntity();
            if (httpEntity == null)
            {
                final String message =
                    "[FRC] Could not download resource '" + resourceFileName + "' from '" + uri + "'. HTTP status " + httpResponse.getStatusLine();
                LOG.warn(message);
                throw new IOException(message);
            }
            else
            {
                try
                {
                    Files.deleteIfExists(destinationPath);
                }
                catch (IOException e)
                {
                    LOG.warn("[FRC] Could not delete existing dir '" + destinationPath + "' to ensure clean update. Cause Summary: " + e.toString(), e);
                }
                Files.createDirectories(destinationPath.getParent());

                //TODO - put in code to check if file already exists

                LOG.info("[FRC] Copying '" + resourceFileName + "' to destination path '" + destinationPath + "'");
                try (
                    final BufferedInputStream inputStream = new BufferedInputStream(httpEntity.getContent());
                    final BufferedOutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(destinationPath));
                )
                {
                    FileUtil.copy(inputStream, outputStream);
                }
                return destinationPath;
            }
        }
    }

}

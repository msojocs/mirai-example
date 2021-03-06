package org.example.robot.utils.HTTP;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.io.ManagedHttpClientConnectionFactory;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.Header;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.apache.hc.core5.util.Timeout;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * ?????????????????????[??????]
 *
 * @author jiyec
 */
public class HttpUtil {

    private static final CustomCookieStore httpCookieStore;
    private static final HttpClientContext defaultContext;
    private static final RequestConfig.Builder unBuildConfig;
    private static final CloseableHttpClient httpClient;
    public static final String CHARSET = "UTF-8";

    // ?????????????????????????????????????????????????????????????????????????????????httpClient??????
    static {

        // SSL??????
        SSLContext sslContext = null;
        SSLConnectionSocketFactory sslCSF = null;
        try {
            sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                // ????????????
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            sslCSF = new SSLConnectionSocketFactory(sslContext);
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            e.printStackTrace();
        }

        ManagedHttpClientConnectionFactory managedHttpClientConnectionFactory = new ManagedHttpClientConnectionFactory(
                null, CharCodingConfig.custom()
                .setMalformedInputAction(CodingErrorAction.IGNORE)
                .setUnmappableInputAction(CodingErrorAction.IGNORE)
                .setCharset(StandardCharsets.UTF_8)
                .build(), null, null);
        // ???????????????
        PoolingHttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslCSF)
                .setConnectionFactory(managedHttpClientConnectionFactory)
                .build();

        // Cookie??????
        httpCookieStore = new CustomCookieStore();

        unBuildConfig = RequestConfig.custom();

        // ??????
        final RequestConfig config = unBuildConfig
                .setConnectTimeout(Timeout.ofSeconds(5))
                .setRedirectsEnabled(false)
                // .setProxy(new HttpHost("127.0.0.1", 8866))      // TODO:????????????????????????
                .setCircularRedirectsAllowed(true)
                .build();

        // ????????????
        defaultContext = HttpClientContext.create();
        defaultContext.setCookieStore(httpCookieStore);
        defaultContext.setRequestConfig(config);

        // ???????????????
        httpClient = HttpClients.custom()
                .setDefaultCookieStore(httpCookieStore)
                .setDefaultRequestConfig(config)
                .setConnectionManager(cm)
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Safari/537.36 Edg/90.0.818.51")
                .build();
    }

    /**
     * ??????Cookie
     *
     * @return Map<String, String>
     */
    public static Map<String, String> getCookie() {
        List<Cookie> cookiesCustom = httpCookieStore.getCookiesCustom();
        Map<String, String> cookies = new HashMap<>();
        for (Cookie cookie : cookiesCustom) {
            cookies.put(cookie.getName(), cookie.getValue());
        }
        return cookies;
    }


    /**
     *    _____/\\\\\\\\\\\\__/\\\\\\\\\\\\\\\__/\\\\\\\\\\\\\\\_
     *     ___/\\\//////////__\/\\\///////////__\///////\\\/////__
     *      __/\\\_____________\/\\\___________________\/\\\_______
     *       _\/\\\____/\\\\\\\_\/\\\\\\\\\\\___________\/\\\_______
     *        _\/\\\___\/////\\\_\/\\\///////____________\/\\\_______
     *         _\/\\\_______\/\\\_\/\\\___________________\/\\\_______
     *          _\/\\\_______\/\\\_\/\\\___________________\/\\\_______
     *           _\//\\\\\\\\\\\\/__\/\\\\\\\\\\\\\\\_______\/\\\_______
     *            __\////////////____\///////////////________\///________
     *            FROM:http://patorjk.com/software/taag
     */

    /**
     * @param url ??????get?????????url
     * @return String ?????????
     */
    public static String doGet(String url) throws IOException, ParseException {
        return getString(Objects.requireNonNull(doGet(url, null, null, CHARSET, defaultContext)), CHARSET);
    }

    public static String doGet(String url, Map<String, String> params) throws IOException, ParseException {
        return getString(Objects.requireNonNull(doGet(url, params, null, CHARSET, defaultContext)), CHARSET);
    }

    /**
     * HTTP Get ????????????
     *
     * @param url     ?????????url?????? ????????????????
     * @param params  ???????????????
     * @param charset ????????????
     * @return ????????????
     */
    public static String doGet(String url, Map<String, String> params, String charset) throws IOException, ParseException {
        return getString(Objects.requireNonNull(doGet(url, params, null, charset, defaultContext)), charset);
    }

    public static String doGet(String url, String charset, Map<String, String> headers) throws IOException, ParseException {
        return getString(Objects.requireNonNull(doGet(url, null, headers, charset, defaultContext)), charset);
    }

    /**
     * HTTP Get ????????????
     *
     * @param url     ?????????url?????? ????????????????
     * @param headers ???????????????????????????
     * @return ????????????
     */
    public static String doGet2(String url, Map<String, String> headers) throws IOException, ParseException {
        return getString(Objects.requireNonNull(doGet(url, null, headers, null, defaultContext)), CHARSET);
    }

    /**
     * HTTP Get ????????????
     *
     * @param url ?????????url?????? ????????????????
     * @return ????????????
     */
    public static HttpUtilEntity doGetEntity(String url) throws IOException, ParseException {
        return doGetEntity(url, null, null, CHARSET);
    }

    /**
     * HTTP Get ????????????
     *
     * @param url     ?????????url?????? ????????????????
     * @param headers ???????????????????????????
     * @return ????????????
     */
    public static HttpUtilEntity doGetEntity(String url, Map<String, String> headers) throws IOException, ParseException {
        return doGetEntity(url, null, headers, CHARSET);
    }

    public static HttpUtilEntity doGetEntity(String url, Map<String, String> headers, String charset) throws IOException, ParseException {
        return doGetEntity(url, null, headers, charset);
    }

    /**
     * HTTP GET ?????? [????????????]
     *
     * @param url     ???????????????
     * @param params  ????????????
     * @param headers ??????????????????
     * @param charset ????????????
     * @return HttpUtilEntity
     */
    public static HttpUtilEntity doGetEntity(String url, Map<String, String> params, Map<String, String> headers, String charset) throws IOException, ParseException {
        CloseableHttpResponse closeableHttpResponse = doGet(url, params, headers, charset, defaultContext);

        if(null == closeableHttpResponse)return null;
        HttpUtilEntity httpUtilEntity = response2entity(closeableHttpResponse, charset);
        closeableHttpResponse.close();

        return httpUtilEntity;
    }
    public static CloseableHttpResponse doGet(
            String url,
            Map<String, String> params,
            Map<String, String> headers,
            String charset
    ) {
        return doGet(url, params, headers, charset, defaultContext);
    }
    /**
     * HTTP Get ???????????? [?????????]
     *
     * @param url     ?????????url?????? ????????????????
     * @param params  ???????????????
     * @param headers ???????????????
     * @param charset ????????????
     * @return CloseableHttpResponse
     */
    public static CloseableHttpResponse doGet(
            String url,
            Map<String, String> params,
            Map<String, String> headers,
            String charset,
            HttpClientContext context
    ) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        try {
            UrlEncodedFormEntity urlEncodedFormEntity = genFormEntity(params, charset);

            // ??????????????????url????????????
            if (null != urlEncodedFormEntity)
                url += "?" + EntityUtils.toString(urlEncodedFormEntity);

            HttpGet httpGet = new HttpGet(url);
            addHeader(httpGet, headers);

            return httpClient.execute(httpGet, context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /***
     *    __/\\\\\\\\\\\\\_________/\\\\\__________/\\\\\\\\\\\____/\\\\\\\\\\\\\\\_
     *     _\/\\\/////////\\\_____/\\\///\\\______/\\\/////////\\\_\///////\\\/////__
     *      _\/\\\_______\/\\\___/\\\/__\///\\\___\//\\\______\///________\/\\\_______
     *       _\/\\\\\\\\\\\\\/___/\\\______\//\\\___\////\\\_______________\/\\\_______
     *        _\/\\\/////////____\/\\\_______\/\\\______\////\\\____________\/\\\_______
     *         _\/\\\_____________\//\\\______/\\\__________\////\\\_________\/\\\_______
     *          _\/\\\______________\///\\\__/\\\_____/\\\______\//\\\________\/\\\_______
     *           _\/\\\________________\///\\\\\/_____\///\\\\\\\\\\\/_________\/\\\_______
     *            _\///___________________\/////_________\///////////___________\///________
     *            FROM:http://patorjk.com/software/taag
     */
    public static String doPost(String url, Map<String, String> params) throws IOException, ParseException {
        return getString(Objects.requireNonNull(doPost(url, params, null, CHARSET)), CHARSET);
    }
    public static String doPost2(String url, Map<String, String> headers) throws IOException, ParseException {
        return getString(Objects.requireNonNull(doPost(url, null, headers, CHARSET)), CHARSET);
    }

    public static String doStreamPost(String url, byte[] data) throws IOException {
        InputStreamEntity inputStreamEntity = genStreamEntity(data);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(inputStreamEntity);

        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpPost, defaultContext);
            int statusCode = response.getCode();
            if (statusCode != 200) {
                httpPost.abort();
                throw new RuntimeException("HttpClient,error status code :" + statusCode);
            }
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, "utf-8");
            }
            EntityUtils.consume(entity);
            return result;
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            if (response != null)
                response.close();
        }
        return null;
    }

    public static String doFilePost(String url, byte[] data) throws IOException {
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.addBinaryBody("captcha", data, ContentType.DEFAULT_BINARY, URLEncoder.encode("captcha.jpg", "utf-8"));
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(multipartEntityBuilder.build());

        try (CloseableHttpResponse response = httpClient.execute(httpPost, defaultContext)) {
            int statusCode = response.getCode();
            if (statusCode != 200) {
                httpPost.abort();
                throw new RuntimeException("HttpClient,error status code :" + statusCode);
            }
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, "utf-8");
            }
            EntityUtils.consume(entity);
            return result;
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (HttpHostConnectException e) {
            throw new RuntimeException("Connect to host failed!", e);
        } catch (ConnectException e) {
            throw new RuntimeException("Connection failed!", e);
        }
        return null;
    }

    /**
     * HTTP Post ????????????
     *
     * @param url     ?????????url?????? ????????????????
     * @param params  ???????????????
     * @param charset ????????????
     * @return ????????????
     * @throws IOException IO??????
     */
    public static String doPost(String url, Map<String, String> params, String charset)
            throws IOException, ParseException {
        return getString(Objects.requireNonNull(doPost(url, params, null, charset)), charset);
    }
    public static String doPost(String url, Map<String, String> params, Map<String, String> header)
            throws IOException, ParseException {
        return getString(Objects.requireNonNull(doPost(url, params, header, CHARSET)), CHARSET);
    }

    public static HttpUtilEntity doPostEntity(String url, Map<String, String> params) throws IOException, ParseException {
        return doPostEntity(url, params, null, "UTF-8");
    }

    public static HttpUtilEntity doPostEntity(String url, Map<String, String> params, Map<String, String> headers) throws IOException, ParseException {
        return doPostEntity(url, params, headers, "UTF-8");
    }

    public static HttpUtilEntity doPostEntity(String url, Map<String, String> params, String charset) throws IOException, ParseException {
        return doPostEntity(url, params, null, charset);
    }

    public static HttpUtilEntity doPostEntity(String url, Map<String, String> params, Map<String, String> headers, String charset) throws IOException, ParseException {
        CloseableHttpResponse closeableHttpResponse = doPost(url, params, headers, charset);
        if(null == closeableHttpResponse)return null;
        HttpUtilEntity httpUtilEntity = response2entity(closeableHttpResponse, charset);
        closeableHttpResponse.close();
        return httpUtilEntity;
    }

    public static CloseableHttpResponse doPost(
            String url,
            Map<String, String> params,
            Map<String, String> headers,
            String charset
    ) throws IOException {
        if (StringUtils.isBlank(url)) {
            return null;
        }

        HttpPost httpPost = new HttpPost(url);
        // ???????????????
        addHeader(httpPost, headers);

        // ????????????????????????
        UrlEncodedFormEntity urlEncodedFormEntity = genFormEntity(params, charset);
        if (null != urlEncodedFormEntity) {
            httpPost.setEntity(urlEncodedFormEntity);
        }

        return httpClient.execute(httpPost, defaultContext);
    }

    /***
     *    __/\\\________/\\\_____/\\\\\\\\\_____/\\\\\_____/\\\__/\\\\\\\\\\\\_____/\\\______________/\\\\\\\\\\\\\\\_
     *     _\/\\\_______\/\\\___/\\\\\\\\\\\\\__\/\\\\\\___\/\\\_\/\\\////////\\\__\/\\\_____________\/\\\///////////__
     *      _\/\\\_______\/\\\__/\\\/////////\\\_\/\\\/\\\__\/\\\_\/\\\______\//\\\_\/\\\_____________\/\\\_____________
     *       _\/\\\\\\\\\\\\\\\_\/\\\_______\/\\\_\/\\\//\\\_\/\\\_\/\\\_______\/\\\_\/\\\_____________\/\\\\\\\\\\\_____
     *        _\/\\\/////////\\\_\/\\\\\\\\\\\\\\\_\/\\\\//\\\\/\\\_\/\\\_______\/\\\_\/\\\_____________\/\\\///////______
     *         _\/\\\_______\/\\\_\/\\\/////////\\\_\/\\\_\//\\\/\\\_\/\\\_______\/\\\_\/\\\_____________\/\\\_____________
     *          _\/\\\_______\/\\\_\/\\\_______\/\\\_\/\\\__\//\\\\\\_\/\\\_______/\\\__\/\\\_____________\/\\\_____________
     *           _\/\\\_______\/\\\_\/\\\_______\/\\\_\/\\\___\//\\\\\_\/\\\\\\\\\\\\/___\/\\\\\\\\\\\\\\\_\/\\\\\\\\\\\\\\\_
     *            _\///________\///__\///________\///__\///_____\/////__\////////////_____\///////////////__\///////////////__
     *
     */
    private static HttpUtilEntity response2entity(CloseableHttpResponse closeableHttpResponse, String charset) throws IOException, ParseException {

        // ???????????????
        Header[] allHeaders = closeableHttpResponse.getHeaders();
        Map<String, String> allHeaderMap = new HashMap<>();

        for (Header header : allHeaders) {
            // if(allHeaderMap.containsKey(header.getName()))
            //     allHeaderMap.put(header.getName(), allHeaderMap.get(header.getName()) + ";" + header.getValue());
            // else
            allHeaderMap.put(header.getName(), header.getValue());
        }

        HttpUtilEntity httpUtilEntity = new HttpUtilEntity();
        // ?????????
        httpUtilEntity.setStatusCode(closeableHttpResponse.getCode());
        httpUtilEntity.setHeaders(allHeaderMap);
        HttpEntity entity = closeableHttpResponse.getEntity();
        httpUtilEntity.setBody(EntityUtils.toString(entity, charset));
        httpUtilEntity.setCookies(getCookie());
        return httpUtilEntity;
    }

    private static InputStreamEntity genStreamEntity(byte[] data) {
        return new InputStreamEntity(new ByteArrayInputStream(data), ContentType.MULTIPART_FORM_DATA);
    }

    private static UrlEncodedFormEntity genFormEntity(Map<String, String> params, String charset) throws UnsupportedEncodingException {
        if (null == params || params.isEmpty()) return null;

        List<NameValuePair> pairs = null;

        pairs = new ArrayList<>(params.size());
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String value;
            value = entry.getValue();
            if (value != null) {
                pairs.add(new BasicNameValuePair(entry.getKey(), value));
            }
        }
        return new UrlEncodedFormEntity(pairs, Charset.forName(charset));
    }

    private static void addHeader(HttpUriRequestBase http, Map<String, String> headers) {
        if (null != headers) headers.forEach(http::setHeader);
    }

    private static String getString(CloseableHttpResponse response, String charset) throws IOException, ParseException {
        HttpEntity entity = response.getEntity();

        String result = null;
        if (entity != null) {
            result = EntityUtils.toString(entity, charset);
        }
        EntityUtils.consume(entity);
        response.close();
        return result;
    }

    public static HttpClientContext genConfig(Map<String, Object> customConfig){
        Object timeout = customConfig.get("timeout");
        Object maxRedirect = customConfig.get("maxRedirect");

        // ??????
        final RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(timeout!=null?(int)timeout:5))
                .setMaxRedirects(maxRedirect!=null?(int)maxRedirect:0)
                .setRedirectsEnabled(false)
                // .setProxy(new HttpHost("127.0.0.1", 8866))      // TODO:????????????????????????
                .setCircularRedirectsAllowed(true)
                .build();

        BasicCookieStore cookieStore = new BasicCookieStore();
        // ????????????
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(cookieStore);
        context.setRequestConfig(config);

        return context;
    }
}


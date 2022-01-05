package czertainly.ip.discovery;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Configuration
@EnableJpaAuditing
@PropertySource(value = ApplicationConfig.EXTERNAL_PROPERTY_SOURCE, ignoreResourceNotFound = true)
public class ApplicationConfig {

	public static final String EXTERNAL_PROPERTY_SOURCE = "file:${raprofiles-ip-discovery.config.dir:/etc/raprofiles-ip-discovery}/raprofiles-ip-discovery.properties";
	/**
	 * Set this property on the {@link BindingProvider#getRequestContext()} to
	 * enable {HttpURLConnection#setConnectTimeout(int)}
	 *
	 * <p>
	 * int timeout = ...; Map<String, Object> ctxt =
	 * ((BindingProvider)proxy).getRequestContext(); ctxt.put(CONNECT_TIMEOUT,
	 * timeout);
	 * </p>
	 */
	public static final String CONNECT_TIMEOUT = "com.sun.xml.ws.connect.timeout";

	/**
	 * Set this property on the {@link BindingProvider#getRequestContext()} to
	 * enable {HttpURLConnection#setReadTimeout(int)}
	 *
	 * <p>
	 * int timeout = ...; Map<String, Object> ctxt =
	 * ((BindingProvider)proxy).getRequestContext(); ctxt.put(REQUEST_TIMEOUT,
	 * timeout);
	 * </p>
	 */
	public static final String REQUEST_TIMEOUT = "com.sun.xml.ws.request.timeout";
	public static final String REQUEST_SSL_SOCKET_FACTORY = "com.sun.xml.internal.ws.transport.https.client.SSLSocketFactory";

	@Bean
	public void httpsConnection() {
		TrustManager[] dummyTrustManager = new TrustManager[] { new X509TrustManager() {

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
				// TODO Auto-generated method stub

			}

			@Override
			public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
				// TODO Auto-generated method stub

			}
		} };
		SSLContext sc;
		try {
			sc = SSLContext.getInstance("SSL");
			sc.init(null, dummyTrustManager, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (NoSuchAlgorithmException | KeyManagementException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		HostnameVerifier allHostsValid = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};

		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	}

}
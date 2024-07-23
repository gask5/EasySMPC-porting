package org.bihealth.mi.easybus.implementations.http;

import com.github.markusbernhardt.proxy.ProxySearch;

import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.List;

/**
 * Try to create a HTTP proxy. In parts derived from
 * k9mail-library/src/main/java/com/fsck/k9/mail/store/imap/ImapStoreUriCreator.java
 *
 * @author Fabian Prasser
 * @author Felix Wirth
 */
public class ConnectionHTTPProxy {

    /**
     * Get proxy for HTTP(S) connection
     *
     * @param uri
     */
    public static Proxy getProxy(URI uri) {
        try {

            // Search process
            ProxySearch proxySearch = ProxySearch.getDefaultProxySearch();

            // Selector
            ProxySelector proxySelector = proxySearch.getProxySelector();

            // Install this ProxySelector as default ProxySelector for all connections.
            ProxySelector.setDefault(proxySelector);

            // Get list of proxies from default ProxySelector available for given URL
            List<Proxy> proxies = null;
            if (ProxySelector.getDefault() != null) {
                proxies = ProxySelector.getDefault().select(uri);
            }

            // Find first proxy for HTTP/S. Any DIRECT proxy in the list returned is only second choice
            if (proxies != null) {
                for (Proxy proxy : proxies) {
                    if (proxy.type() == Proxy.Type.HTTP) {
                        return proxy;
                    }
                }
            }

            // Nothing found
            return null;

        } catch (Exception e) {

            // Something went wrong
            return null;
        }
    }
}

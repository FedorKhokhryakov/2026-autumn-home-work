package company.vk.edu.distrib.compute.fedorkhokhryakov.urlshortener;

import company.vk.edu.distrib.compute.urlshortener.UrlShortenerService;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class UrlShortenerServiceImpl implements UrlShortenerService {
    private final int port;
    private final InMemoryDao<String> dao;
    private final InMemoryUserDao userDao;
    private HttpServer server;

    public UrlShortenerServiceImpl(int port, InMemoryDao<String> dao) {
        this(port, dao, new InMemoryUserDao());
    }

    public UrlShortenerServiceImpl(
        int port,
        InMemoryDao<String> dao,
        InMemoryUserDao userDao
    ) {
        this.port = port;
        this.dao = dao;
        this.userDao = userDao;
    }

    @Override
    public void start() {
        if (server != null) {
            throw new IllegalStateException("Service has already been started");
        }

        try {
            server = HttpServer.create(new InetSocketAddress("localhost", port), 0);

            UrlShortenerHttpHandler handler =
                new UrlShortenerHttpHandler(port, dao, userDao);

            server.createContext("/v0/status", handler::handleStatus);
            server.createContext("/v0/links", handler::handleLinks);
            server.createContext("/internal/users", handler::handleUsers);
            server.createContext("/", handler::handleRedirect);

            server.start();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start HTTP server", e);
        }
    }

    @Override
    public void stop() {
        if (server == null) {
            throw new IllegalStateException("Service has not been started");
        }

        server.stop(0);
        server = null;
    }
}

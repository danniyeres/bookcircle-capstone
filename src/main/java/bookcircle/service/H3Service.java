package bookcircle.service;

import com.uber.h3core.H3Core;
import bookcircle.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class H3Service {
    private static final Logger log = LoggerFactory.getLogger(H3Service.class);

    private final H3Core h3;
    private final int defaultResolution;

    public H3Service(@Value("${app.h3.defaultResolution}") int defaultResolution) {
        this.defaultResolution = defaultResolution;
        try {
            this.h3 = H3Core.newInstance();
            log.info("H3 initialized with defaultResolution={}", defaultResolution);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to init H3", e);
        }
    }

    public String toH3(double lat, double lon) {
        return toH3(lat, lon, defaultResolution);
    }

    public String toH3(double lat, double lon, int res) {
        if (res < 0 || res > 15) {
            log.warn("Invalid H3 resolution requested res={}", res);
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid H3 resolution (0..15)");
        }
        long idx = h3.latLngToCell(lat, lon, res);
        String h3Index = h3.h3ToString(idx);
        log.debug("H3 calculated lat={} lon={} res={} h3={}", lat, lon, res, h3Index);
        return h3Index;
    }
}

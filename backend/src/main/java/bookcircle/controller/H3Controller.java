package bookcircle.controller;

import bookcircle.service.H3Service;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/h3")
public class H3Controller {

    private final H3Service h3Service;

    public H3Controller(H3Service h3Service) {
        this.h3Service = h3Service;
    }

    @GetMapping("/encode")
    public Map<String, Object> encode(@RequestParam double lat, @RequestParam double lon, @RequestParam(required = false) Integer res) {
        String h3 = (res == null) ? h3Service.toH3(lat, lon) : h3Service.toH3(lat, lon, res);
        Map<String, Object> out = new HashMap<>();
        out.put("h3Index", h3);
        out.put("lat", lat);
        out.put("lon", lon);
        if (res != null) out.put("resolution", res);
        return out;
    }
}

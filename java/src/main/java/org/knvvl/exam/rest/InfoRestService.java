package org.knvvl.exam.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InfoRestService
{
    @GetMapping("/info")
    String info()
    {
        return "Running";
    }
}

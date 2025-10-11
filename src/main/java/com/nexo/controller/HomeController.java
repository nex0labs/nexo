package com.nexo.controller;

import com.nexo.annotation.Controller;
import com.nexo.annotation.Route;
import com.nexo.dto.HomeResponse;
import com.nexo.service.VersionService;

@Controller
public class HomeController {
  private final long startTime;
  private final VersionService versionService;

  public HomeController() {
    this.startTime = System.currentTimeMillis();
    this.versionService = new VersionService();
  }

  @Route(path = "/")
  public HomeResponse health() {
    long currentTime = System.currentTimeMillis();
    long uptime = currentTime - startTime;

    return new HomeResponse("ok", versionService.getVersion(), currentTime, uptime);
  }
}

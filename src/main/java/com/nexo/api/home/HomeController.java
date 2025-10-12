package com.nexo.api.home;

import com.nexo.server.annotation.Controller;
import com.nexo.server.annotation.Route;

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

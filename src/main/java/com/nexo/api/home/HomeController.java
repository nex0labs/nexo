package com.nexo.api.home;

import com.nexo.server.annotation.Controller;
import com.nexo.server.annotation.Route;

@Controller
public class HomeController {
  private final HomeService homeService;
  private static final String NAME = "nexo";
  private static final String TAGLINE = "Knowledge Search, Built for Agents";

  public HomeController() {
    this.homeService = new HomeService();
  }

  @Route(path = "/")
  public HomeResponse health() {
    HomeResponse.VersionInfo versionInfo =
        new HomeResponse.VersionInfo(homeService.getVersion(), homeService.getTantivyVersion());
    return new HomeResponse(NAME, TAGLINE, versionInfo, homeService.getBuildTimestamp());
  }
}

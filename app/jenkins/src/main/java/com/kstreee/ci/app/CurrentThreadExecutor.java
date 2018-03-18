package com.kstreee.ci.app;

import java.util.concurrent.Executor;

public class CurrentThreadExecutor implements Executor {
  @Override
  public void execute(Runnable r) {
    r.run();
  }
}
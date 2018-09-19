package org.kiwix.kiwixmobile.downloader;

import com.google.common.util.concurrent.Monitor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

public class DownloadExecutor extends ScheduledThreadPoolExecutor {
  private final Monitor monitor = new Monitor();
  private boolean isPaused;
  private final Monitor.Guard paused = new Monitor.Guard(monitor) {
    @Override
    public boolean isSatisfied() {
      return isPaused;
    }
  };

  private final Monitor.Guard notPaused = new Monitor.Guard(monitor) {
    public boolean isSatisfied() {
      return !isPaused;
    }
  };

  public DownloadExecutor(int corePoolSize) {
    super(corePoolSize);
  }

  protected void beforeExecute(Thread t, Runnable r) {
    super.beforeExecute(t, r);
    monitor.enterWhenUninterruptibly(notPaused);
    try {
      monitor.waitForUninterruptibly(notPaused);
    } finally {
      monitor.leave();
    }
  }

  public void pause() {
    monitor.enterIf(notPaused);
    try {
      isPaused = true;
    } finally {
      monitor.leave();
    }
  }

  public void resume() {
    monitor.enterIf(paused);
    try {
      isPaused = false;
    } finally {
      monitor.leave();
    }
  }
}

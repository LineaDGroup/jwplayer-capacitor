package com.lineadgroup.capacitorjwplayer;

import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;

public class MySessionManagerListener implements SessionManagerListener<CastSession> {

  private CastSession mCastSession;

  public MySessionManagerListener(CastSession mCastSession) {
    this.mCastSession = mCastSession;
  }

  @Override
  public void onSessionEnded(CastSession session, int error) {
    if (session == mCastSession) {
      mCastSession = null;
    }
    // invalidateOptionsMenu();
  }

  @Override
  public void onSessionResumed(CastSession session, boolean wasSuspended) {
    mCastSession = session;
    // invalidateOptionsMenu();
  }

  @Override
  public void onSessionStarted(CastSession session, String sessionId) {
    mCastSession = session;
    // invalidateOptionsMenu();
  }

  @Override
  public void onSessionStarting(CastSession session) {
  }

  @Override
  public void onSessionStartFailed(CastSession session, int error) {
  }

  @Override
  public void onSessionEnding(CastSession session) {
  }

  @Override
  public void onSessionResuming(CastSession session, String sessionId) {
  }

  @Override
  public void onSessionResumeFailed(CastSession session, int error) {
  }

  @Override
  public void onSessionSuspended(CastSession session, int reason) {
  }

}

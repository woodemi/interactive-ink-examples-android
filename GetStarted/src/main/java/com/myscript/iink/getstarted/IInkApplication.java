// Copyright MyScript. All rights reserved.

package com.myscript.iink.getstarted;

import android.app.Application;

import com.myscript.certificate.MyCertificate;
import com.myscript.iink.Editor;
import com.myscript.iink.Engine;
import com.myscript.iink.Renderer;

public class IInkApplication extends Application
{

  private static float widthDpi = 2610;
  private static float width = 14800;
  private static float heightDpi = 2540;
  private static float height = 21000;

  private static Engine engine;
  public static Renderer renderer;
  public static Editor editor;

  public static synchronized Engine getEngine()
  {
    if (engine == null)
    {
      engine = Engine.create(MyCertificate.getBytes());
      renderer = engine.createRenderer(widthDpi, heightDpi, null);
      editor = engine.createEditor(renderer);
    }

    return engine;
  }

}

// Copyright MyScript. All rights reserved.

package com.myscript.iink.getstarted;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.gson.Gson;
import com.myscript.iink.Configuration;
import com.myscript.iink.ContentPackage;
import com.myscript.iink.ContentPart;
import com.myscript.iink.ConversionState;
import com.myscript.iink.Editor;
import com.myscript.iink.Engine;
import com.myscript.iink.IEditorListener;
import com.myscript.iink.MimeType;
import com.myscript.iink.PointerType;
import com.myscript.iink.uireferenceimplementation.EditorView;
import com.myscript.iink.uireferenceimplementation.FontUtils;
import com.myscript.iink.uireferenceimplementation.InputController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
  private static final String TAG = "MainActivity";

  private Engine engine;
  private ContentPackage contentPackage;
  private ContentPart contentPart;
  private EditorView editorView;

  private double exportScale = 7.498404170270271 * 2;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    ErrorActivity.installHandler(this);

    engine = IInkApplication.getEngine();

    // configure recognition
    Configuration conf = engine.getConfiguration();
    String confDir = "zip://" + getPackageCodePath() + "!/assets/conf";
    conf.setStringArray("configuration-manager.search-path", new String[]{confDir});
    conf.setBoolean("text.guides.enable", false);
    conf.setBoolean("export.jiix.strokes", true);
    conf.setBoolean("export.jiix.glyphs", true);
    String tempDir = getFilesDir().getPath() + File.separator + "tmp";
    conf.setString("content-package.temp-folder", tempDir);

    setContentView(R.layout.activity_main);

    editorView = findViewById(R.id.editor_view);

    // load fonts
    AssetManager assetManager = getApplicationContext().getAssets();
    Map<String, Typeface> typefaceMap = FontUtils.loadFontsFromAssets(assetManager);
    editorView.setTypefaces(typefaceMap);

    editorView.setEngine(engine);

    final Editor editor = editorView.getEditor();
    editor.addListener(new IEditorListener()
    {
      @Override
      public void partChanging(Editor editor, ContentPart oldPart, ContentPart newPart)
      {
        // no-op
      }

      @Override
      public void partChanged(Editor editor)
      {
        invalidateOptionsMenu();
        invalidateIconButtons();
      }

      @Override
      public void contentChanged(Editor editor, String[] blockIds)
      {
        invalidateOptionsMenu();
        invalidateIconButtons();
      }

      @Override
      public void onError(Editor editor, String blockId, String message)
      {
        Log.e(TAG, "Failed to edit block \"" + blockId + "\"" + message);
      }
    });

    setInputMode(InputController.INPUT_MODE_FORCE_PEN); // If using an active pen, put INPUT_MODE_AUTO here

    String packageName = "File1.iink";
    File file = new File(getFilesDir(), packageName);
    try
    {
      File fileFile = new File("/data/user/0/com.myscript.iink.getstarted/cache/23.pts");
      Log.e(TAG, "fileFile.exists(): " + fileFile.exists() + ", cacheDir: " + this.getCacheDir());

      contentPackage = engine.openPackage(fileFile);
      contentPart = contentPackage.getPart(0);

//      contentPackage = engine.createPackage(file);
//      contentPart = contentPackage.createPart("Text"); // Choose type of content (possible values are: "Text Document", "Text", "Diagram", "Math", and "Drawing")
    }
    catch (IOException e)
    {
      Log.e(TAG, "Failed to open package \"" + packageName + "\"", e);
    }
    catch (IllegalArgumentException e)
    {
      Log.e(TAG, "Failed to open package \"" + packageName + "\"", e);
    }

    setTitle("Type: " + contentPart.getType());

    // wait for view size initialization before setting part
    editorView.post(new Runnable()
    {
      @Override
      public void run()
      {
        editorView.getRenderer().setViewOffset(0, 0);
        editorView.getRenderer().setViewScale(1);
        editorView.setVisibility(View.VISIBLE);
        editor.setPart(contentPart);
      }
    });

    findViewById(R.id.button_input_mode_forcePen).setOnClickListener(this);
    findViewById(R.id.button_input_mode_forceTouch).setOnClickListener(this);
    findViewById(R.id.button_input_mode_auto).setOnClickListener(this);
    findViewById(R.id.button_undo).setOnClickListener(this);
    findViewById(R.id.button_redo).setOnClickListener(this);
    findViewById(R.id.button_clear).setOnClickListener(this);

    findViewById(R.id.exportValue).setOnClickListener(this);
//    findViewById(R.id.throwPointer).setOnClickListener(this);
    findViewById(R.id.splitPointer).setOnClickListener(this);

    invalidateIconButtons();
  }

  @Override
  protected void onDestroy()
  {
    editorView.setOnTouchListener(null);
    editorView.close();

    if (contentPart != null)
    {
      contentPart.close();
      contentPart = null;
    }
    if (contentPackage != null)
    {
      contentPackage.close();
      contentPackage = null;
    }


    // IInkApplication has the ownership, do not close here
    engine = null;

    super.onDestroy();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    getMenuInflater().inflate(R.menu.activity_main, menu);

    MenuItem convertMenuItem = menu.findItem(R.id.menu_convert);
    convertMenuItem.setEnabled(true);

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch (item.getItemId())
    {
      case R.id.menu_convert:
      {
        Editor editor = editorView.getEditor();
        ConversionState[] supportedStates = editor.getSupportedTargetConversionStates(null);
        if (supportedStates.length > 0)
          editor.convert(null, supportedStates[0]);
        return true;
      }
      default:
      {
        return super.onOptionsItemSelected(item);
      }
    }
  }

  @Override
  public void onClick(View v)
  {
    switch (v.getId())
    {
      case R.id.button_input_mode_forcePen:
        setInputMode(InputController.INPUT_MODE_FORCE_PEN);
        break;
      case R.id.button_input_mode_forceTouch:
        setInputMode(InputController.INPUT_MODE_FORCE_TOUCH);
        break;
      case R.id.button_input_mode_auto:
        setInputMode(InputController.INPUT_MODE_AUTO);
        break;
      case R.id.button_undo:
        editorView.getEditor().undo();
        break;
      case R.id.button_redo:
        editorView.getEditor().redo();
        break;
      case R.id.button_clear:
        editorView.getEditor().clear();
        break;
      case R.id.exportValue:
        try {
          String value = editorView.getEditor().export_(null, MimeType.TEXT);
          Toast.makeText(this, "识别结果：" + value, Toast.LENGTH_LONG).show();
          Log.e(TAG, "识别结果：" + value);
        } catch (IOException e) {
          e.printStackTrace();
        }
        try {
          String value = editorView.getEditor().export_(null, MimeType.JIIX);
          Toast.makeText(this, "JIIX：" + value, Toast.LENGTH_LONG).show();
          Log.e(TAG, "JIIX：" + value);
        } catch (IOException e) {
          e.printStackTrace();
        }
        break;
//      case R.id.throwPointer:
//        this.importPointer();
//        break;
      case R.id.splitPointer:
        Toast.makeText(this, "split pointer", Toast.LENGTH_LONG).show();
        try {
          String value = editorView.getEditor().export_(null, MimeType.JIIX);
          Toast.makeText(this, "识别结果：" + value, Toast.LENGTH_LONG).show();
//          Log.e(TAG, "jiix：" + value);

          List<NotePointer> pointers = new ArrayList<>();

          // gson

          Map map = new Gson().fromJson(value, Map.class);
//          Log.e(TAG, "map: " + map);

          List words = (List)map.get("words");

          words.forEach( word -> {
            Map wordMap = (Map) word;
            if (wordMap.get("items") != null) {
              List items = (List)wordMap.get("items");
              items.forEach( item -> {

                List xList = (List) ((Map) item).get("X");
                List yList = (List) ((Map) item).get("Y");
                List pList = (List) ((Map) item).get("F");

                xList.forEach( x -> {
                  int index = xList.indexOf(x);
//                  Log.e(TAG, "x: " + (double)x * exportScale + ", y: " + (double)yList.get(index) * exportScale + ", p: " + pList.get(index));
                  NotePointer pointer = new NotePointer((double)x * exportScale, (double)yList.get(index) * exportScale, -1, (double)pList.get(index));
                  pointers.add(pointer);
                });
              });
            }
          });

          List<NotePointer> subList = new ArrayList<>(pointers.subList(0, pointers.size() / 12));
          this.splitPointer(subList);

          Log.e(TAG, "words: " + words.size() + ", subList.size: " + subList.size());
          Log.e(TAG, "words: " + words.size() + ", pointers.size: " + pointers.size());
        } catch (IOException e) {
          e.printStackTrace();
        }
        break;
      default:
        Log.e(TAG, "Failed to handle click event");
        break;
    }
  }

  private void splitPointer(List<NotePointer> pointers) {
    Log.e(TAG, "开始拆分抛点");

    editorView.getEditor().clear();

    NotePointer prePointer = null;

    float viewScale = 0.07f;

    int count = 0;

//    for (NotePointer pointer : pointers) {
//      count += 1;
//
//      double pre = prePointer == null ? 0 : prePointer.p;
//
//      if (pre <= 0 && pointer.p > 0) {
////        Log.e(TAG, "111：x：" + pointer.x + ", viewScale：" + viewScale + "，result：" + pointer.x * viewScale);
//        editorView.getEditor().pointerDown((float)pointer.x, (float)pointer.y, -1, (float)pointer.p, PointerType.PEN, 10);
//      } else if (pre > 0 && pointer.p > 0) {
////        Log.e(TAG, "222：x：" + pointer.x + ", viewScale：" + viewScale + "，result：" + pointer.x * viewScale);
//        editorView.getEditor().pointerMove((float)pointer.x, (float)pointer.y, -1, (float)pointer.p, PointerType.PEN, 10);
//      } else if (pre > 0 && pointer.p <= 0) {
////        Log.e(TAG, "333：x：" + pointer.x + ", viewScale：" + viewScale + "，result：" + pointer.x * viewScale);
//        editorView.getEditor().pointerUp((float)pointer.x, (float)pointer.y, -1, (float)pointer.p, PointerType.PEN, 10);
//      }
//
//      prePointer = pointer;
//
////      if (count == pointers.size() - 1) {
////        Log.e(TAG, "*** >>> finish !!!!!");
////      } else {
////        Log.e(TAG, "*** >>> current count: " + count);
////      }
//    }

    int nbPointers = pointers.size();

    for(int i=0;i<nbPointers;i++)
    {
      NotePointer pointer = pointers.get(i);
      if(i==0)
        editorView.getEditor().pointerDown((float)pointer.x, (float)pointer.y, -1, (float)pointer.p, PointerType.PEN, 10);
      if (i==nbPointers-1)
        editorView.getEditor().pointerUp((float)pointer.x, (float)pointer.y, -1, (float)pointer.p, PointerType.PEN, 10);
      else
      {
        editorView.getEditor().pointerMove((float)pointer.x, (float)pointer.y, -1, (float)pointer.p, PointerType.PEN, 10);
      }
    }
  }

  private void importPointer() {
    Log.e(TAG, "开始抛点");

    InputStream input = getResources().openRawResource(R.raw.bottom3);
    Reader reader = new InputStreamReader(input);
    StringBuffer stringBuffer = new StringBuffer();
    char b[] = new char[1024];
    int len = 1;

    try {
      while ((len = reader.read(b)) != -1) {
        stringBuffer.append(b);
      }
    } catch (IOException e) {
      Log.e(TAG, "Reading file io exception");
    }

    String result = stringBuffer.toString();

    Log.e(TAG, "result: " + result);

    Log.e(TAG, "读点抛点");

    String array[] = result.split(",");
    Log.e(TAG, "array.count：" + array.length);

    NotePenPointer prePointer = null;

    float viewScale = 0.07f;

    int count = 0;

    for (String p : array) {
      count += 1;
      Log.e(TAG, "p: " + p);

      String pArray[] = p.split("a");
      Log.e(TAG, "pArray: " + pArray.length);

      if (pArray.length != 4) break;

      NotePenPointer pointer = new NotePenPointer(Long.parseLong(pArray[0]), Long.parseLong(pArray[1]), Double.parseDouble(pArray[2]), Long.parseLong(pArray[3]));

      long pre = prePointer == null ? 0 : prePointer.p;

      if (pre <= 0 && pointer.p > 0) {
        Log.e(TAG, "111：x：" + pointer.x + ", p：" + pointer.p + ", p2: " + (pointer.p / 512) + "，result：" + pointer.x * viewScale);
        editorView.getEditor().pointerDown(pointer.x * viewScale, pointer.y * viewScale, -1, (pointer.p / 512), PointerType.PEN, 100);
      } else if (pre > 0 && pointer.p > 0) {
        Log.e(TAG, "222：x：" + pointer.x + ", p：" + pointer.p + ", p2: " + (pointer.p / 512) + "，result：" + pointer.x * viewScale);
        editorView.getEditor().pointerMove(pointer.x * viewScale, pointer.y * viewScale, -1, (pointer.p / 512), PointerType.PEN, 100);
      } else if (pre > 0 && pointer.p <= 0) {
        Log.e(TAG, "333：x：" + pointer.x + ", p：" + pointer.p + ", p2: " + (pointer.p / 512) + "，result：" + pointer.x * viewScale);
        editorView.getEditor().pointerUp(pointer.x * viewScale, pointer.y * viewScale, -1, (pointer.p / 512), PointerType.PEN, 100);
      }

      prePointer = pointer;

      if (count == array.length - 1) {
        Log.e(TAG, "*** >>> finish !!!!!");
      } else {
        Log.e(TAG, "*** >>> current count: " + count);
      }
    }
  }

  private void setInputMode(int inputMode)
  {
    editorView.setInputMode(inputMode);
    findViewById(R.id.button_input_mode_forcePen).setEnabled(inputMode != InputController.INPUT_MODE_FORCE_PEN);
    findViewById(R.id.button_input_mode_forceTouch).setEnabled(inputMode != InputController.INPUT_MODE_FORCE_TOUCH);
    findViewById(R.id.button_input_mode_auto).setEnabled(inputMode != InputController.INPUT_MODE_AUTO);
  }

  private void invalidateIconButtons()
  {
    Editor editor = editorView.getEditor();
    final boolean canUndo = editor.canUndo();
    final boolean canRedo = editor.canRedo();
    runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        ImageButton imageButtonUndo = (ImageButton) findViewById(R.id.button_undo);
        imageButtonUndo.setEnabled(canUndo);
        ImageButton imageButtonRedo = (ImageButton) findViewById(R.id.button_redo);
        imageButtonRedo.setEnabled(canRedo);
        ImageButton imageButtonClear = (ImageButton) findViewById(R.id.button_clear);
        imageButtonClear.setEnabled(contentPart != null);
      }
    });
  }
}

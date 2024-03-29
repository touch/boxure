package boxure;

import clojure.lang.DynamicClassLoader;
import clojure.lang.RT;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Array;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.Arrays;



public class BoxureClassLoader extends DynamicClassLoader {

  static {
      registerAsParallelCapable();
  }

  public final static String ISOLATE =
    "clojure[./]lang[./]Agent.*"  // We want a threadpool per boxure instance.

    // All Clojure functions that are loaded by Boxure need to be redefined,
    // such that they use the isolated classes.
    + "|clojure[./]main.*"
    + "|clojure[./]edn.*"
    + "|clojure[./]walk.*"
    + "|clojure[./]set.*"
    + "|clojure[./]tools.*"
    + "|clojure[./]data.*"
    + "|clojure[./]test.*"
    + "|clojure[./]template.*"
    + "|clojure[./]stacktrace.*"
    + "|clojure[./]pprint.*"
    + "|clojure[./]zip.*"
    + "|boxure[./]core.*"
    + "|leiningen[./]core.*"
    + "|cemerick[./]pomegranate.*"
    + "|dynapath.*"
    + "|useful.*"
    + "|classlojure[./]core.*"
    + "|pedantic.*"
    + "|compile__stub.*"
    ;


  private final String userIsolate;
  private final boolean logging;

  public BoxureClassLoader(final URL[] urls, final ClassLoader parent, final String userIsolate,
                           final boolean logging) {
    super(urls, parent, new clojure.lang.LoaderContext());
    this.userIsolate = userIsolate;
    this.logging = logging;
  }


  /**
   * Process:
   * 1. Check if it is already loaded by this classloader, if so return it.
   * 2. Check if the class needs to be loaded in isolation. If so, try
   *    to load the class here, or fail.
   * 3. Try to load the class in the normal way (parent first).
   */
  protected Class<?> loadClass(final String name, final boolean resolve)
    throws ClassNotFoundException {

    Class<?> clazz = super.findLoadedClass(name);
    if (clazz == null) clazz = DynamicClassLoader.findInMemoryClass(context, name);
    if (clazz != null) {
      if (logging) {
        log("[Boxure returning already loading class: "+ name +"]");
        if (this != clazz.getClassLoader() && name.endsWith(RT.LOADER_SUFFIX)) {
          Object injected = context.injectNamespaces(clojure.lang.LoaderContext.ROOT, name.substring(0, name.length() - RT.LOADER_SUFFIX.length()));
          log("  [injected outer namespaces: "+ injected +"]");
        }
      }
      return clazz;
    }

    if (name.matches(ISOLATE) || name.matches(userIsolate)) {
      try {
        final Class<?> isoClazz = super.findClass(name);
        if (logging) log("[Boxure loading class "+ name +" in isolation]");
        return isoClazz;
      } catch (ClassNotFoundException cnfe) {
        if (logging) log("[Boxure could not load class "+ name +" in isolation]");
        throw cnfe;
      }
    } else {
      try {
        if (logging) log("[Boxure loading class "+ name +" by normal classloading.]");
        final Class<?> superClazz = super.loadClass(name, resolve);
        if (this != superClazz.getClassLoader()) {
          if (logging) log("  |- class was loaded outside box, (need to resolve? " + resolve + ") by: "+ superClazz.getClassLoader());
          if (logging) log("  |- current context ClassLoader: "+ Thread.currentThread().getContextClassLoader());
          if (name.endsWith(RT.LOADER_SUFFIX)) {
            // This is pre-compiled Clojure class that needs to be initialized in the ROOT context,
            // in order to create the Namespace object(s) related to this class.
            if (!resolve) {
              ClassLoader cl = Thread.currentThread().getContextClassLoader();
              Thread.currentThread().setContextClassLoader(superClazz.getClassLoader());
              if (logging) log("  [forcing initialization in ROOT]");
              try {
                Class.forName(name, true, superClazz.getClassLoader());
              } finally {
                Thread.currentThread().setContextClassLoader(cl);
              }
            }
            // Inject the required Namespace into the current loader context, otherwise we get:
            // java.lang.ExceptionInInitializerError Caused by: 'No namespace: ... found'
            Object injected = context.injectNamespaces(clojure.lang.LoaderContext.ROOT, name.substring(0, name.length() - RT.LOADER_SUFFIX.length()));
            if (logging) log("  [injected outer namespaces: "+ injected +"]");
          }
        }
        return superClazz;
      } catch (ClassNotFoundException cnfe2) {
        if (logging) log("[Boxure could not load "+ name +" by normal classloading]");
        throw cnfe2;
      }
    }
  }


  public URL getResource(String name) {
    if (name.matches(ISOLATE) || name.matches(userIsolate)) {
      final URL resource = super.findResource(name);
      if (logging) log("[Boxure loading resource "+ name +" in isolation]");
      return resource;
    } else {
      if (logging) log("[Boxure loading resource "+ name +" checking parent first]");
      return super.getResource(name);
    }
  }

  private void log(final String msg) {
    if (logging) {
      try {
        System.out.println(msg);
      } catch (Exception e) {
        System.out.println(e.getMessage());
        e.printStackTrace();
        System.out.println(msg.toString());
      }
    }
  }


  /** ThreadLocal clearing. **/

  private static Field threadLocalsField = null;
  private static Class threadLocalMapClass = null;
  private static Field tableField = null;
  private static Field referentField = null;
  private static Field entryField = null;

  static {
    try {
      threadLocalsField = Thread.class.getDeclaredField("threadLocals");
      threadLocalsField.setAccessible(true);
      threadLocalMapClass = Class.forName("java.lang.ThreadLocal$ThreadLocalMap");
      tableField = threadLocalMapClass.getDeclaredField("table");
      tableField.setAccessible(true);
      referentField = Reference.class.getDeclaredField("referent");
      referentField.setAccessible(true);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public static void gc() {
    Object obj = new Object();
    WeakReference ref = new WeakReference<Object>(obj);
    //noinspection UnusedAssignment
    obj = null;
    while(ref.get() != null) {
      System.gc();
    }
  }

  public static void cleanThreadLocals() {
    cleanThreadLocals(Thread.currentThread());
  }

  public static void cleanThreadLocals(Thread thread) {
    try {
      // Get a reference to the thread locals table of the current thread
      Object threadLocalTable = threadLocalsField.get(thread);

      if (threadLocalTable != null) {
        // Get a reference to the array holding the thread local variables inside the
        // ThreadLocalMap of the current thread
        Object table = tableField.get(threadLocalTable);

        // The key to the ThreadLocalMap is a WeakReference object. The referent field of this
        // object is a reference to the actual ThreadLocal variable
        for (int i=0; i < Array.getLength(table); i++) {
          // Each entry in the table array of ThreadLocalMap is an Entry object
          // representing the thread local reference and its value
          Reference entry = (Reference)Array.get(table, i);
          if (entry != null) {
            // Get a reference to the thread local object and remove it from the table
            ThreadLocal threadLocal = (ThreadLocal)referentField.get(entry);
            if (threadLocal != null) threadLocal.remove(); // somehow can be null sometimes!

            // Clean the entry.
            if (entryField == null) {
              entryField = entry.getClass().getDeclaredField("value");
              entryField.setAccessible(true);
            }
            entry.clear();
            entryField.set(entry, null);
          }
        }
      }
    } catch(Exception e) {
      // We will tolerate an exception here and just log it
      throw new IllegalStateException(e);
    }
  }
}
